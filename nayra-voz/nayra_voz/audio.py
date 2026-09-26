"""Lectura y validación del audio (D-045): WAV PCM 16 kHz, mono, 16 bits."""
from __future__ import annotations

import io
import wave

import numpy as np

FRECUENCIA = 16000


class AudioInvalido(ValueError):
    pass


def leer_wav(datos: bytes) -> np.ndarray:
    """Devuelve las muestras como float32 en [-1, 1]. Rechaza cualquier formato distinto al aprobado."""
    try:
        with wave.open(io.BytesIO(datos), "rb") as w:
            if w.getcomptype() != "NONE":
                raise AudioInvalido("El audio debe ser PCM sin compresión")
            if w.getframerate() != FRECUENCIA:
                raise AudioInvalido("El audio debe ser de 16 kHz")
            if w.getnchannels() != 1:
                raise AudioInvalido("El audio debe ser mono")
            if w.getsampwidth() != 2:
                raise AudioInvalido("El audio debe ser de 16 bits")
            pcm = w.readframes(w.getnframes())
    except (wave.Error, EOFError) as e:
        raise AudioInvalido("El archivo no es un WAV válido") from e
    muestras = np.frombuffer(pcm, dtype="<i2").astype(np.float32) / 32768.0
    if muestras.size == 0:
        raise AudioInvalido("El audio está vacío")
    return muestras


def a_pcm16(muestras: np.ndarray) -> bytes:
    return (np.clip(muestras, -1.0, 32767 / 32768) * 32768.0).astype("<i2").tobytes()
