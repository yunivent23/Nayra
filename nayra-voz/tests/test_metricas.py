import numpy as np
import pytest

from evaluacion.metricas import eer, far_frr, umbral_para_far


def test_far_frr_y_eer_en_caso_separable():
    gen, imp = np.array([0.8, 0.9, 0.7]), np.array([0.1, 0.2, 0.3])
    assert far_frr(gen, imp, 0.5) == (0.0, 0.0)
    assert eer(gen, imp)[0] == 0.0


def test_umbral_para_far_objetivo():
    gen = np.array([0.4, 0.6, 0.8, 0.9])
    imp = np.array([0.1, 0.2, 0.5, 0.7])
    u, far, frr = umbral_para_far(gen, imp, 0.25)
    assert far <= 0.25
    assert (u, far, frr) == (0.6, 0.25, 0.25)
    assert eer(gen, imp)[0] == pytest.approx(0.25)
