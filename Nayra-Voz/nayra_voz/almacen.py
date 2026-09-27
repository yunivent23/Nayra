"""Referencia biométrica cifrada (D-013).

Solo se guarda el embedding de referencia (centroide) cifrado con AES-256-GCM,
con el nombre y la versión del modelo. El audio nunca se guarda.

PROVISIONAL:
- La tabla física `biometria.PERFILES_VOZ` depende de la estrategia de migraciones
  (D-051, pendiente). Mientras tanto se usa `AlmacenPerfilesEnMemoria`, que pierde
  los perfiles al reiniciar el servicio.
- La custodia de la clave depende de D-017 (pendiente). La clave se lee de la
  variable de entorno NAYRA_VOZ_CLAVE_EMBEDDINGS (32 bytes en base64); nunca está
  en el código ni en la base de datos.
"""
from __future__ import annotations

import base64
import os
import threading
from dataclasses import dataclass
from datetime import datetime, timezone
from typing import Protocol

import numpy as np
from cryptography.hazmat.primitives.ciphers.aead import AESGCM

VARIABLE_CLAVE = "NAYRA_VOZ_CLAVE_EMBEDDINGS"


@dataclass(frozen=True)
class PerfilCifrado:
    usuario_id: str
    nonce: bytes
    cifrado: bytes
    modelo: str
    version_parametros: str
    creado_en: datetime


class AlmacenPerfiles(Protocol):
    def guardar(self, perfil: PerfilCifrado) -> None: ...
    def obtener(self, usuario_id: str) -> PerfilCifrado | None: ...
    def eliminar(self, usuario_id: str) -> bool: ...


class AlmacenPerfilesEnMemoria:
    """PROVISIONAL — adaptador temporal hasta la migración de biometria.PERFILES_VOZ (D-051)."""

    def __init__(self) -> None:
        self._perfiles: dict[str, PerfilCifrado] = {}
        self._candado = threading.Lock()

    def guardar(self, perfil: PerfilCifrado) -> None:
        with self._candado:
            self._perfiles[perfil.usuario_id] = perfil

    def obtener(self, usuario_id: str) -> PerfilCifrado | None:
        with self._candado:
            return self._perfiles.get(usuario_id)

    def eliminar(self, usuario_id: str) -> bool:
        with self._candado:
            return self._perfiles.pop(usuario_id, None) is not None


def clave_desde_entorno() -> bytes:
    valor = os.environ.get(VARIABLE_CLAVE)
    if not valor:
        raise RuntimeError(f"Falta la variable de entorno {VARIABLE_CLAVE} (clave AES-256 en base64).")
    clave = base64.b64decode(valor)
    if len(clave) != 32:
        raise RuntimeError(f"{VARIABLE_CLAVE} debe contener exactamente 32 bytes.")
    return clave


class CifradorEmbeddings:
    def __init__(self, clave: bytes):
        self._aes = AESGCM(clave)

    @staticmethod
    def _datos_asociados(usuario_id: str, modelo: str) -> bytes:
        # Liga el cifrado al usuario y al modelo: un perfil no se puede mover a otra cuenta.
        return f"{usuario_id}|{modelo}".encode("utf-8")

    def cifrar(self, usuario_id: str, embedding: np.ndarray, modelo: str, version_parametros: str) -> PerfilCifrado:
        nonce = os.urandom(12)
        datos = np.asarray(embedding, dtype="<f4").tobytes()
        cifrado = self._aes.encrypt(nonce, datos, self._datos_asociados(usuario_id, modelo))
        return PerfilCifrado(usuario_id, nonce, cifrado, modelo, version_parametros, datetime.now(timezone.utc))

    def descifrar(self, perfil: PerfilCifrado) -> np.ndarray:
        datos = self._aes.decrypt(perfil.nonce, perfil.cifrado, self._datos_asociados(perfil.usuario_id, perfil.modelo))
        return np.frombuffer(datos, dtype="<f4").copy()
