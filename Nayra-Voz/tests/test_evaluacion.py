"""Pruebas de las métricas, el calibrador y el protocolo de evaluación.

Usan puntajes y embeddings SINTÉTICOS con propiedades conocidas: comprueban que el cálculo es correcto, no
miden el desempeño de ECAPA-TDNN ni de AASIST.
"""
import numpy as np
import pytest

from nayra_voz.evaluacion import metricas
from nayra_voz.evaluacion.calibrador import CalibradorLogistico, entrenar, entropia_cruzada
from nayra_voz.evaluacion.protocolo import Hablante, construir_ensayos, particion


def test_tasas_usa_la_regla_de_aceptacion_del_servicio():
    # Se acepta con puntaje >= umbral (Pipeline.verificar).
    p = metricas.tasas([0.7, 0.8, 0.9], [0.1, 0.8], 0.8)
    assert p.far == pytest.approx(0.5)
    assert p.frr == pytest.approx(1 / 3)


def test_eer_de_clases_separadas_es_cero():
    p = metricas.eer([0.8, 0.9, 0.95], [0.1, 0.2, 0.3])
    assert p.far == 0.0 and p.frr == 0.0
    assert 0.3 <= p.umbral <= 0.8


def test_eer_de_gaussianas_coincide_con_la_teoria():
    rng = np.random.default_rng(0)
    g = rng.normal(1.0, 1.0, 200_000)
    i = rng.normal(-1.0, 1.0, 200_000)
    # Con medias ±1 y varianza 1, el EER teórico es Φ(-1) ≈ 0.1587 con umbral 0.
    p = metricas.eer(g, i)
    assert p.far == pytest.approx(0.1587, abs=0.003)
    assert p.umbral == pytest.approx(0.0, abs=0.02)


def test_umbral_para_far_respeta_el_objetivo():
    rng = np.random.default_rng(1)
    g, i = rng.normal(0.7, 0.1, 5000), rng.normal(0.2, 0.1, 5000)
    p = metricas.umbral_para_far(g, i, 0.01)
    assert p.far <= 0.01
    assert metricas.tasas(g, i, p.umbral).far == pytest.approx(p.far)


def test_umbral_para_far_sin_umbral_observado_usa_el_maximo_impostor():
    p = metricas.umbral_para_far([0.5, 0.6], [0.4, 0.9], 0.0)
    assert p.far == 0.0 and p.umbral > 0.9


def test_intervalo_wilson_contiene_la_tasa():
    bajo, alto = metricas.intervalo_wilson(5, 100)
    assert bajo < 0.05 < alto
    assert metricas.intervalo_wilson(0, 100)[0] == 0.0


def test_calibrador_recupera_una_sigmoide_conocida():
    rng = np.random.default_rng(2)
    s = rng.uniform(-1, 1, 200_000)
    etiquetas = rng.uniform(size=s.size) < 1 / (1 + np.exp(-(8 * s - 2)))
    c = entrenar(s[etiquetas], s[~etiquetas])
    # El entrenamiento balancea las clases; la pendiente no cambia y el sesgo se corre por el log del desbalance.
    assert c.a == pytest.approx(8, rel=0.05)
    previa = np.log(etiquetas.mean() / (1 - etiquetas.mean()))
    assert c.b == pytest.approx(-2 - previa, abs=0.1)


def test_calibrador_inversa_y_serializacion():
    c = CalibradorLogistico(a=10.0, b=-5.0, n_genuinos=1, n_impostores=1)
    u = c.umbral_coseno_para_probabilidad(0.9)
    assert c.probabilidad([u])[0] == pytest.approx(0.9)
    assert CalibradorLogistico.desde_json(c.a_json()) == c


def test_calibrador_informativo_tiene_entropia_menor_a_un_bit():
    rng = np.random.default_rng(3)
    g, i = rng.normal(0.7, 0.1, 2000), rng.normal(0.2, 0.1, 2000)
    assert entropia_cruzada(entrenar(g, i), g, i) < 0.2


def _hablante(k: int, genero: str, rng, n: int = 6, dim: int = 16) -> Hablante:
    centro = rng.normal(size=dim)
    return Hablante(f"h{k}", genero, [centro + 0.2 * rng.normal(size=dim) for _ in range(n)])


def test_protocolo_genuinos_superan_a_impostores_y_cuenta_ensayos():
    rng = np.random.default_rng(4)
    hablantes = [_hablante(k, "f" if k % 2 else "m", rng) for k in range(6)]
    e = construir_ensayos(hablantes, muestras_enrolamiento=3)
    assert e.hablantes == 6
    assert e.genuinos.size == 6 * 3                 # 6 grabaciones: 3 de enrolamiento, 3 de prueba
    assert e.impostores.size == 6 * 3 * 5           # cada prueba contra los otros 5 perfiles
    assert e.impostores_mismo_genero.size == 6 * 3 * 2
    assert e.genuinos.min() > e.impostores.max()


def test_protocolo_descarta_hablantes_con_pocas_grabaciones():
    rng = np.random.default_rng(5)
    hablantes = [_hablante(0, "f", rng), _hablante(1, "f", rng), _hablante(2, "f", rng, n=3)]
    assert construir_ensayos(hablantes, muestras_enrolamiento=3).hablantes == 2


def test_particion_es_estable_y_usa_ambos_grupos():
    grupos = {particion(f"pef_{k:05d}") for k in range(50)}
    assert grupos == {"desarrollo", "prueba"}
    assert particion("pef_00001") == particion("pef_00001")
