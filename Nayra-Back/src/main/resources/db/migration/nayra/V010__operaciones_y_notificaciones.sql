-- Modelo de datos v4 (2026-09-27), §2.8 y §2.11: operaciones (transferencias simuladas) y notificaciones.
-- Entorno bancario simulado: no son transacciones reales (D-021, D-022).
-- G-1 (qué dato identifica al destinatario en la interfaz de HU-69) sigue PENDIENTE BLOQUEANTE y no se decide aquí:
-- la tabla guarda la cuenta de destino por su id interno.

CREATE TABLE nayra.operaciones (
    id                  uuid          NOT NULL,
    cuenta_origen_id    uuid          NOT NULL,
    cuenta_destino_id   uuid          NOT NULL,
    tipo                varchar(30)   NOT NULL,
    monto               numeric(15,2) NOT NULL,
    moneda              char(3)       NOT NULL DEFAULT 'PEN',   -- DEFAULT (a confirmar)
    detalle             varchar(255)  NULL,
    codigo_referencia   char(6)       NOT NULL,
    estado              varchar(20)   NOT NULL,
    canal               varchar(10)   NOT NULL DEFAULT 'MOVIL',
    fecha               timestamptz   NOT NULL,
    fecha_actualizacion timestamptz   NOT NULL,
    CONSTRAINT pk_operaciones PRIMARY KEY (id),
    CONSTRAINT fk_operaciones_cuenta_origen_id FOREIGN KEY (cuenta_origen_id)
        REFERENCES nayra.cuentas (id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_operaciones_cuenta_destino_id FOREIGN KEY (cuenta_destino_id)
        REFERENCES nayra.cuentas (id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT uq_operaciones_codigo_referencia UNIQUE (codigo_referencia),
    CONSTRAINT ck_operaciones_cuentas_distintas CHECK (cuenta_origen_id <> cuenta_destino_id),
    CONSTRAINT ck_operaciones_tipo CHECK (tipo IN ('TRANSFERENCIA')),
    CONSTRAINT ck_operaciones_monto CHECK (monto > 0 AND monto < 500),
    CONSTRAINT ck_operaciones_moneda CHECK (moneda = 'PEN'),
    CONSTRAINT ck_operaciones_codigo_referencia CHECK (codigo_referencia ~ '^[0-9]{6}$'),
    CONSTRAINT ck_operaciones_estado CHECK (estado IN ('EXITOSO', 'FALLIDO', 'CANCELADO')),
    CONSTRAINT ck_operaciones_canal CHECK (canal = 'MOVIL')
);

COMMENT ON TABLE nayra.operaciones IS 'Transferencias SIMULADAS (v4 §2.8). Identificador del destinatario en la interfaz: G-1 PENDIENTE BLOQUEANTE.';
COMMENT ON COLUMN nayra.operaciones.codigo_referencia IS '6 dígitos, único global; lo genera el backend y reintenta si el UNIQUE lo rechaza (v4 §2.8).';

-- (a confirmar) v4 §2.8.
CREATE INDEX ix_operaciones_cuenta_origen_id ON nayra.operaciones (cuenta_origen_id);
CREATE INDEX ix_operaciones_cuenta_destino_id ON nayra.operaciones (cuenta_destino_id);

CREATE TABLE nayra.notificaciones (
    id               uuid         NOT NULL,
    destinatario_id  uuid         NOT NULL,
    operacion_id     uuid         NOT NULL,
    tipo             varchar(30)  NOT NULL,
    titulo           varchar(150) NOT NULL,
    contenido        text         NOT NULL,
    leida            boolean      NOT NULL DEFAULT false,
    fecha_generacion timestamptz  NOT NULL,
    fecha_lectura    timestamptz  NULL,
    CONSTRAINT pk_notificaciones PRIMARY KEY (id),
    CONSTRAINT fk_notificaciones_destinatario_id FOREIGN KEY (destinatario_id)
        REFERENCES nayra.usuarios (id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_notificaciones_operacion_id FOREIGN KEY (operacion_id)
        REFERENCES nayra.operaciones (id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT ck_notificaciones_tipo CHECK (tipo IN ('OPERACION')),
    -- (a confirmar) v4 §2.11.
    CONSTRAINT uq_notificaciones_operacion_destinatario UNIQUE (operacion_id, destinatario_id),
    CONSTRAINT ck_notificaciones_leida CHECK (leida = (fecha_lectura IS NOT NULL))
);

COMMENT ON COLUMN nayra.notificaciones.contenido IS 'Texto generado, p. ej. "Juan Per... te realizó una transferencia de S/ 150.00." El emisor y el monto no se duplican en columnas (v4 §2.11).';
