"""biometria.perfiles_voz (modelo de datos v4 §2.12, B-1 a B-13): cifrado, AAD, clave_version y ciclo de vida.

Las pruebas de PostgreSQL corren solo si existe NAYRA_VOZ_TEST_BD (cadena de conexión a una base EXCLUSIVA de pruebas:
se borra el esquema biometria y se aplican las migraciones de migraciones/biometria).

Los embeddings son dobles de prueba de 192 dimensiones: el extractor ECAPA real no se ejecutó en este entorno, así que
estas pruebas no confirman la dimensión que produce el modelo real.
"""
import dataclasses
import os
import uuid
from pathlib import Path

import numpy as np
import pytest
from cryptography.exceptions import InvalidTag

from nayra_voz.almacen import (
    AlmacenPerfilesEnMemoria,
    AlmacenPerfilesPostgres,
    CifradorEmbeddings,
    DIMENSION_EMBEDDING,
    cifrador_desde_entorno,
)
from nayra_voz.resultados import Motivo
from tests.falsos import vector
from tests.test_almacen_pipeline import DESAFIO, VOZ, construir, enrolar

MODELO = "speechbrain/spkrec-ecapa-voxceleb"
VERSION = "falso-1"
MIGRACIONES = Path(__file__).resolve().parent.parent / "migraciones" / "biometria"


def nuevo_usuario() -> str:
    return str(uuid.uuid4())


# --- Cifrado (B-1 a B-5) ---

def test_serializacion_float32_little_endian_y_784_bytes_con_192_dimensiones():
    cifrador = CifradorEmbeddings(os.urandom(32))
    e = vector(1)
    assert CifradorEmbeddings.serializar(e) == np.asarray(e, dtype="<f4").tobytes()
    assert len(CifradorEmbeddings.serializar(e)) == 768
    perfil = cifrador.cifrar(nuevo_usuario(), e, MODELO, VERSION, 3)
    # cifrado ‖ tag GCM de 16 bytes (B-3)
    assert len(perfil.embedding_cifrado) == DIMENSION_EMBEDDING * 4 + 16 == 784
    np.testing.assert_array_equal(cifrador.descifrar(perfil), e)


def test_dimension_distinta_de_192_se_rechaza():
    cifrador = CifradorEmbeddings(os.urandom(32))
    for malo in (np.ones(191, dtype=np.float32), np.ones((1, 192), dtype=np.float32), np.full(192, np.nan, dtype=np.float32)):
        with pytest.raises(ValueError):
            cifrador.cifrar(nuevo_usuario(), malo, MODELO, VERSION, 3)


def test_iv_de_12_bytes_nuevo_en_cada_cifrado():
    cifrador = CifradorEmbeddings(os.urandom(32))
    u = nuevo_usuario()
    perfiles = [cifrador.cifrar(u, vector(2), MODELO, VERSION, 3) for _ in range(50)]
    assert all(len(p.iv) == 12 for p in perfiles)
    assert len({p.iv for p in perfiles}) == 50
    assert len({p.id for p in perfiles}) == 50
    assert len({p.embedding_cifrado for p in perfiles}) == 50


def test_tag_gcm_detecta_cualquier_alteracion():
    cifrador = CifradorEmbeddings(os.urandom(32))
    perfil = cifrador.cifrar(nuevo_usuario(), vector(3), MODELO, VERSION, 3)
    alterado = bytearray(perfil.embedding_cifrado)
    alterado[-1] ^= 0x01  # último byte del tag
    with pytest.raises(InvalidTag):
        cifrador.descifrar(dataclasses.replace(perfil, embedding_cifrado=bytes(alterado)))
    with pytest.raises(InvalidTag):
        cifrador.descifrar(dataclasses.replace(perfil, embedding_cifrado=perfil.embedding_cifrado[:-16]))


def test_aad_liga_id_usuario_modelo_y_version():
    cifrador = CifradorEmbeddings(os.urandom(32))
    perfil = cifrador.cifrar(nuevo_usuario(), vector(4), MODELO, VERSION, 3)
    assert CifradorEmbeddings.datos_asociados(perfil.id, perfil.usuario_id, MODELO, VERSION) == (
        f"nayra-voz:v1|{perfil.id}|{perfil.usuario_id}|{MODELO}|{VERSION}".encode())
    for cambio in ({"id": str(uuid.uuid4())}, {"usuario_id": nuevo_usuario()}, {"modelo": "otro"}, {"version_modelo": "otra"}):
        with pytest.raises(InvalidTag):
            cifrador.descifrar(dataclasses.replace(perfil, **cambio))


def test_usuario_id_debe_ser_uuid():
    with pytest.raises(ValueError):
        CifradorEmbeddings(os.urandom(32)).cifrar("u1", vector(1), MODELO, VERSION, 3)


