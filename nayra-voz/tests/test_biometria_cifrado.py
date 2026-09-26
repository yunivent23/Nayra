import numpy as np
import pytest
from cryptography.exceptions import InvalidTag

from nayra_voz import biometria
from nayra_voz.cifrado import CifradoEmbedding


def test_centroide_normalizado_y_similitud():
    rng = np.random.default_rng(1)
    base = rng.standard_normal(192)
    muestras = [base + rng.standard_normal(192) * 0.1 for _ in range(3)]
    c = biometria.centroide(muestras)
    assert np.linalg.norm(c) == pytest.approx(1.0, abs=1e-5)
    assert biometria.similitud(muestras[0], c) > 0.95
    assert biometria.similitud(rng.standard_normal(192), c) < 0.5


def test_cifrado_ida_y_vuelta_ligado_a_usuario_y_version():
    cif = CifradoEmbedding(bytes(range(32)))
    e = biometria.normalizar(np.arange(1, 193, dtype=np.float32))
    datos, iv = cif.cifrar(e, 7, "rev1")
    assert e.tobytes() not in datos
    assert np.array_equal(cif.descifrar(datos, iv, 7, "rev1"), e)
    with pytest.raises(InvalidTag):
        cif.descifrar(datos, iv, 8, "rev1")
    with pytest.raises(InvalidTag):
        cif.descifrar(datos, iv, 7, "rev2")
