import numpy as np
import pytest

from nayra_voz import calidad
from nayra_voz.audio import AudioInvalido, leer_wav
from tests.util import ruido, wav


def test_acepta_formato_aprobado():
    m = leer_wav(wav(ruido(1.0)))
    assert m.dtype == np.float32 and len(m) == 16000


@pytest.mark.parametrize("args", [dict(frecuencia=8000), dict(canales=2), dict(ancho=1)])
def test_rechaza_formatos_distintos(args):
    with pytest.raises(AudioInvalido):
        leer_wav(wav(ruido(0.5), **args))


def test_rechaza_basura_y_vacio():
    with pytest.raises(AudioInvalido):
        leer_wav(b"no es un wav")
    with pytest.raises(AudioInvalido):
        leer_wav(wav(np.zeros(0, dtype=np.float32)))


def test_silencio_no_tiene_voz_neta():
    c = calidad.medir(np.zeros(32000, dtype=np.float32))
    assert c.duracion_s == 2.0
    assert c.voz_neta_s == 0.0
    assert c.saturacion == 0.0


def test_mide_saturacion():
    m = ruido(1.0, amplitud=0.1)
    m[:1600] = 1.0  # 10 % de muestras saturadas
    assert calidad.medir(leer_wav(wav(m))).saturacion == pytest.approx(0.1, abs=0.001)