def test_clave_version_y_rotacion():
    v1, v2 = os.urandom(32), os.urandom(32)
    antiguo = CifradorEmbeddings(v1, 1)
    perfil = antiguo.cifrar(nuevo_usuario(), vector(5), MODELO, VERSION, 3)
    assert perfil.clave_version == 1
    nuevo = CifradorEmbeddings(v2, 2, {1: v1})
    np.testing.assert_array_equal(nuevo.descifrar(perfil), vector(5))
    rotado = nuevo.recifrar(perfil)
    assert rotado.clave_version == 2 and rotado.id == perfil.id and rotado.iv != perfil.iv
    np.testing.assert_array_equal(CifradorEmbeddings(v2, 2).descifrar(rotado), vector(5))
    with pytest.raises(KeyError):
        CifradorEmbeddings(v2, 2).descifrar(perfil)
    with pytest.raises(ValueError):
        CifradorEmbeddings(v1, 0)


def test_claves_desde_el_entorno(monkeypatch):
    import base64
    v1, v2 = os.urandom(32), os.urandom(32)
    monkeypatch.setenv("NAYRA_VOZ_CLAVE_EMBEDDINGS", base64.b64encode(v2).decode())
    monkeypatch.setenv("NAYRA_VOZ_CLAVE_EMBEDDINGS_VERSION", "2")
    monkeypatch.setenv("NAYRA_VOZ_CLAVE_EMBEDDINGS_V1", base64.b64encode(v1).decode())
    cifrador = cifrador_desde_entorno()
    assert cifrador.clave_version == 2
    perfil = CifradorEmbeddings(v1, 1).cifrar(nuevo_usuario(), vector(6), MODELO, VERSION, 3)
    np.testing.assert_array_equal(cifrador.descifrar(perfil), vector(6))


# --- Ciclo de vida con el pipeline (B-7, B-8, B-9, B-13), en memoria ---

def test_reenrolar_reemplaza_el_perfil_con_id_iv_y_cifrado_nuevos():
    pipeline, almacen, _ = construir()
    u = nuevo_usuario()
    enrolar(pipeline, u)
    anterior = almacen.obtener(u)
    enrolar(pipeline, u)
    nuevo = almacen.obtener(u)
    assert nuevo.id != anterior.id and nuevo.iv != anterior.iv and nuevo.embedding_cifrado != anterior.embedding_cifrado
    assert nuevo.estado == "ACTIVO"


def test_revocar_y_eliminar():
    pipeline, almacen, _ = construir()
    u = nuevo_usuario()
    enrolar(pipeline, u)
    assert almacen.revocar(u)
    revocado = almacen.obtener(u)
    assert revocado.estado == "REVOCADO" and revocado.fecha_revocacion is not None
    assert not almacen.revocar(u)
    assert pipeline.verificar(u, VOZ, DESAFIO).motivo == Motivo.SIN_REFERENCIA
    assert almacen.eliminar(u)
    assert almacen.obtener(u) is None
    assert not almacen.eliminar(u)


def test_cambio_de_version_del_modelo_exige_reenrolar():
    pipeline, _, extractor = construir()
    u = nuevo_usuario()
    enrolar(pipeline, u)
    extractor.version_modelo = "falso-2"
    assert pipeline.verificar(u, VOZ, DESAFIO).motivo == Motivo.SIN_REFERENCIA


# --- PostgreSQL ---

@pytest.fixture(scope="module")
def bd():
    conexion = os.environ.get("NAYRA_VOZ_TEST_BD")
    if not conexion:
        pytest.skip("Requiere PostgreSQL: definir NAYRA_VOZ_TEST_BD (base exclusiva de pruebas)")
    import psycopg
    with psycopg.connect(conexion, autocommit=True) as con:
        con.execute("DROP SCHEMA IF EXISTS biometria CASCADE")
        for archivo in sorted(MIGRACIONES.glob("V*.sql")):
            con.execute(archivo.read_text(encoding="utf-8").replace("${rol_biometria}", "nayra_biometria"))
    return conexion


def perfil_nuevo(u: str | None = None, cifrador=None):
    cifrador = cifrador or CifradorEmbeddings(os.urandom(32))
    return cifrador, cifrador.cifrar(u or nuevo_usuario(), vector(7), MODELO, VERSION, 3)


def test_postgres_guarda_lee_y_descifra(bd):
    almacen = AlmacenPerfilesPostgres(bd)
    cifrador, perfil = perfil_nuevo()
    almacen.guardar(perfil)
    leido = almacen.obtener(perfil.usuario_id)
    assert leido.id == perfil.id and leido.iv == perfil.iv and leido.embedding_cifrado == perfil.embedding_cifrado
    assert leido.clave_version == 1 and leido.numero_muestras == 3 and leido.estado == "ACTIVO"
    assert leido.fecha_revocacion is None
    np.testing.assert_array_equal(cifrador.descifrar(leido), vector(7))
    assert almacen.obtener("no-es-uuid") is None


