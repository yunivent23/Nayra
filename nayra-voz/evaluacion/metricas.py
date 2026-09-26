"""Métricas de calibración (D-038): FAR, FRR y EER a partir de puntajes del dataset de calibración (D-049).

Uso fuera de línea, nunca con datos de usuarios del sistema. Convención: puntaje mayor = más parecido
a la clase aceptada (mismo hablante o voz genuina).
"""
from __future__ import annotations

import numpy as np


def far_frr(genuinos: np.ndarray, impostores: np.ndarray, umbral: float) -> tuple[float, float]:
    """FAR: impostores aceptados (>= umbral). FRR: genuinos rechazados (< umbral)."""
    genuinos, impostores = np.asarray(genuinos, float), np.asarray(impostores, float)
    return float(np.mean(impostores >= umbral)), float(np.mean(genuinos < umbral))


def eer(genuinos: np.ndarray, impostores: np.ndarray) -> tuple[float, float]:
    """Devuelve (EER, umbral) evaluando todos los puntajes observados como umbrales candidatos."""
    candidatos = np.unique(np.concatenate([genuinos, impostores]))
    mejor = (1.0, float(candidatos[0]), 1.0)
    for u in candidatos:
        far, frr = far_frr(genuinos, impostores, float(u))
        if abs(far - frr) < mejor[2]:
            mejor = ((far + frr) / 2, float(u), abs(far - frr))
    return mejor[0], mejor[1]


def umbral_para_far(genuinos: np.ndarray, impostores: np.ndarray, far_objetivo: float) -> tuple[float, float, float]:
    """Menor umbral cuya FAR no supera el objetivo (punto de operación orientado a baja FAR). Devuelve (umbral, FAR, FRR)."""
    for u in np.unique(np.concatenate([genuinos, impostores])):
        far, frr = far_frr(genuinos, impostores, float(u))
        if far <= far_objetivo:
            return float(u), far, frr
    u = float(np.max(np.concatenate([genuinos, impostores]))) + 1e-9
    return (u, *far_frr(genuinos, impostores, u))
