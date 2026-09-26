"""Cifrado AES-256-GCM del embedding de referencia (D-039)."""
from __future__ import annotations

import os

import numpy as np
from cryptography.hazmat.primitives.ciphers.aead import AESGCM


class CifradoEmbedding:
    def __init__(self, clave: bytes):
        self._aes = AESGCM(clave)

    @staticmethod
    def _contexto(usuario_id: int, modelo_version: str) -> bytes:
        # Datos asociados: el texto cifrado solo es válido para ese usuario y esa versión de modelo
        return f"nayra|perfil_voz|{usuario_id}|{modelo_version}".encode()

    def cifrar(self, embedding: np.ndarray, usuario_id: int, modelo_version: str) -> tuple[bytes, bytes]:
        iv = os.urandom(12)
        datos = embedding.astype("<f4").tobytes()
        return self._aes.encrypt(iv, datos, self._contexto(usuario_id, modelo_version)), iv

    def descifrar(self, cifrado: bytes, iv: bytes, usuario_id: int, modelo_version: str) -> np.ndarray:
        datos = self._aes.decrypt(iv, cifrado, self._contexto(usuario_id, modelo_version))
        return np.frombuffer(datos, dtype="<f4").copy()
