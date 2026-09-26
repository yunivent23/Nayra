"""Anti-spoofing con AASIST preentrenado en ASVspoof 2019 LA (D-035)."""
from __future__ import annotations

import json
from pathlib import Path

import numpy as np

from .terceros.aasist.AASIST import Model

CONFIG = Path(__file__).resolve().parent / "terceros" / "aasist" / "AASIST.conf"


class DetectorAasist:
    nombre = "aasist-asvspoof2019-la@a04c986"

    def __init__(self, ruta_pesos: Path):
        import torch

        self._torch = torch
        config = json.loads(CONFIG.read_text())["model_config"]
        self._n = config["nb_samp"]
        self._modelo = Model(config)
        self._modelo.load_state_dict(torch.load(ruta_pesos, map_location="cpu", weights_only=True))
        self._modelo.eval()

    def puntaje(self, muestras: np.ndarray) -> float:
        """Logit de la clase 'bona fide', como en la evaluación oficial de AASIST: mayor = más parecido a voz genuina."""
        x = ajustar_longitud(muestras, self._n)
        with self._torch.no_grad():
            _, salida = self._modelo(self._torch.from_numpy(x).unsqueeze(0))
        return float(salida[0, 1])


def ajustar_longitud(muestras: np.ndarray, n: int) -> np.ndarray:
    """Recorta o repite la señal hasta n muestras (mismo criterio que el código de evaluación de AASIST)."""
    if len(muestras) >= n:
        return muestras[:n].astype(np.float32)
    repeticiones = int(np.ceil(n / len(muestras)))
    return np.tile(muestras, repeticiones)[:n].astype(np.float32)
