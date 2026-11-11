"""Calibrador de puntajes por regresión logística (herramienta de evaluación de D-055 / D-060).

No es un modelo biométrico nuevo ni reemplaza a ECAPA-TDNN (D-011): ECAPA sigue sin reentrenarse. Es un
modelo de dos parámetros que se ajusta sobre los puntajes coseno de un conjunto de DESARROLLO y convierte la
similitud en una probabilidad de «misma persona»:

    P(misma persona | s) = 1 / (1 + exp(-(a·s + b)))

Sirve para (1) expresar el umbral como una probabilidad interpretable y (2) comprobar en un conjunto de PRUEBA
separado que la calibración generaliza. El servicio sigue aplicando el umbral coseno de
`config/parametros_provisionales.yaml`; este calibrador NO está conectado al pipeline.

Se entrena con pesos por clase (genuinos e impostores pesan lo mismo) para que el desbalance típico (muchos más
intentos de impostor) no sesgue la probabilidad. Solo numpy (Newton-Raphson).
"""
from __future__ import annotations

import json
from dataclasses import asdict, dataclass

import numpy as np


@dataclass(frozen=True)
class CalibradorLogistico:
    a: float
    b: float
    n_genuinos: int
    n_impostores: int

    def probabilidad(self, puntajes) -> np.ndarray:
        z = self.a * np.asarray(puntajes, dtype=np.float64) + self.b
        return 1.0 / (1.0 + np.exp(-z))

    def umbral_coseno_para_probabilidad(self, probabilidad: float) -> float:
        """Similitud coseno equivalente a una probabilidad mínima (inversa de la sigmoide)."""
        if not 0.0 < probabilidad < 1.0:
            raise ValueError("La probabilidad debe estar en (0, 1).")
        return float((np.log(probabilidad / (1 - probabilidad)) - self.b) / self.a)

    def a_json(self) -> str:
        return json.dumps(asdict(self), indent=2)

    @staticmethod
    def desde_json(texto: str) -> "CalibradorLogistico":
        return CalibradorLogistico(**json.loads(texto))


def entrenar(genuinos, impostores, regularizacion: float = 1e-4, iteraciones: int = 100) -> CalibradorLogistico:
    g = np.asarray(genuinos, dtype=np.float64).ravel()
    i = np.asarray(impostores, dtype=np.float64).ravel()
    if g.size == 0 or i.size == 0:
        raise ValueError("Se necesitan intentos genuinos y de impostor.")
    x = np.concatenate([g, i])
    y = np.concatenate([np.ones_like(g), np.zeros_like(i)])
    w = np.concatenate([np.full(g.size, 0.5 / g.size), np.full(i.size, 0.5 / i.size)])
    X = np.stack([x, np.ones_like(x)], axis=1)
    theta = np.zeros(2)
    for _ in range(iteraciones):
        p = 1.0 / (1.0 + np.exp(-(X @ theta)))
        gradiente = X.T @ (w * (p - y)) + regularizacion * np.array([theta[0], 0.0])
        hessiano = (X * (w * p * (1 - p))[:, None]).T @ X + regularizacion * np.diag([1.0, 0.0])
        paso = np.linalg.solve(hessiano + 1e-12 * np.eye(2), gradiente)
        theta -= paso
        if np.max(np.abs(paso)) < 1e-10:
            break
    return CalibradorLogistico(float(theta[0]), float(theta[1]), int(g.size), int(i.size))


def entropia_cruzada(calibrador: CalibradorLogistico, genuinos, impostores) -> float:
    """Pérdida logarítmica balanceada por clase (en bits): 1.0 equivale a no saber nada; menor es mejor."""
    pg = np.clip(calibrador.probabilidad(genuinos), 1e-12, 1.0)
    pi = np.clip(1.0 - calibrador.probabilidad(impostores), 1e-12, 1.0)
    return float(-(np.mean(np.log2(pg)) + np.mean(np.log2(pi))) / 2)
