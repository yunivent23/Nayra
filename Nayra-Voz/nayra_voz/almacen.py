"""Referencia biométrica cifrada: biometria.perfiles_voz (D-013; modelo de datos v4 §2.12, B-1 a B-13).

Solo se guarda el embedding de referencia (centroide) cifrado con AES-256-GCM. El audio nunca se guarda.

- B-1: texto plano = 192 × float32 little-endian del centroide normalizado L2 (768 bytes).
- B-2: IV de 12 bytes de un CSPRNG, nuevo en cada cifrado.
- B-3: el tag GCM (16 bytes) va unido al texto cifrado en embedding_cifrado (784 bytes con 192 dimensiones).
- B-4 (PENDIENTE NO BLOQUEANTE): AAD "nayra-voz:v1|{id}|{usuario_id}|{modelo}|{version_modelo}", reconstruida desde
  las columnas de la fila.
- B-5: la clave nunca está en PostgreSQL, en el código ni en el repositorio; se guarda solo clave_version.
  PROVISIONAL: la clave vigente se lee de NAYRA_VOZ_CLAVE_EMBEDDINGS (32 bytes en base64) y su versión de
  NAYRA_VOZ_CLAVE_EMBEDDINGS_VERSION (1 si no se define). Las claves anteriores, necesarias durante una rotación,
  de NAYRA_VOZ_CLAVE_EMBEDDINGS_V<n>. Proveedor definitivo: D-016/D-017. Frecuencia de rotación: PENDIENTE.
- B-7: un único perfil por usuario. B-8: estados ACTIVO y REVOCADO.
- B-13: revocar = REVOCADO + fecha_revocacion; eliminar = borrado físico; volver a enrolar = en una transacción se
  borra el perfil anterior y se inserta el nuevo (id, IV y AAD nuevos). El embedding anterior no se conserva.
"""
from __future__ import annotations

import base64
import os
import re
import threading
import uuid
from dataclasses import dataclass, replace
from datetime import datetime, timezone
from typing import Protocol

import numpy as np
from cryptography.hazmat.primitives.ciphers.aead import AESGCM

VARIABLE_CLAVE = "NAYRA_VOZ_CLAVE_EMBEDDINGS"
VARIABLE_VERSION_CLAVE = "NAYRA_VOZ_CLAVE_EMBEDDINGS_VERSION"
PREFIJO_CLAVE_ANTERIOR = "NAYRA_VOZ_CLAVE_EMBEDDINGS_V"
VARIABLE_BD = "NAYRA_VOZ_BD"

FORMATO_AAD = "nayra-voz:v1"
DIMENSION_EMBEDDING = 192          # B-1 (ECAPA-TDNN, D-011)
BYTES_IV = 12                      # B-2
ACTIVO = "ACTIVO"
REVOCADO = "REVOCADO"


def _ahora() -> datetime:
    return datetime.now(timezone.utc)


def uuid_canonico(valor: str) -> str:
    """usuario_id e id son uuid (B-6). Lanza ValueError si el valor no es un UUID."""
    return str(uuid.UUID(str(valor)))


@dataclass(frozen=True)
class PerfilVoz:
    """Fila de biometria.perfiles_voz."""

    id: str
    usuario_id: str
    embedding_cifrado: bytes
    iv: bytes
    clave_version: int
    modelo: str
    version_modelo: str
    numero_muestras: int
    estado: str
    fecha_creacion: datetime
    fecha_actualizacion: datetime
    fecha_revocacion: datetime | None = None

    @property
    def activo(self) -> bool:
        return self.estado == ACTIVO


class AlmacenPerfiles(Protocol):
    def guardar(self, perfil: PerfilVoz) -> None:
        """Enrolamiento o nuevo enrolamiento: borra el perfil anterior del usuario e inserta el nuevo (B-13)."""

    def obtener(self, usuario_id: str) -> PerfilVoz | None: ...

    def revocar(self, usuario_id: str) -> bool: ...

    def eliminar(self, usuario_id: str) -> bool: ...


