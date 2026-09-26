"""Reconocimiento del contenido hablado con Vosk y gramática restringida (D-036)."""
from __future__ import annotations

import json
from pathlib import Path

import numpy as np

from .audio import FRECUENCIA, a_pcm16
from .motores import Transcripcion


class ReconocedorVosk:
    nombre = "vosk-model-small-es-0.42"

    def __init__(self, ruta_modelo: Path, vocabulario: list[str]):
        from vosk import Model, SetLogLevel

        SetLogLevel(-1)
        self._modelo = Model(str(ruta_modelo))
        self._gramatica = json.dumps(vocabulario, ensure_ascii=False)

    def transcribir(self, muestras: np.ndarray) -> Transcripcion:
        from vosk import KaldiRecognizer

        rec = KaldiRecognizer(self._modelo, FRECUENCIA, self._gramatica)
        rec.SetWords(True)
        rec.AcceptWaveform(a_pcm16(muestras))
        resultado = json.loads(rec.FinalResult())
        palabras = resultado.get("result", [])
        # La confianza de la frase es la de su palabra menos segura
        confianza = min((p["conf"] for p in palabras), default=None)
        return Transcripcion(texto=resultado.get("text", "").strip(), confianza=confianza)


def coincide(transcripcion: str, esperado: str) -> bool:
    return transcripcion.split() == esperado.split()
