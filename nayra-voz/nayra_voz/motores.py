"""Interfaces de los modelos. Permiten sustituirlos por dobles en las pruebas sin cambiar la lógica."""
from __future__ import annotations

from dataclasses import dataclass
from typing import Protocol

import numpy as np


@dataclass(frozen=True)
class Transcripcion:
    texto: str
    confianza: float | None


class ReconocedorContenido(Protocol):
    nombre: str

    def transcribir(self, muestras: np.ndarray) -> Transcripcion: ...


class DetectorSpoofing(Protocol):
    nombre: str

    def puntaje(self, muestras: np.ndarray) -> float: ...


class ExtractorEmbedding(Protocol):
    nombre: str
    version: str

    def embedding(self, muestras: np.ndarray) -> np.ndarray: ...


@dataclass(frozen=True)
class Motores:
    contenido: ReconocedorContenido
    spoofing: DetectorSpoofing
    biometria: ExtractorEmbedding
