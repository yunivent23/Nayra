import os

from fastapi.testclient import TestClient

from nayra_voz.almacen import AlmacenPerfilesEnMemoria, CifradorEmbeddings
from nayra_voz.antispoofing import EvaluadorSpoofing
from nayra_voz.api import Componentes, crear_app
from nayra_voz.calidad import EvaluadorCalidad
from nayra_voz.config import cargar_parametros
from nayra_voz.contenido import TranscriptorPin, VerificadorContenido
from nayra_voz.pipeline import Pipeline
from nayra_voz.vocabulario import cargar_vocabulario
from tests.falsos import DetectorFalso, ExtractorFalso, ReconocedorFalso, vector
from tests.utilidades import a_wav, grabacion

P = cargar_parametros()
V = cargar_vocabulario()
TOKEN = "token-de-prueba"
CAB = {"Authorization": f"Bearer {TOKEN}"}
DESAFIO = "llave, cuatro, siete, dos, mesa"
U1 = "0f6c5a1e-1111-4a2b-8c3d-000000000001"
WAV = a_wav(grabacion())


def cliente(palabras=None):
    rec = ReconocedorFalso(palabras or [(w, 0.9) for w in DESAFIO.replace(",", "").split()])
    pipeline = Pipeline(
        P, EvaluadorCalidad(P.calidad), VerificadorContenido(rec, V, P.contenido),
        EvaluadorSpoofing(DetectorFalso(0.9), P.antispoofing), ExtractorFalso([vector(1)]),
        AlmacenPerfilesEnMemoria(), CifradorEmbeddings(os.urandom(32)),
    )
    return TestClient(crear_app(Componentes(P, pipeline, TranscriptorPin(rec, V, P.contenido)), token_servicio=TOKEN))


def archivo(datos=WAV):
    return {"audio": ("muestra.wav", datos, "audio/wav")}


def test_exige_token_de_servicio():
    c = cliente()
    assert c.get("/prototipo/v1/salud").status_code == 401
    assert c.get("/prototipo/v1/salud", headers={"Authorization": "Bearer otro"}).status_code == 401
    assert c.get("/prototipo/v1/salud", headers=CAB).json()["listo"] is True


def test_sin_modelos_responde_503():
    c = TestClient(crear_app(None, token_servicio=TOKEN))
    r = c.post("/prototipo/v1/verificaciones", data={"usuarioId": U1, "desafio": DESAFIO}, files=archivo(), headers=CAB)
    assert r.status_code == 503


def test_flujo_enrolamiento_y_verificacion():
    c = cliente()
    for i in range(3):
        r = c.post("/prototipo/v1/enrolamientos/" + U1 + "/muestras", data={"desafio": DESAFIO}, files=archivo(), headers=CAB)
        assert r.status_code == 200 and r.json()["aprobado"] and r.json()["muestrasValidas"] == i + 1
    fin = c.post("/prototipo/v1/enrolamientos/" + U1 + "/finalizacion", headers=CAB).json()
    assert fin == {"correcto": True, "motivo": None, "muestrasValidas": 3}
    r = c.post("/prototipo/v1/verificaciones", data={"usuarioId": U1, "desafio": DESAFIO}, files=archivo(), headers=CAB)
    cuerpo = r.json()
    assert cuerpo["aprobado"] and [e["etapa"] for e in cuerpo["etapas"]][-1] == "VERIFICACION"
    assert "embedding" not in r.text.lower()


def test_formato_invalido_no_llega_al_pipeline():
    c = cliente()
    r = c.post("/prototipo/v1/verificaciones", data={"usuarioId": U1, "desafio": DESAFIO},
               files=archivo(b"RIFF basura"), headers=CAB)
    assert r.json()["motivo"] == "FORMATO_INVALIDO"


def test_transcripcion_de_pin():
    c = cliente(palabras=[(d, 0.9) for d in ["uno", "nueve", "cero", "cuatro", "cuatro", "dos"]])
    r = c.post("/prototipo/v1/transcripciones-pin", files=archivo(), headers=CAB)
    assert r.json() == {"reconocido": True, "pin": "190442"}
