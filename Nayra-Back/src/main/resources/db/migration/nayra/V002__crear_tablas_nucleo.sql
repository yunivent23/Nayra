-- D-051 y D-009 (aprobadas el 2026-09-27). Seis tablas del núcleo; nombres físicos en 03 §17.
-- Identificadores: uuid generado por la aplicación (UUID v4). Las FK, UNIQUE, CHECK e índices están en V003.
-- No se crean todavía: sesiones, operaciones, solicitudes_atencion, notificaciones, desafíos/nonces, QR ni roles (D-009, opción A).

CREATE TABLE nayra.entidades_bancarias (
    id     uuid         NOT NULL,
    nombre varchar(100) NOT NULL,
    CONSTRAINT pk_entidades_bancarias PRIMARY KEY (id)
);

CREATE TABLE nayra.registro_identidad_simulado (
    id        uuid         NOT NULL,
    dni       varchar(20)  NOT NULL,
    nombres   varchar(100) NOT NULL,
    apellidos varchar(100) NOT NULL,
    CONSTRAINT pk_registro_identidad_simulado PRIMARY KEY (id)
);

CREATE TABLE nayra.usuarios (
    id                  uuid         NOT NULL,
    dni                 varchar(20)  NOT NULL,
    nombres             varchar(100) NOT NULL,
    apellidos           varchar(100) NOT NULL,
    numero_celular      varchar(20)  NOT NULL,
    pin_hash            text         NOT NULL,
    rol                 varchar(10)  NOT NULL,
    estado              varchar(20)  NOT NULL DEFAULT 'ACTIVA',
    fecha_creacion      timestamptz  NOT NULL,
    fecha_actualizacion timestamptz  NOT NULL,
    intentos_fallidos   smallint     NOT NULL DEFAULT 0,
    CONSTRAINT pk_usuarios PRIMARY KEY (id)
);

COMMENT ON COLUMN nayra.usuarios.pin_hash IS 'Hash autodescriptivo del PIN (D-061). Algoritmo y parámetros PENDIENTES (D-047); el pepper nunca va en la BD.';
COMMENT ON COLUMN nayra.usuarios.intentos_fallidos IS 'PROVISIONAL: contador de intentos hasta cerrar el detalle de D-044.';

CREATE TABLE nayra.cuentas (
    id                  uuid          NOT NULL,
    titular_id          uuid          NOT NULL,
    propietario_id      uuid          NULL,
    entidad_bancaria_id uuid          NOT NULL,
    codigo_cuenta       varchar(30)   NOT NULL,
    saldo               numeric(15,2) NOT NULL,
    moneda              char(3)       NOT NULL,
    estado              varchar(20)   NOT NULL DEFAULT 'ACTIVA',
    fecha_creacion      timestamptz   NOT NULL,
    fecha_actualizacion timestamptz   NOT NULL,
    CONSTRAINT pk_cuentas PRIMARY KEY (id)
);

COMMENT ON TABLE nayra.cuentas IS 'Cuenta financiera SIMULADA (D-021, D-024). El identificador para QR sigue pendiente (03 §16.10).';

CREATE TABLE nayra.dispositivos (
    id                uuid        NOT NULL,
    usuario_id        uuid        NOT NULL,
    clave_publica     bytea       NOT NULL,
    algoritmo_clave   varchar(30) NOT NULL DEFAULT 'ECDSA_P256_SHA256',
    plataforma        varchar(10) NOT NULL,
    estado            varchar(20) NOT NULL DEFAULT 'ACTIVO',
    fecha_vinculacion timestamptz NOT NULL,
    fecha_revocacion  timestamptz NULL,
    CONSTRAINT pk_dispositivos PRIMARY KEY (id)
);

COMMENT ON COLUMN nayra.dispositivos.id IS 'También es el identificador del dispositivo que la app firma junto al nonce (D-048). No se guarda identificador de hardware.';
COMMENT ON COLUMN nayra.dispositivos.clave_publica IS 'Clave pública X.509 SubjectPublicKeyInfo (DER). La privada nunca sale del teléfono (D-048).';

CREATE TABLE nayra.auditoria (
    id                  uuid        NOT NULL,
    fecha               timestamptz NOT NULL,
    actor_id            uuid        NULL,
    usuario_afectado_id uuid        NULL,
    accion              varchar(50) NOT NULL,
    resultado           varchar(10) NOT NULL,
    motivo              varchar(50) NULL,
    ip                  inet        NULL,
    dispositivo_id      uuid        NULL,
    CONSTRAINT pk_auditoria PRIMARY KEY (id)
);

COMMENT ON TABLE nayra.auditoria IS 'Solo inserción para el usuario de ejecución (V004). Sin PIN, audio, embeddings ni puntajes (03 §16.9). Catálogo definitivo pendiente (D-019).';
