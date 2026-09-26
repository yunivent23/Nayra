"""Vocabulario cerrado del desafío (D-037), compartido con el backend Java."""
from __future__ import annotations

from pathlib import Path

DIGITOS = ["cero", "uno", "dos", "tres", "cuatro", "cinco", "seis", "siete", "ocho", "nueve"]


def cargar_palabras(ruta: Path) -> list[str]:
    palabras = [l.strip() for l in ruta.read_text(encoding="utf-8").splitlines()
                if l.strip() and not l.lstrip().startswith("#")]
    if len(palabras) < 2:
        raise ValueError("La lista de palabras del desafío está vacía")
    return palabras


def gramatica(palabras: list[str]) -> list[str]:
    """Gramática de Vosk: solo palabras del desafío y dígitos; lo demás se reconoce como [unk]."""
    return sorted(set(palabras) | set(DIGITOS)) + ["[unk]"]
