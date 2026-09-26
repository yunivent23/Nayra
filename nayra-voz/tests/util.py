import io
import wave

import numpy as np


def wav(muestras: np.ndarray, frecuencia: int = 16000, canales: int = 1, ancho: int = 2) -> bytes:
    buf = io.BytesIO()
    with wave.open(buf, "wb") as w:
        w.setnchannels(canales)
        w.setsampwidth(ancho)
        w.setframerate(frecuencia)
        if ancho == 2:
            datos = (np.clip(muestras, -1, 32767 / 32768) * 32768).astype("<i2")
            if canales == 2:
                datos = np.repeat(datos, 2)
            w.writeframes(datos.tobytes())
        else:
            w.writeframes((np.clip(muestras, -1, 1) * 127 + 128).astype(np.uint8).tobytes())
    return buf.getvalue()


def ruido(segundos: float = 2.0, amplitud: float = 0.1, semilla: int = 0) -> np.ndarray:
    return (np.random.default_rng(semilla).standard_normal(int(16000 * segundos)) * amplitud).astype(np.float32)
