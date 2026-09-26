import numpy as np
import pytest
from fastapi.testclient import TestClient

from nayra_voz.app import crear_app
from nayra_voz.biometria import normalizar
from nayra_voz.cifrado import CifradoEmbedding
from nayra_voz.motores import Motores, Transcripcion
from tests.util import ruido, wav

TOKEN = "token-prueba"


class ContenidoFalso:
    nombre = "falso"
    texto = "sol cuatro siete dos mesa"

    def transcribir(self, muestras):
        return Transcripcion(self.texto, 0.9)


class SpoofingFalso:
    nombre = "falso"

    def puntaje(self, muestras):
        return 1.5


class BiometriaFalsa:
    """Embedding determinista por semilla del audio: mismo audio => misma 'voz'."""
    nombre = "falso"
    version = "v1"

    def embedding(self, muestras):
        semilla = int(abs(muestras[:100].sum()) * 1e6) % 2**32
        return normalizar(np.random.default_rng(semilla).standard_normal(192).astype(np.float32))


class RepoMemoria:
    def __init__(self):
        self.perfiles = {}

    def obtener(self, usuario_id):
        return self.perfiles.get(usuario_id)

    def guardar(self, p):
        self.perfiles[p.usuario_id] = p

    def eliminar(self, usuario_id):
        return self.perfiles.pop(usuario_id, None) is not None


@pytest.fixture
def entorno():
    repo = RepoMemoria()
    bio = BiometriaFalsa()
    app = crear_app(TOKEN, Motores(ContenidoFalso(), SpoofingFalso(), bio), repo, CifradoEmbedding(bytes(32)))
    return TestClient(app), repo, bio


H = {"X-Nayra-Service-Token": TOKEN}
VOZ_A = wav(ruido(3, semilla=1))
VOZ_B = wav(ruido(3, semilla=2))


def verificar(cliente, audio, usuario=5, texto="sol cuatro siete dos mesa"):
    return cliente.post("/interno/v1/verificaciones", headers=H,
                        data={"usuario_id": usuario, "texto_esperado": texto},
                        files={"audio": ("a.wav", audio, "audio/wav")})


def enrolar(cliente, modo, audio=VOZ_A, usuario=5):
    return cliente.post(f"/interno/v1/perfiles/{usuario}?modo={modo}", headers=H,
                        files={f"audio_{i}": ("a.wav", audio, "audio/wav") for i in (1, 2, 3)})


def test_requiere_token_de_servicio(entorno):
    cliente, _, _ = entorno
    assert cliente.get("/interno/v1/salud").status_code == 401
    assert cliente.get("/interno/v1/salud", headers={"X-Nayra-Service-Token": "otro"}).status_code == 401
    assert cliente.get("/interno/v1/salud", headers=H).json()["estado"] == "OK"


def test_sin_perfil_devuelve_puntajes_sin_similitud(entorno):
    cliente, _, _ = entorno
    r = verificar(cliente, VOZ_A).json()
    assert r["biometria"] == {"similitud": None, "perfil_encontrado": False, "requiere_reenrolamiento": False}
    assert r["contenido"]["coincide"] is True
    assert r["spoofing"]["puntaje"] == 1.5
    assert set(r["calidad"]) == {"duracion_s", "voz_neta_s", "snr_db", "saturacion"}
    assert "decision" not in r and "aprobado" not in str(r)  # Python no decide (D-040)


def test_evaluar_no_guarda_y_guardar_cifra(entorno):
    cliente, repo, _ = entorno
    assert enrolar(cliente, "evaluar").json()["guardado"] is False
    assert repo.perfiles == {}
    r = enrolar(cliente, "guardar").json()
    assert r["guardado"] is True and r["num_muestras"] == 3
    assert len(repo.perfiles[5].embedding_cifrado) == 192 * 4 + 16  # float32 + etiqueta GCM


def test_verificacion_con_perfil(entorno):
    cliente, _, _ = entorno
    enrolar(cliente, "guardar", VOZ_A)
    misma = verificar(cliente, VOZ_A).json()["biometria"]
    otra = verificar(cliente, VOZ_B).json()["biometria"]
    assert misma["similitud"] == pytest.approx(1.0, abs=1e-5)
    assert otra["similitud"] < 0.5


def test_cambio_de_modelo_exige_reenrolamiento(entorno):
    cliente, _, bio = entorno
    enrolar(cliente, "guardar")
    bio.version = "v2"
    b = verificar(cliente, VOZ_A).json()["biometria"]
    assert b == {"similitud": None, "perfil_encontrado": False, "requiere_reenrolamiento": True}


def test_contenido_distinto_no_coincide(entorno):
    cliente, _, _ = entorno
    assert verificar(cliente, VOZ_A, texto="luna uno dos tres gato").json()["contenido"]["coincide"] is False


def test_audio_invalido_es_422(entorno):
    cliente, _, _ = entorno
    r = verificar(cliente, wav(ruido(1), frecuencia=44100))
    assert r.status_code == 422 and r.json()["error"] == "AUDIO_INVALIDO"


def test_eliminar_perfil(entorno):
    cliente, repo, _ = entorno
    enrolar(cliente, "guardar")
    assert cliente.delete("/interno/v1/perfiles/5", headers=H).status_code == 204
    assert repo.perfiles == {}
