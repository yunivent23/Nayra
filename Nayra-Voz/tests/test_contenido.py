from nayra_voz.audio import decodificar_wav
from nayra_voz.config import cargar_parametros
from nayra_voz.contenido import TranscriptorPin, VerificadorContenido, normalizar, tokens_desafio
from nayra_voz.vocabulario import cargar_vocabulario
from tests.falsos import ReconocedorFalso
from tests.utilidades import a_wav, grabacion

P = cargar_parametros()
V = cargar_vocabulario()
AUDIO = decodificar_wav(a_wav(grabacion()), P.audio)


def test_vocabulario_compartido_de_40_palabras():
    assert len(V.palabras) == 40 and len(set(V.palabras)) == 40
    assert V.digitos[0] == "cero" and V.digitos[9] == "nueve"


def test_normaliza_tildes():
    assert normalizar("Árbol") == "arbol"
    assert tokens_desafio("jardín, cuatro, siete, dos, río") == ["jardin", "cuatro", "siete", "dos", "rio"]


def test_contenido_correcto_con_confianza_suficiente():
    rec = ReconocedorFalso([("llave", 0.9), ("cuatro", 0.8), ("siete", 0.95), ("dos", 0.9), ("mesa", 0.85)])
    r = VerificadorContenido(rec, V, P.contenido).evaluar(AUDIO, "llave, cuatro, siete, dos, mesa")
    assert r.aprobada and r.puntaje == 0.8
    assert "[unk]" not in rec.gramaticas[0] and "llave" in rec.gramaticas[0]


def test_contenido_rechaza_secuencia_distinta():
    rec = ReconocedorFalso([("llave", 0.9), ("cuatro", 0.9), ("dos", 0.9), ("siete", 0.9), ("mesa", 0.9)])
    r = VerificadorContenido(rec, V, P.contenido).evaluar(AUDIO, "llave, cuatro, siete, dos, mesa")
    assert not r.aprobada and r.detalle["secuenciaCoincide"] is False


def test_contenido_rechaza_confianza_baja():
    rec = ReconocedorFalso([("llave", 0.9), ("cuatro", 0.2), ("siete", 0.9), ("dos", 0.9), ("mesa", 0.9)])
    assert not VerificadorContenido(rec, V, P.contenido).evaluar(AUDIO, "llave, cuatro, siete, dos, mesa").aprobada


def test_contenido_ignora_unk_pero_exige_secuencia_exacta():
    rec = ReconocedorFalso([("[unk]", 0.3), ("llave", 0.9), ("cuatro", 0.9), ("siete", 0.9), ("dos", 0.9), ("mesa", 0.9)])
    assert VerificadorContenido(rec, V, P.contenido).evaluar(AUDIO, "llave, cuatro, siete, dos, mesa").aprobada


def test_pin_dictado_seis_digitos():
    rec = ReconocedorFalso([(d, 0.9) for d in ["uno", "nueve", "cero", "cuatro", "cuatro", "dos"]])
    assert TranscriptorPin(rec, V, P.contenido).transcribir(AUDIO) == "190442"
    assert rec.gramaticas[0] == list(V.digitos)


def test_pin_dictado_incompleto_o_dudoso_no_se_acepta():
    t = TranscriptorPin(ReconocedorFalso([(d, 0.9) for d in ["uno", "dos", "tres"]]), V, P.contenido)
    assert t.transcribir(AUDIO) is None
    dudoso = [(d, 0.9) for d in ["uno", "dos", "tres", "cuatro", "cinco"]] + [("seis", 0.1)]
    assert TranscriptorPin(ReconocedorFalso(dudoso), V, P.contenido).transcribir(AUDIO) is None
