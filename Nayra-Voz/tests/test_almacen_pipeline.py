import base64
import os

import numpy as np
import pytest
from cryptography.exceptions import InvalidTag

from nayra_voz.almacen import AlmacenPerfilesEnMemoria, CifradorEmbeddings, clave_desde_entorno
from nayra_voz.antispoofing import EvaluadorSpoofing, ajustar_longitud
from nayra_voz.audio import decodificar_wav
from nayra_voz.calidad import EvaluadorCalidad
from nayra_voz.config import cargar_parametros
from nayra_voz.contenido import VerificadorContenido
from nayra_voz.locutor import centroide, similitud_coseno
from nayra_voz.pipeline import Pipeline, descartar_atipicas
from nayra_voz.resultados import Etapa, Motivo
from nayra_voz.vocabulario import cargar_vocabulario
from tests.falsos import DetectorFalso, ExtractorFalso, ReconocedorFalso, vector
from tests.utilidades import a_wav, grabacion, silencio

P = cargar_parametros()
V = cargar_vocabulario()
DESAFIO = "llave, cuatro, siete, dos, mesa"
PALABRAS_OK = [(w, 0.9) for w in ["llave", "cuatro", "siete", "dos", "mesa"]]
VOZ = decodificar_wav(a_wav(grabacion()), P.audio)
SILENCIO = decodificar_wav(a_wav(silencio(3)), P.audio)


def construir(palabras=PALABRAS_OK, bona_fide=0.9, embeddings=None):
    almacen = AlmacenPerfilesEnMemoria()
    extractor = ExtractorFalso(embeddings or [vector(1)])
    pipeline = Pipeline(
        P,
        EvaluadorCalidad(P.calidad),
        VerificadorContenido(ReconocedorFalso(palabras), V, P.contenido),
        EvaluadorSpoofing(DetectorFalso(bona_fide), P.antispoofing),
        extractor,
        almacen,
        CifradorEmbeddings(os.urandom(32)),
    )
    return pipeline, almacen, extractor


def enrolar(pipeline, usuario="u1"):
    for _ in range(3):
        resultado, _ = pipeline.agregar_muestra(usuario, VOZ, DESAFIO)
        assert resultado.aprobado, resultado.como_dict()
    return pipeline.finalizar_enrolamiento(usuario)


# --- Cifrado del embedding (D-013) ---

def test_cifrado_ida_y_vuelta_y_ligado_al_usuario():
    cifrador = CifradorEmbeddings(os.urandom(32))
    e = vector(3)
    perfil = cifrador.cifrar("u1", e, "modelo", "v")
    assert e.tobytes() not in perfil.cifrado
    np.testing.assert_allclose(cifrador.descifrar(perfil), e)
    movido = type(perfil)("u2", perfil.nonce, perfil.cifrado, perfil.modelo, perfil.version_parametros, perfil.creado_en)
    with pytest.raises(InvalidTag):
        cifrador.descifrar(movido)


def test_clave_desde_entorno(monkeypatch):
    monkeypatch.delenv("NAYRA_VOZ_CLAVE_EMBEDDINGS", raising=False)
    with pytest.raises(RuntimeError):
        clave_desde_entorno()
    monkeypatch.setenv("NAYRA_VOZ_CLAVE_EMBEDDINGS", base64.b64encode(b"x" * 16).decode())
    with pytest.raises(RuntimeError):
        clave_desde_entorno()
    monkeypatch.setenv("NAYRA_VOZ_CLAVE_EMBEDDINGS", base64.b64encode(b"x" * 32).decode())
    assert len(clave_desde_entorno()) == 32


# --- Utilidades biométricas ---

def test_centroide_y_coseno():
    a, b = vector(1), vector(2)
    c = centroide([a, b])
    assert np.linalg.norm(c) == pytest.approx(1.0, abs=1e-5)
    assert similitud_coseno(a, a) == pytest.approx(1.0, abs=1e-5)


def test_descarta_muestra_atipica():
    base = vector(10)
    cercanas = [vector(s, base=base, ruido=0.03) for s in (11, 12, 13)]
    lejana = vector(99)
    conservadas = descartar_atipicas(cercanas + [lejana], P.enrolamiento.similitud_minima_al_resto)
    assert len(conservadas) == 3 and not any(np.array_equal(lejana, c) for c in conservadas)


