"""Persistencia del perfil de voz en el esquema propio `biometria` (D-039). Nunca guarda audio."""
from __future__ import annotations

from dataclasses import dataclass
from typing import Protocol


@dataclass(frozen=True)
class PerfilCifrado:
    usuario_id: int
    embedding_cifrado: bytes
    iv: bytes
    modelo: str
    modelo_version: str
    num_muestras: int


class RepositorioPerfiles(Protocol):
    def obtener(self, usuario_id: int) -> PerfilCifrado | None: ...

    def guardar(self, perfil: PerfilCifrado) -> None: ...

    def eliminar(self, usuario_id: int) -> bool: ...


class RepositorioPostgres:
    def __init__(self, dsn: str):
        import psycopg_pool

        self._pool = psycopg_pool.ConnectionPool(dsn, min_size=1, max_size=4, open=True)

    def obtener(self, usuario_id: int) -> PerfilCifrado | None:
        with self._pool.connection() as c:
            fila = c.execute(
                "select usuario_id, embedding_cifrado, iv, modelo, modelo_version, num_muestras "
                "from biometria.perfiles_voz where usuario_id = %s and estado = 'ACTIVO'", (usuario_id,)).fetchone()
        return None if fila is None else PerfilCifrado(fila[0], bytes(fila[1]), bytes(fila[2]), fila[3], fila[4], fila[5])

    def guardar(self, p: PerfilCifrado) -> None:
        with self._pool.connection() as c:
            c.execute(
                "insert into biometria.perfiles_voz (usuario_id, embedding_cifrado, iv, modelo, modelo_version, "
                "num_muestras, estado, fecha_creacion, fecha_actualizacion) "
                "values (%s, %s, %s, %s, %s, %s, 'ACTIVO', now(), now()) "
                "on conflict (usuario_id) do update set embedding_cifrado = excluded.embedding_cifrado, "
                "iv = excluded.iv, modelo = excluded.modelo, modelo_version = excluded.modelo_version, "
                "num_muestras = excluded.num_muestras, estado = 'ACTIVO', fecha_actualizacion = now()",
                (p.usuario_id, p.embedding_cifrado, p.iv, p.modelo, p.modelo_version, p.num_muestras))

    def eliminar(self, usuario_id: int) -> bool:
        with self._pool.connection() as c:
            return c.execute("delete from biometria.perfiles_voz where usuario_id = %s", (usuario_id,)).rowcount > 0