class AlmacenPerfilesEnMemoria:
    """Adaptador para pruebas y desarrollo sin base de datos: pierde los perfiles al reiniciar."""

    def __init__(self) -> None:
        self._perfiles: dict[str, PerfilVoz] = {}
        self._candado = threading.Lock()

    def guardar(self, perfil: PerfilVoz) -> None:
        with self._candado:
            self._perfiles[perfil.usuario_id] = perfil

    def obtener(self, usuario_id: str) -> PerfilVoz | None:
        with self._candado:
            return self._perfiles.get(str(usuario_id))

    def revocar(self, usuario_id: str) -> bool:
        with self._candado:
            perfil = self._perfiles.get(str(usuario_id))
            if perfil is None or not perfil.activo:
                return False
            ahora = _ahora()
            self._perfiles[perfil.usuario_id] = replace(perfil, estado=REVOCADO, fecha_revocacion=ahora, fecha_actualizacion=ahora)
            return True

    def eliminar(self, usuario_id: str) -> bool:
        with self._candado:
            return self._perfiles.pop(str(usuario_id), None) is not None


_COLUMNAS = ("id, usuario_id, embedding_cifrado, iv, clave_version, modelo, version_modelo, numero_muestras, estado, "
             "fecha_creacion, fecha_actualizacion, fecha_revocacion")


class AlmacenPerfilesPostgres:
    """biometria.perfiles_voz en el PostgreSQL del proyecto, con el usuario exclusivo del servicio biométrico (D-013).

    La cadena de conexión llega por NAYRA_VOZ_BD (D-017: nunca en el repositorio). Sin FK física hacia nayra.usuarios (B-6).
    """

    def __init__(self, conexion: str):
        import psycopg  # solo lo necesita este adaptador

        self._psycopg = psycopg
        self._conexion = conexion

    def _conectar(self):
        return self._psycopg.connect(self._conexion)

    @staticmethod
    def _perfil(fila) -> PerfilVoz:
        return PerfilVoz(str(fila[0]), str(fila[1]), bytes(fila[2]), bytes(fila[3]), fila[4], fila[5], fila[6], fila[7], fila[8],
                         fila[9], fila[10], fila[11])

    def guardar(self, perfil: PerfilVoz) -> None:
        with self._conectar() as con, con.transaction():
            con.execute("DELETE FROM biometria.perfiles_voz WHERE usuario_id = %s", (perfil.usuario_id,))
            con.execute(
                f"INSERT INTO biometria.perfiles_voz ({_COLUMNAS}) VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)",
                (perfil.id, perfil.usuario_id, perfil.embedding_cifrado, perfil.iv, perfil.clave_version, perfil.modelo,
                 perfil.version_modelo, perfil.numero_muestras, perfil.estado, perfil.fecha_creacion,
                 perfil.fecha_actualizacion, perfil.fecha_revocacion),
            )

    def obtener(self, usuario_id: str) -> PerfilVoz | None:
        try:
            usuario = uuid_canonico(usuario_id)
        except ValueError:
            return None
        with self._conectar() as con:
            fila = con.execute(f"SELECT {_COLUMNAS} FROM biometria.perfiles_voz WHERE usuario_id = %s", (usuario,)).fetchone()
        return None if fila is None else self._perfil(fila)

    def revocar(self, usuario_id: str) -> bool:
        with self._conectar() as con:
            cursor = con.execute(
                "UPDATE biometria.perfiles_voz SET estado = %s, fecha_revocacion = now(), fecha_actualizacion = now() "
                "WHERE usuario_id = %s AND estado = %s", (REVOCADO, uuid_canonico(usuario_id), ACTIVO))
            return cursor.rowcount == 1

    def eliminar(self, usuario_id: str) -> bool:
        with self._conectar() as con:
            return con.execute("DELETE FROM biometria.perfiles_voz WHERE usuario_id = %s",
                               (uuid_canonico(usuario_id),)).rowcount == 1


def _decodificar_clave(valor: str, variable: str) -> bytes:
    clave = base64.b64decode(valor)
    if len(clave) != 32:
        raise RuntimeError(f"{variable} debe contener exactamente 32 bytes.")
    return clave


def clave_desde_entorno() -> bytes:
    valor = os.environ.get(VARIABLE_CLAVE)
    if not valor:
        raise RuntimeError(f"Falta la variable de entorno {VARIABLE_CLAVE} (clave AES-256 en base64).")
    return _decodificar_clave(valor, VARIABLE_CLAVE)


def cifrador_desde_entorno() -> "CifradorEmbeddings":
    """Clave vigente, su versión y las anteriores (PROVISIONAL, B-5)."""
    version = int(os.environ.get(VARIABLE_VERSION_CLAVE, "1"))
    anteriores = {}
    for nombre, valor in os.environ.items():
        m = re.fullmatch(re.escape(PREFIJO_CLAVE_ANTERIOR) + r"(\d+)", nombre)
        if m and valor:
            anteriores[int(m.group(1))] = _decodificar_clave(valor, nombre)
    return CifradorEmbeddings(clave_desde_entorno(), version, anteriores)