def test_ajusta_longitud_para_aasist():
    assert len(ajustar_longitud(np.ones(1000, dtype=np.float32), 64600)) == 64600
    assert len(ajustar_longitud(np.ones(70000, dtype=np.float32), 64600)) == 64600


# --- Enrolamiento (D-059) ---

def test_enrolamiento_guarda_solo_embedding_cifrado():
    pipeline, almacen, _ = construir()
    correcto, motivo, validas = enrolar(pipeline)
    assert correcto and motivo is None and validas == 3
    perfil = almacen.obtener("u1")
    assert perfil.modelo == "speechbrain/spkrec-ecapa-voxceleb"
    assert perfil.version_parametros == P.version


def test_enrolamiento_no_finaliza_con_menos_de_tres_muestras():
    pipeline, almacen, _ = construir()
    pipeline.agregar_muestra("u1", VOZ, DESAFIO)
    assert pipeline.finalizar_enrolamiento("u1") == (False, "MUESTRAS_INSUFICIENTES", 1)
    assert almacen.obtener("u1") is None


def test_enrolamiento_aplica_antispoofing_y_limite_de_muestras():
    pipeline, _, extractor = construir(bona_fide=0.1)
    for _ in range(P.enrolamiento.muestras_maximas):
        resultado, validas = pipeline.agregar_muestra("u1", VOZ, DESAFIO)
        assert resultado.motivo == Motivo.POSIBLE_SPOOFING and validas == 0
    resultado, _ = pipeline.agregar_muestra("u1", VOZ, DESAFIO)
    assert resultado.motivo == Motivo.LIMITE_MUESTRAS
    assert extractor.llamadas == 0  # no se extrae embedding de muestras rechazadas


# --- Verificación 1:1 (orden D-059, veredictos D-056) ---

def test_verificacion_exitosa_devuelve_todas_las_etapas():
    pipeline, _, _ = construir()
    enrolar(pipeline)
    r = pipeline.verificar("u1", VOZ, DESAFIO)
    assert r.aprobado and r.motivo is None
    assert [e.etapa for e in r.etapas] == [Etapa.CALIDAD, Etapa.CONTENIDO, Etapa.ANTISPOOFING, Etapa.VERIFICACION]
    assert r.etapas[-1].umbral == P.verificacion.similitud_coseno_minima


def test_verificacion_sin_referencia():
    pipeline, _, _ = construir()
    assert pipeline.verificar("desconocido", VOZ, DESAFIO).motivo == Motivo.SIN_REFERENCIA


def test_verificacion_se_detiene_en_la_primera_etapa_fallida():
    pipeline, _, extractor = construir()
    enrolar(pipeline)
    llamadas = extractor.llamadas
    r = pipeline.verificar("u1", SILENCIO, DESAFIO)
    assert r.motivo == Motivo.CALIDAD_INSUFICIENTE and len(r.etapas) == 1
    assert extractor.llamadas == llamadas


def test_verificacion_contenido_incorrecto():
    pipeline, almacen, _ = construir(palabras=[("luna", 0.9)])
    pipeline_ok, almacen_ok, _ = construir()
    enrolar(pipeline_ok)
    almacen.guardar(almacen_ok.obtener("u1"))
    pipeline._cifrador = pipeline_ok._cifrador
    r = pipeline.verificar("u1", VOZ, DESAFIO)
    assert r.motivo == Motivo.CONTENIDO_INCORRECTO and len(r.etapas) == 2


def test_verificacion_posible_spoofing():
    pipeline, _, _ = construir()
    enrolar(pipeline)
    pipeline._spoofing = EvaluadorSpoofing(DetectorFalso(0.2), P.antispoofing)
    r = pipeline.verificar("u1", VOZ, DESAFIO)
    assert r.motivo == Motivo.POSIBLE_SPOOFING and len(r.etapas) == 3


def test_verificacion_no_coincide():
    titular, impostor = vector(1), vector(2)
    pipeline, _, extractor = construir(embeddings=[titular])
    enrolar(pipeline)
    extractor.embeddings = [impostor]
    r = pipeline.verificar("u1", VOZ, DESAFIO)
    assert r.motivo == Motivo.NO_COINCIDE and not r.etapas[-1].aprobada


def test_cambio_de_modelo_exige_reenrolar():
    pipeline, _, extractor = construir()
    enrolar(pipeline)
    extractor.nombre_modelo = "otro-modelo"
    assert pipeline.verificar("u1", VOZ, DESAFIO).motivo == Motivo.SIN_REFERENCIA
