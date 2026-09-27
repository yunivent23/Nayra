"""Embedding y verificación 1:1 (D-011): SpeechBrain ECAPA-TDNN, similitud coseno. Nunca 1:N."""
from __future__ import annotations

from typing import Protocol

import numpy as np


def normalizar_l2(vector: np.ndarray) -> np.ndarray:
    norma = float(np.linalg.norm(vector))
    if norma == 0.0:
        raise ValueError("Embedding nulo.")
    return (vector / norma).astype(np.float32)


def similitud_coseno(a: np.ndarray, b: np.ndarray) -> float:
    return float(np.dot(normalizar_l2(a), normalizar_l2(b)))


def centroide(embeddings: list[np.ndarray]) -> np.ndarray:
    """Promedio renormalizado de las muestras válidas (D-013)."""
    return normalizar_l2(np.mean([normalizar_l2(e) for e in embeddings], axis=0))


class ExtractorEmbedding(Protocol):
    nombre_modelo: str

    def extraer(self, muestras: np.ndarray) -> np.ndarray:
        """Devuelve el embedding normalizado (192 dimensiones con ECAPA)."""


class ExtractorEcapa:
    nombre_modelo = "speechbrain/spkrec-ecapa-voxceleb"

    def __init__(self, ruta_modelo: str):
        import torch
        from speechbrain.inference.speaker import EncoderClassifier

        self._torch = torch
        self._modelo = EncoderClassifier.from_hparams(source=ruta_modelo, savedir=ruta_modelo, run_opts={"device": "cpu"})

    def extraer(self, muestras: np.ndarray) -> np.ndarray:
        with self._torch.no_grad():
            embedding = self._modelo.encode_batch(self._torch.from_numpy(muestras).float().unsqueeze(0))
        return normalizar_l2(embedding.squeeze().cpu().numpy())