def almacen_desde_entorno():
    """PostgreSQL si NAYRA_VOZ_BD está definida; si no, memoria (solo desarrollo: se pierde al reiniciar)."""
    conexion = os.environ.get(VARIABLE_BD)
    return AlmacenPerfilesPostgres(conexion) if conexion else AlmacenPerfilesEnMemoria()


class CifradorEmbeddings:
    def __init__(self, clave: bytes, clave_version: int = 1, anteriores: dict[int, bytes] | None = None):
        if clave_version <= 0:
            raise ValueError("clave_version debe ser mayor que 0.")
        self._version = clave_version
        self._claves = {**(anteriores or {}), clave_version: clave}

    @property
    def clave_version(self) -> int:
        return self._version

    @staticmethod
    def datos_asociados(perfil_id: str, usuario_id: str, modelo: str, version_modelo: str) -> bytes:
        # B-4: liga el cifrado a la fila (id), al usuario, al modelo y a su versión.
        return f"{FORMATO_AAD}|{perfil_id}|{usuario_id}|{modelo}|{version_modelo}".encode("utf-8")

    @staticmethod
    def serializar(embedding: np.ndarray) -> bytes:
        """B-1: vector de 192 dimensiones en float32 little-endian (768 bytes)."""
        vector = np.asarray(embedding)
        if vector.ndim != 1 or vector.shape[0] != DIMENSION_EMBEDDING:
            raise ValueError(f"El embedding debe tener {DIMENSION_EMBEDDING} dimensiones; tiene forma {vector.shape}.")
        if not np.all(np.isfinite(vector)):
            raise ValueError("El embedding contiene valores no finitos.")
        return vector.astype("<f4").tobytes()

    def _cifrar(self, perfil_id: str, usuario_id: str, datos: bytes, modelo: str, version_modelo: str) -> tuple[bytes, bytes]:
        iv = os.urandom(BYTES_IV)
        aad = self.datos_asociados(perfil_id, usuario_id, modelo, version_modelo)
        return iv, AESGCM(self._claves[self._version]).encrypt(iv, datos, aad)

    def cifrar(self, usuario_id: str, embedding: np.ndarray, modelo: str, version_modelo: str,
               numero_muestras: int) -> PerfilVoz:
        """Perfil nuevo: id nuevo, IV nuevo y AAD nueva en cada enrolamiento (B-13)."""
        if numero_muestras <= 0:
            raise ValueError("numero_muestras debe ser mayor que 0.")
        usuario = uuid_canonico(usuario_id)
        perfil_id = str(uuid.uuid4())
        iv, cifrado = self._cifrar(perfil_id, usuario, self.serializar(embedding), modelo, version_modelo)
        ahora = _ahora()
        return PerfilVoz(perfil_id, usuario, cifrado, iv, self._version, modelo, version_modelo, numero_muestras, ACTIVO,
                         ahora, ahora)

    def descifrar(self, perfil: PerfilVoz) -> np.ndarray:
        clave = self._claves.get(perfil.clave_version)
        if clave is None:
            raise KeyError(f"No hay clave para clave_version={perfil.clave_version}.")
        aad = self.datos_asociados(perfil.id, perfil.usuario_id, perfil.modelo, perfil.version_modelo)
        datos = AESGCM(clave).decrypt(perfil.iv, perfil.embedding_cifrado, aad)
        embedding = np.frombuffer(datos, dtype="<f4").copy()
        if embedding.shape[0] != DIMENSION_EMBEDDING:
            raise ValueError(f"Embedding descifrado de {embedding.shape[0]} dimensiones.")
        return embedding

    def recifrar(self, perfil: PerfilVoz) -> PerfilVoz:
        """Rotación de clave (B-5): mismo perfil, cifrado con la clave vigente y un IV nuevo."""
        datos = self.serializar(self.descifrar(perfil))
        iv, cifrado = self._cifrar(perfil.id, perfil.usuario_id, datos, perfil.modelo, perfil.version_modelo)
        return replace(perfil, embedding_cifrado=cifrado, iv=iv, clave_version=self._version, fecha_actualizacion=_ahora())
