"""Embeddings ECAPA-TDNN de SpeechBrain y comparación 1:1 por similitud coseno (D-034, D-039)."""
from __future__ import annotations

from pathlib import Path

import numpy as np


class ExtractorEcapa:
    nombre = "speechbrain/spkrec-ecapa-voxceleb"

    def __init__(self, ruta_modelo: Path, version: str):
        import torch
        from speechbrain.inference.speaker import EncoderClassifier

        self._torch = torch
        self.version = version
        self._modelo = EncoderClassifier.from_hparams(source=str(ruta_modelo), savedir=str(ruta_modelo),
                                                      run_opts={"device": "cpu"})

    def embedding(self, muestras: np.ndarray) -> np.ndarray:
        with self._torch.no_grad():
            e = self._modelo.encode_batch(self._torch.from_numpy(muestras.astype(np.float32)).unsqueeze(0))
        return normalizar(e.squeeze().cpu().numpy().astype(np.float32))


def normalizar(v: np.ndarray) -> np.ndarray:
    norma = float(np.linalg.norm(v))
    if norma == 0.0:
        raise ValueError("Embedding nulo")
    return (v / norma).astype(np.float32)


def centroide(embeddings: list[np.ndarray]) -> np.ndarray:
    """Promedio de embeddings normalizados, renormalizado (D-039)."""
    return normalizar(np.mean(np.stack([normalizar(e) for e in embeddings]), axis=0))


def similitud(a: np.ndarray, b: np.ndarray) -> float:
    return float(np.dot(normalizar(a), normalizar(b)))
