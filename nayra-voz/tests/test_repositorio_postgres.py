"""Persistencia real en el esquema biometria. Requiere NAYRA_VOZ_IT_DSN (usuario nayra_voz de una BD de pruebas)."""
import os

import numpy as np
import pytest

from nayra_voz.cifrado import CifradoEmbedding
from nayra_voz.repositorio import PerfilCifrado, RepositorioPostgres

DSN = os.environ.get("NAYRA_VOZ_IT_DSN")


@pytest.mark.skipif(not DSN, reason="NAYRA_VOZ_IT_DSN no definido")
def test_guardar_reemplazar_obtener_eliminar():
    repo = RepositorioPostgres(DSN)
    repo.eliminar(99)
    cif = CifradoEmbedding(bytes(32))
    e1 = (np.ones(192) / np.sqrt(192)).astype(np.float32)
    datos, iv = cif.cifrar(e1, 99, "v1")
    repo.guardar(PerfilCifrado(99, datos, iv, "m", "v1", 3))
    e2 = -e1
    datos2, iv2 = cif.cifrar(e2, 99, "v1")
    repo.guardar(PerfilCifrado(99, datos2, iv2, "m", "v1", 3))  # re-enrolamiento reemplaza
    p = repo.obtener(99)
    assert np.array_equal(cif.descifrar(p.embedding_cifrado, p.iv, 99, p.modelo_version), e2)
    assert repo.eliminar(99) and repo.obtener(99) is None
