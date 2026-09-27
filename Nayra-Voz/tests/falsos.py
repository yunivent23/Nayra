"""Dobles de prueba para los modelos (no reemplazan a ECAPA, AASIST ni Vosk en el prototipo)."""
from __future__ import annotations

import numpy as np

from nayra_voz.locutor import normalizar_l2


class ReconocedorFalso:
    def __init__(self, palabras: list[tuple[str, float]]):
        self.palabras = palabras
        self.gramaticas: list[list[str]] = []

    def reconocer(self, audio, gramatica):
        self.gramaticas.append(gramatica)
        return list(self.palabras)


class DetectorFalso:
    def __init__(self, probabilidad: float):
        self.probabilidad = probabilidad

    def probabilidad_bona_fide(self, muestras):
        return self.probabilidad


class ExtractorFalso:
    """Devuelve embeddings de una cola, o uno fijo."""

    nombre_modelo = "speechbrain/spkrec-ecapa-voxceleb"

    def __init__(self, embeddings: list[np.ndarray]):
        self.embeddings = list(embeddings)
        self.llamadas = 0

    def extraer(self, muestras):
        self.llamadas += 1
        e = self.embeddings.pop(0) if len(self.embeddings) > 1 else self.embeddings[0]
        return normalizar_l2(e)


def vector(semilla: int, base: np.ndarray | None = None, ruido: float = 1.0) -> np.ndarray:
    rng = np.random.default_rng(semilla)
    v = rng.standard_normal(192).astype(np.float32)
    return normalizar_l2(v if base is None else base + ruido * v)
