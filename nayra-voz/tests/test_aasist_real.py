"""Carga el modelo AASIST real con los pesos oficiales, si están descargados (scripts/descargar_modelos.sh)."""
import os
from pathlib import Path

import numpy as np
import pytest

PESOS = Path(os.environ.get("NAYRA_VOZ_MODELOS_DIR", Path(__file__).resolve().parents[1] / "modelos")) / "aasist/AASIST.pth"


@pytest.mark.skipif(not PESOS.exists(), reason="pesos de AASIST no descargados")
def test_aasist_real_produce_puntaje_determinista():
    from nayra_voz.antispoofing import DetectorAasist, ajustar_longitud

    d = DetectorAasist(PESOS)
    m = (np.random.default_rng(0).standard_normal(48000) * 0.05).astype(np.float32)
    p1, p2 = d.puntaje(m), d.puntaje(m)
    assert isinstance(p1, float) and np.isfinite(p1) and p1 == p2
    assert len(ajustar_longitud(m, 64600)) == 64600
