import numpy as np
import pytest

from nayra_voz.audio import AudioInvalido, decodificar_wav
from nayra_voz.calidad import EvaluadorCalidad
from nayra_voz.config import cargar_parametros
from tests.utilidades import a_wav, grabacion, silencio

P = cargar_parametros()


def test_parametros_marcados_como_provisionales():
    assert P.estado == "PROVISIONAL — PENDIENTE DE VALIDACIÓN"
    assert P.calidad.herramienta_vad == "webrtcvad"
    assert P.enrolamiento.muestras_validas == 3


def test_decodifica_wav_valido():
    audio = decodificar_wav(a_wav(grabacion()), P.audio)
    assert audio.frecuencia_hz == 16000
    assert audio.duracion_s == pytest.approx(3.5, abs=0.01)
    assert audio.muestras.dtype == np.float32


@pytest.mark.parametrize("fs,canales,ancho", [(8000, 1, 2), (16000, 2, 2), (16000, 1, 1)])
def test_rechaza_formato_distinto_de_d057(fs, canales, ancho):
    muestras = np.zeros(int(fs * 3 * canales), dtype=np.float32)
    with pytest.raises(AudioInvalido):
        decodificar_wav(a_wav(muestras, fs=fs, canales=canales, ancho=ancho), P.audio)


def test_rechaza_duracion_fuera_de_rango():
    with pytest.raises(AudioInvalido):
        decodificar_wav(a_wav(silencio(0.5)), P.audio)
    with pytest.raises(AudioInvalido):
        decodificar_wav(a_wav(silencio(25)), P.audio)


def test_rechaza_bytes_que_no_son_wav():
    with pytest.raises(AudioInvalido):
        decodificar_wav(b"no es un wav", P.audio)


def test_calidad_aprueba_voz_con_pausas():
    resultado = EvaluadorCalidad(P.calidad).evaluar(decodificar_wav(a_wav(grabacion()), P.audio))
    assert resultado.aprobada, resultado.detalle
    assert resultado.detalle["vozNetaS"] >= P.calidad.voz_neta_minima_s


def test_calidad_rechaza_silencio():
    resultado = EvaluadorCalidad(P.calidad).evaluar(decodificar_wav(a_wav(silencio(3)), P.audio))
    assert not resultado.aprobada
    assert "VOZ_INSUFICIENTE" in resultado.detalle["fallos"]


def test_calidad_rechaza_saturacion():
    saturada = np.clip(grabacion(amplitud=1.0) * 4, -1, 1)
    resultado = EvaluadorCalidad(P.calidad).evaluar(decodificar_wav(a_wav(saturada), P.audio))
    assert "SATURACION" in resultado.detalle["fallos"]
