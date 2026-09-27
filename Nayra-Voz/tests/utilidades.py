"""Audio sintético para pruebas. No son voces reales ni datos experimentales."""
from __future__ import annotations

import io
import wave

import numpy as np

FS = 16000


def a_wav(muestras: np.ndarray, fs: int = FS, canales: int = 1, ancho: int = 2) -> bytes:
    enteros = np.clip(muestras * 32767, -32768, 32767).astype("<i2")
    buffer = io.BytesIO()
    with wave.open(buffer, "wb") as wav:
        wav.setnchannels(canales)
        wav.setsampwidth(ancho)
        wav.setframerate(fs)
        wav.writeframes(enteros.tobytes())
    return buffer.getvalue()


def voz_sintetica(segundos: float, f0: float = 140.0, amplitud: float = 0.3, semilla: int = 0) -> np.ndarray:
    """Tren de pulsos glotales con armónicos y leve modulación: suficiente para activar el VAD."""
    rng = np.random.default_rng(semilla)
    t = np.arange(int(segundos * FS)) / FS
    f = f0 * (1 + 0.05 * np.sin(2 * np.pi * 3 * t))
    fase = 2 * np.pi * np.cumsum(f) / FS
    senal = sum(np.sin(k * fase) / k for k in range(1, 25))
    envolvente = 0.6 + 0.4 * np.abs(np.sin(2 * np.pi * 4 * t))
    senal = senal * envolvente + 0.01 * rng.standard_normal(len(t))
    return (amplitud * senal / np.max(np.abs(senal))).astype(np.float32)


def silencio(segundos: float, ruido: float = 0.001, semilla: int = 1) -> np.ndarray:
    return (ruido * np.random.default_rng(semilla).standard_normal(int(segundos * FS))).astype(np.float32)


def grabacion(voz_s: float = 2.5, pausa_s: float = 0.5, **kw) -> np.ndarray:
    return np.concatenate([silencio(pausa_s), voz_sintetica(voz_s, **kw), silencio(pausa_s)])
