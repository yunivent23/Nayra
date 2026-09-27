"""Validación y decodificación del audio (D-057): WAV PCM 16 kHz, mono, 16 bits.

El audio se procesa solo en memoria; nunca se escribe en disco ni en logs (D-013).
"""
from __future__ import annotations

import io
import wave
from dataclasses import dataclass

import numpy as np

from .config import ParametrosAudio


class AudioInvalido(ValueError):
    """El audio no cumple el formato aprobado o las duraciones provisionales."""


@dataclass(frozen=True)
class Audio:
    pcm16: bytes            # muestras int16 little-endian, para WebRTC VAD y Vosk
    muestras: np.ndarray    # float32 en [-1, 1], para AASIST y ECAPA
    frecuencia_hz: int

    @property
    def duracion_s(self) -> float:
        return len(self.muestras) / float(self.frecuencia_hz)


def decodificar_wav(datos: bytes, parametros: ParametrosAudio) -> Audio:
    try:
        with wave.open(io.BytesIO(datos), "rb") as wav:
            if wav.getcomptype() != "NONE":
                raise AudioInvalido("El audio debe ser PCM sin compresión.")
            if wav.getframerate() != parametros.frecuencia_hz:
                raise AudioInvalido("Frecuencia de muestreo no admitida.")
            if wav.getnchannels() != parametros.canales:
                raise AudioInvalido("El audio debe ser mono.")
            if wav.getsampwidth() * 8 != parametros.bits:
                raise AudioInvalido("El audio debe ser de 16 bits.")
            pcm = wav.readframes(wav.getnframes())
    except (wave.Error, EOFError) as exc:
        raise AudioInvalido("El archivo no es un WAV válido.") from exc

    enteros = np.frombuffer(pcm, dtype="<i2")
    audio = Audio(pcm16=pcm, muestras=enteros.astype(np.float32) / 32768.0, frecuencia_hz=parametros.frecuencia_hz)
    if audio.duracion_s < parametros.duracion_minima_s:
        raise AudioInvalido("La grabación es demasiado corta.")
    if audio.duracion_s > parametros.duracion_maxima_s:
        raise AudioInvalido("La grabación es demasiado larga.")
    return audio
