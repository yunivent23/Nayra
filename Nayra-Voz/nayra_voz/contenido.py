"""Etapa de contenido del desafío (D-046): Vosk con gramática cerrada, en el servidor.

También transcribe el PIN dictado con una gramática de dígitos. Esa transcripción
es PROVISIONAL: la tecnología para el PIN dictado sigue pendiente en D-046. El audio
del PIN nunca entra al pipeline biométrico, y ni el audio ni la transcripción se
registran (05 §26.6, D-061).
"""
from __future__ import annotations

import json
import unicodedata
from typing import Protocol

from .audio import Audio
from .config import ParametrosContenido
from .resultados import Etapa, ResultadoEtapa
from .vocabulario import Vocabulario


def normalizar(texto: str) -> str:
    """Minúsculas y sin tildes, para comparar lo reconocido con lo esperado."""
    descompuesto = unicodedata.normalize("NFD", texto.strip().lower())
    return "".join(c for c in descompuesto if unicodedata.category(c) != "Mn")


def tokens_desafio(texto: str) -> list[str]:
    return [normalizar(t) for t in texto.replace(",", " ").split() if t.strip()]


class Reconocedor(Protocol):
    def reconocer(self, audio: Audio, gramatica: list[str]) -> list[tuple[str, float]]:
        """Devuelve la secuencia de palabras reconocidas con su confianza."""


class ReconocedorVosk:
    def __init__(self, ruta_modelo: str):
        import vosk

        vosk.SetLogLevel(-1)
        self._vosk = vosk
        self._modelo = vosk.Model(ruta_modelo)

    def reconocer(self, audio: Audio, gramatica: list[str]) -> list[tuple[str, float]]:
        reconocedor = self._vosk.KaldiRecognizer(
            self._modelo, audio.frecuencia_hz, json.dumps(gramatica + ["[unk]"], ensure_ascii=False)
        )
        reconocedor.SetWords(True)
        reconocedor.AcceptWaveform(audio.pcm16)
        resultado = json.loads(reconocedor.FinalResult())
        return [(p["word"], float(p.get("conf", 0.0))) for p in resultado.get("result", [])]


class VerificadorContenido:
    def __init__(self, reconocedor: Reconocedor, vocabulario: Vocabulario, parametros: ParametrosContenido):
        self._reconocedor = reconocedor
        self._vocabulario = vocabulario
        self._parametros = parametros

    def evaluar(self, audio: Audio, desafio_esperado: str) -> ResultadoEtapa:
        esperado = tokens_desafio(desafio_esperado)
        reconocido = self._reconocedor.reconocer(audio, self._vocabulario.terminos())
        palabras = [(normalizar(p), c) for p, c in reconocido if p != "[unk]"]
        coincide = [p for p, _ in palabras] == esperado
        confianza_minima = min((c for _, c in palabras), default=0.0)
        umbral = self._parametros.confianza_minima_por_palabra
        return ResultadoEtapa(
            etapa=Etapa.CONTENIDO,
            aprobada=coincide and confianza_minima >= umbral,
            puntaje=round(confianza_minima, 4),
            umbral=umbral,
            detalle={"secuenciaCoincide": coincide},
        )


class TranscriptorPin:
    """PROVISIONAL — tecnología del PIN dictado pendiente (D-046)."""

    def __init__(self, reconocedor: Reconocedor, vocabulario: Vocabulario, parametros: ParametrosContenido):
        self._reconocedor = reconocedor
        self._digitos = list(vocabulario.digitos)
        self._parametros = parametros

    def transcribir(self, audio: Audio) -> str | None:
        """Devuelve los 6 dígitos reconocidos, o None si no se reconocieron con claridad."""
        reconocido = self._reconocedor.reconocer(audio, self._digitos)
        indices = {normalizar(d): str(i) for i, d in enumerate(self._digitos)}
        digitos = []
        for palabra, confianza in reconocido:
            clave = normalizar(palabra)
            if clave not in indices or confianza < self._parametros.confianza_minima_por_palabra:
                return None
            digitos.append(indices[clave])
        return "".join(digitos) if len(digitos) == 6 else None