def test_postgres_reenrolar_borra_el_anterior_en_la_misma_transaccion(bd):
    import psycopg
    almacen = AlmacenPerfilesPostgres(bd)
    cifrador, primero = perfil_nuevo()
    almacen.guardar(primero)
    _, segundo = perfil_nuevo(primero.usuario_id, cifrador)
    almacen.guardar(segundo)
    with psycopg.connect(bd) as con:
        filas = con.execute("SELECT id::text FROM biometria.perfiles_voz WHERE usuario_id = %s", (primero.usuario_id,)).fetchall()
    assert filas == [(segundo.id,)]
    # Un embedding antiguo copiado en la fila nueva no se descifra: la AAD incluye el id del perfil.
    with pytest.raises(InvalidTag):
        cifrador.descifrar(dataclasses.replace(almacen.obtener(primero.usuario_id), embedding_cifrado=primero.embedding_cifrado,
                                               iv=primero.iv))


def test_postgres_revocar_y_eliminar(bd):
    almacen = AlmacenPerfilesPostgres(bd)
    _, perfil = perfil_nuevo()
    almacen.guardar(perfil)
    assert almacen.revocar(perfil.usuario_id)
    revocado = almacen.obtener(perfil.usuario_id)
    assert revocado.estado == "REVOCADO" and revocado.fecha_revocacion is not None
    assert not almacen.revocar(perfil.usuario_id)
    assert almacen.eliminar(perfil.usuario_id)
    assert almacen.obtener(perfil.usuario_id) is None


def test_postgres_restricciones(bd):
    import psycopg
    _, perfil = perfil_nuevo()
    sql = ("INSERT INTO biometria.perfiles_voz (id, usuario_id, embedding_cifrado, iv, clave_version, modelo, version_modelo, "
           "numero_muestras, estado, fecha_creacion, fecha_actualizacion, fecha_revocacion) "
           "VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, now(), now(), %s)")

    def fila(**cambios):
        base = dict(id=str(uuid.uuid4()), usuario_id=nuevo_usuario(), embedding_cifrado=perfil.embedding_cifrado, iv=perfil.iv,
                    clave_version=1, modelo=MODELO, version_modelo=VERSION, numero_muestras=3, estado="ACTIVO", fecha_revocacion=None)
        base.update(cambios)
        return tuple(base.values())

    with psycopg.connect(bd, autocommit=True) as con:
        con.execute(sql, fila())
        for cambios in ({"iv": b"\x00" * 11}, {"iv": b"\x00" * 16}, {"clave_version": 0}, {"numero_muestras": 0},
                        {"estado": "PENDIENTE"}, {"estado": "REVOCADO"}, {"fecha_revocacion": perfil.fecha_creacion}):
            with pytest.raises(psycopg.errors.CheckViolation):
                con.execute(sql, fila(**cambios))
        u = nuevo_usuario()
        con.execute(sql, fila(usuario_id=u))
        with pytest.raises(psycopg.errors.UniqueViolation):
            con.execute(sql, fila(usuario_id=u))
        # Sin FK física hacia nayra.usuarios (B-6): cualquier uuid se admite.
        fks = con.execute("SELECT count(*) FROM information_schema.table_constraints WHERE table_schema = 'biometria' "
                          "AND constraint_type = 'FOREIGN KEY'").fetchone()[0]
        assert fks == 0


def test_postgres_no_guarda_audio(bd):
    import psycopg
    with psycopg.connect(bd) as con:
        columnas = con.execute("SELECT column_name, data_type FROM information_schema.columns WHERE table_schema = 'biometria' "
                               "AND table_name = 'perfiles_voz' ORDER BY ordinal_position").fetchall()
    assert [c for c, _ in columnas] == ["id", "usuario_id", "embedding_cifrado", "iv", "clave_version", "modelo", "version_modelo",
                                        "numero_muestras", "estado", "fecha_creacion", "fecha_actualizacion", "fecha_revocacion"]
    # Los únicos binarios son el embedding cifrado (784 bytes con 192 dimensiones) y el IV.
    assert [c for c, t in columnas if t == "bytea"] == ["embedding_cifrado", "iv"]


def test_pipeline_con_postgres(bd):
    pipeline, _, _ = construir()
    pipeline._almacen = AlmacenPerfilesPostgres(bd)
    u = nuevo_usuario()
    assert enrolar(pipeline, u)[0]
    assert pipeline.verificar(u, VOZ, DESAFIO).aprobado
    pipeline._almacen.revocar(u)
    assert pipeline.verificar(u, VOZ, DESAFIO).motivo == Motivo.SIN_REFERENCIA
