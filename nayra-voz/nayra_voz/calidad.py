"""Medidas de calidad de la muestra (HU-31, HU-32). Solo calcula valores; los umbrales los aplica Java (D-038)."""
from __future__ import annotations

from dataclasses import dataclass

import numpy as np
import webrtcvad

from .audio import FRECUENCIA, a_pcm16

MS_TRAMA = 30
MUESTRAS_TRAMA = FRECUENCIA * MS_TRAMA // 1000
NIVEL_SATURACION = 32767 / 32768


@dataclass(frozen=True)
class Calidad:
    duracion_s: float
    voz_neta_s: float
    snr_db: float | None
    saturacion: float


def medir(muestras: np.ndarray, agresividad: int = 2) -> Calidad:
    vad = webrtcvad.Vad(agresividad)
    n_tramas = len(muestras) // MUESTRAS_TRAMA
    potencia_voz, potencia_ruido = [], []
    for i in range(n_tramas):
        trama = muestras[i * MUESTRAS_TRAMA:(i + 1) * MUESTRAS_TRAMA]
        potencia = float(np.mean(trama.astype(np.float64) ** 2))
        if vad.is_speech(a_pcm16(trama), FRECUENCIA):
            potencia_voz.append(potencia)
        else:
            potencia_ruido.append(potencia)

    snr = None
    if potencia_voz and potencia_ruido:
        ruido = max(float(np.mean(potencia_ruido)), 1e-12)
        snr = 10.0 * float(np.log10(max(float(np.mean(potencia_voz)), 1e-12) / ruido))

    return Calidad(
        duracion_s=round(len(muestras) / FRECUENCIA, 3),
        voz_neta_s=round(len(potencia_voz) * MS_TRAMA / 1000, 3),
        snr_db=None if snr is None else round(snr, 2),
        saturacion=round(float(np.mean(np.abs(muestras) >= NIVEL_SATURACION)), 5),
    )
