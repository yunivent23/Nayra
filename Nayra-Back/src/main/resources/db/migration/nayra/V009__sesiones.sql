-- Modelo de datos v4 (2026-09-27), §2.7 y §4.1: sesiones del servicio de autenticación.
-- id = claim jti del JWT. No se guarda el JWT, su hash, un refresh token, una expiración absoluta ni un motivo.
-- Sesión válida: fecha_revocacion IS NULL y como máximo 5 minutos desde fecha_ultimo_acceso (D-018), evaluado en
-- el servidor. Sin renovación ni duración máxima absoluta.

CREATE TABLE nayra.sesiones (
    id                  uuid        NOT NULL,
    usuario_id          uuid        NOT NULL,
    dispositivo_id      uuid        NOT NULL,
    fecha_creacion      timestamptz NOT NULL,
    fecha_ultimo_acceso timestamptz NOT NULL,
    fecha_revocacion    timestamptz NULL,
    CONSTRAINT pk_sesiones PRIMARY KEY (id),
    -- FK entre dominios (Autenticación → Negocio), v4 §3.
    CONSTRAINT fk_sesiones_usuario_id FOREIGN KEY (usuario_id)
        REFERENCES nayra.usuarios (id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_sesiones_dispositivo_id FOREIGN KEY (dispositivo_id)
        REFERENCES nayra.dispositivos (id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    -- (a confirmar) v4 §2.7.
    CONSTRAINT ck_sesiones_ultimo_acceso CHECK (fecha_ultimo_acceso >= fecha_creacion)
);

COMMENT ON COLUMN nayra.sesiones.id IS 'Igual al claim jti del JWT (v4 §4.1). El JWT no se guarda.';

-- (a confirmar) v4 §2.7.
CREATE INDEX ix_sesiones_usuario_id ON nayra.sesiones (usuario_id);
CREATE INDEX ix_sesiones_dispositivo_id ON nayra.sesiones (dispositivo_id);
