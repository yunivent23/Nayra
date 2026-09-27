"""Etapa de anti-spoofing (D-012): AASIST preentrenado, en CPU."""
from __future__ import annotations

from typing import Protocol

import numpy as np

from .audio import Audio
from .config import ParametrosAntispoofing
from .resultados import Etapa, ResultadoEtapa

# Configuración de la arquitectura publicada con los pesos (config/AASIST.conf del repositorio original).
# No son parámetros de Nayra.
_CONFIG_AASIST = {
    "architecture": "AASIST",
    "nb_samp": 64600,
    "first_conv": 128,
    "filts": [70, [1, 32], [32, 32], [32, 64], [64, 64]],
    "gat_dims": [64, 32],
    "pool_ratios": [0.5, 0.7, 0.5, 0.5],
    "temperatures": [2.0, 2.0, 100.0, 100.0],
}


def ajustar_longitud(muestras: np.ndarray, longitud: int) -> np.ndarray:
    """Recorta o repite la señal hasta la ventana del modelo (mismo criterio que el repositorio de AASIST)."""
    if len(muestras) >= longitud:
        return muestras[:longitud]
    repeticiones = int(np.ceil(longitud / max(len(muestras), 1)))
    return np.tile(muestras, repeticiones)[:longitud]


class DetectorSpoofing(Protocol):
    def probabilidad_bona_fide(self, muestras: np.ndarray) -> float:
        ...


class DetectorAasist:
    def __init__(self, ruta_pesos: str, muestras_entrada: int):
        import torch

        from .terceros.aasist.modelo_aasist import Model

        self._torch = torch
        self._muestras_entrada = muestras_entrada
        self._modelo = Model(_CONFIG_AASIST)
        self._modelo.load_state_dict(torch.load(ruta_pesos, map_location="cpu"))
        self._modelo.eval()

    def probabilidad_bona_fide(self, muestras: np.ndarray) -> float:
        entrada = ajustar_longitud(muestras, self._muestras_entrada)
        with self._torch.no_grad():
            _, salida = self._modelo(self._torch.from_numpy(entrada).float().unsqueeze(0))
            # Índice 1 = bona fide, como en el repositorio de AASIST.
            return float(self._torch.softmax(salida, dim=1)[0, 1])


class EvaluadorSpoofing:
    def __init__(self, detector: DetectorSpoofing, parametros: ParametrosAntispoofing):
        self._detector = detector
        self._parametros = parametros

    def evaluar(self, audio: Audio) -> ResultadoEtapa:
        probabilidad = self._detector.probabilidad_bona_fide(audio.muestras)
        umbral = self._parametros.probabilidad_bona_fide_minima
        return ResultadoEtapa(
            etapa=Etapa.ANTISPOOFING,
            aprobada=probabilidad >= umbral,
            puntaje=round(probabilidad, 4),
            umbral=umbral,
        )
