"""Métricas de verificación de hablante para la calibración de umbrales (D-055, D-060).

Convención: un puntaje más alto significa «más parecido» (similitud coseno o probabilidad). Se acepta si
`puntaje >= umbral`, igual que en `pipeline.Pipeline.verificar`.

- FAR (tasa de falsa aceptación): fracción de intentos de impostor aceptados.
- FRR (tasa de falso rechazo): fracción de intentos genuinos rechazados.
- EER: punto donde FAR = FRR.

Solo numpy: el servicio no necesita dependencias nuevas para calcularlas.
"""
from __future__ import annotations

from dataclasses import dataclass

import numpy as np


@dataclass(frozen=True)
class PuntoOperacion:
    umbral: float
    far: float
    frr: float

    def como_dict(self) -> dict:
        return {"umbral": round(self.umbral, 4), "far": round(self.far, 5), "frr": round(self.frr, 5)}


def _arreglos(genuinos, impostores) -> tuple[np.ndarray, np.ndarray]:
    g = np.asarray(genuinos, dtype=np.float64).ravel()
    i = np.asarray(impostores, dtype=np.float64).ravel()
    if g.size == 0 or i.size == 0:
        raise ValueError("Se necesitan intentos genuinos y de impostor.")
    return g, i


def tasas(genuinos, impostores, umbral: float) -> PuntoOperacion:
    """FAR y FRR con la regla de aceptación del servicio (`puntaje >= umbral`)."""
    g, i = _arreglos(genuinos, impostores)
    return PuntoOperacion(float(umbral), float(np.mean(i >= umbral)), float(np.mean(g < umbral)))


def curva_det(genuinos, impostores) -> tuple[np.ndarray, np.ndarray, np.ndarray]:
    """FAR y FRR para cada umbral candidato (todos los puntajes observados, ordenados de menor a mayor).

    Devuelve (umbrales, far, frr). FAR baja y FRR sube a medida que sube el umbral.
    """
    g, i = _arreglos(genuinos, impostores)
    umbrales = np.unique(np.concatenate([g, i]))
    g_ord, i_ord = np.sort(g), np.sort(i)
    # Rechazados = puntajes estrictamente menores que el umbral.
    frr = np.searchsorted(g_ord, umbrales, side="left") / g.size
    far = 1.0 - np.searchsorted(i_ord, umbrales, side="left") / i.size
    return umbrales, far, frr


def eer(genuinos, impostores) -> PuntoOperacion:
    """Tasa de error igual. Interpola entre los dos umbrales que rodean el cruce FAR = FRR."""
    umbrales, far, frr = curva_det(genuinos, impostores)
    diferencia = far - frr  # decreciente
    k = int(np.argmax(diferencia <= 0)) if np.any(diferencia <= 0) else len(umbrales) - 1
    if k == 0:
        return PuntoOperacion(float(umbrales[0]), float(far[0]), float(frr[0]))
    d0, d1 = diferencia[k - 1], diferencia[k]
    t = d0 / (d0 - d1) if d0 != d1 else 0.0
    umbral = umbrales[k - 1] + t * (umbrales[k] - umbrales[k - 1])
    valor = far[k - 1] + t * (far[k] - far[k - 1])
    valor_frr = frr[k - 1] + t * (frr[k] - frr[k - 1])
    return PuntoOperacion(float(umbral), float((valor + valor_frr) / 2), float((valor + valor_frr) / 2))


def umbral_para_far(genuinos, impostores, far_objetivo: float) -> PuntoOperacion:
    """Menor umbral observado cuya FAR no supera `far_objetivo` (punto de operación de baja FAR, D-055).

    Con pocos impostores la FAR mínima medible es 1/N: si `far_objetivo` es menor, el resultado solo garantiza
    FAR = 0 en estos datos, no la FAR objetivo. El informe debe decirlo (ver `far_minima_medible`).
    """
    umbrales, far, frr = curva_det(genuinos, impostores)
    validos = np.flatnonzero(far <= far_objetivo)
    if validos.size == 0:
        # Ningún umbral observado llega: por encima del máximo impostor la FAR es 0.
        _, i = _arreglos(genuinos, impostores)
        return tasas(genuinos, impostores, float(np.nextafter(i.max(), np.inf)))
    k = int(validos[0])
    return PuntoOperacion(float(umbrales[k]), float(far[k]), float(frr[k]))


def far_minima_medible(n_impostores: int) -> float:
    """Con N intentos de impostor, una FAR por debajo de 1/N no se puede distinguir de 0."""
    return 1.0 / max(n_impostores, 1)


def intervalo_wilson(errores: int, total: int, z: float = 1.96) -> tuple[float, float]:
    """Intervalo de confianza de Wilson (95 % por defecto) para una tasa de error observada."""
    if total <= 0:
        return 0.0, 1.0
    p = errores / total
    den = 1 + z * z / total
    centro = (p + z * z / (2 * total)) / den
    margen = z * np.sqrt(p * (1 - p) / total + z * z / (4 * total * total)) / den
    return float(max(0.0, centro - margen)), float(min(1.0, centro + margen))


def min_dcf(genuinos, impostores, p_objetivo: float = 0.01, c_fa: float = 1.0, c_fr: float = 1.0) -> PuntoOperacion:
    """Costo de detección mínimo normalizado (NIST SRE); devuelve el umbral que lo minimiza.

    Con p_objetivo = 0.01 penaliza más la falsa aceptación, coherente con una billetera (D-055: baja FAR).
    El valor del costo queda en `far`/`frr` del punto devuelto; el costo se obtiene con `costo_dcf`.
    """
    umbrales, far, frr = curva_det(genuinos, impostores)
    costo = c_fr * p_objetivo * frr + c_fa * (1 - p_objetivo) * far
    k = int(np.argmin(costo))
    return PuntoOperacion(float(umbrales[k]), float(far[k]), float(frr[k]))


def costo_dcf(punto: PuntoOperacion, p_objetivo: float = 0.01, c_fa: float = 1.0, c_fr: float = 1.0) -> float:
    bruto = c_fr * p_objetivo * punto.frr + c_fa * (1 - p_objetivo) * punto.far
    return float(bruto / min(c_fr * p_objetivo, c_fa * (1 - p_objetivo)))
