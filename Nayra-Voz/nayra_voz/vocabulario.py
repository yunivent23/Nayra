"""Vocabulario versionado del desafío (D-054), compartido con Spring Boot."""
from __future__ import annotations

import json
import os
from dataclasses import dataclass
from pathlib import Path

from .config import RUTA_VOCABULARIO_POR_DEFECTO


@dataclass(frozen=True)
class Vocabulario:
    version: str
    palabras: tuple[str, ...]
    digitos: tuple[str, ...]

    def terminos(self) -> list[str]:
        """Todos los términos que la gramática cerrada de Vosk puede reconocer."""
        return list(self.palabras) + list(self.digitos)


def cargar_vocabulario(ruta: str | os.PathLike | None = None) -> Vocabulario:
    ruta = Path(ruta or os.environ.get("NAYRA_VOZ_VOCABULARIO", RUTA_VOCABULARIO_POR_DEFECTO))
    datos = json.loads(ruta.read_text(encoding="utf-8"))
    return Vocabulario(
        version=str(datos["version"]),
        palabras=tuple(datos["palabras"]),
        digitos=tuple(datos["digitos"]),
    )
