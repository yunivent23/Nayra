"""Configuración del servicio de voz, solo por variables de entorno (D-017)."""
from __future__ import annotations

import base64
import os
from dataclasses import dataclass
from pathlib import Path

RAIZ = Path(__file__).resolve().parent


class ConfiguracionInvalida(RuntimeError):
    pass


def _obligatoria(nombre: str) -> str:
    valor = os.environ.get(nombre, "").strip()
    if not valor:
        raise ConfiguracionInvalida(f"Falta la variable de entorno obligatoria {nombre}")
    return valor


@dataclass(frozen=True)
class Config:
    token_servicio: str
    clave_cifrado: bytes
    db_dsn: str
    modelos_dir: Path
    vocabulario: Path
    vad_agresividad: int

    @staticmethod
    def desde_entorno() -> "Config":
        clave = base64.b64decode(_obligatoria("NAYRA_VOZ_CLAVE_CIFRADO"))
        if len(clave) != 32:
            raise ConfiguracionInvalida("NAYRA_VOZ_CLAVE_CIFRADO debe ser una clave AES-256 (32 bytes en base64)")
        return Config(
            token_servicio=_obligatoria("NAYRA_VOZ_TOKEN"),
            clave_cifrado=clave,
            db_dsn=_obligatoria("NAYRA_VOZ_DB_DSN"),
            modelos_dir=Path(os.environ.get("NAYRA_VOZ_MODELOS_DIR", str(RAIZ.parent / "modelos"))),
            vocabulario=Path(os.environ.get("NAYRA_VOZ_VOCABULARIO", str(RAIZ / "challenge-words.es.txt"))),
            # Agresividad del detector de voz WebRTC (0-3); parámetro del algoritmo, no umbral de decisión
            vad_agresividad=int(os.environ.get("NAYRA_VOZ_VAD_AGRESIVIDAD", "2")),
        )
