"""Carga real de AASIST con los pesos publicados. Solo comprueba que el modelo corre;
no mide ninguna tasa de detección (eso queda para la validación de D-012)."""
from pathlib import Path

import pytest

from nayra_voz.config import cargar_parametros
from tests.utilidades import grabacion

PESOS = Path(__file__).resolve().parent.parent / "modelos" / "aasist" / "AASIST.pth"


@pytest.mark.skipif(not PESOS.exists(), reason="Pesos de AASIST no descargados (scripts/descargar_modelos.sh)")
def test_aasist_carga_y_devuelve_probabilidad():
    pytest.importorskip("torch")
    from nayra_voz.antispoofing import DetectorAasist

    detector = DetectorAasist(str(PESOS), cargar_parametros().antispoofing.muestras_entrada)
    probabilidad = detector.probabilidad_bona_fide(grabacion())
    assert 0.0 <= probabilidad <= 1.0
