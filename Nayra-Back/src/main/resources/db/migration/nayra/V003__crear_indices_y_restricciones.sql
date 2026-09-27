-- Restricciones derivadas de reglas aprobadas e índices mínimos (D-051, 2026-09-27).
-- Todas las FK: ON DELETE RESTRICT ON UPDATE RESTRICT (sin CASCADE ni SET NULL).

-- registro_identidad_simulado: el DNI es clave alternativa (03 §16.3).
ALTER TABLE nayra.registro_identidad_simulado
    ADD CONSTRAINT uq_registro_identidad_simulado_dni UNIQUE (dni);

-- usuarios
ALTER TABLE nayra.usuarios
    ADD CONSTRAINT uq_usuarios_dni UNIQUE (dni),                                   -- D-036
    ADD CONSTRAINT ck_usuarios_rol CHECK (rol IN ('USER', 'ADMIN')),               -- D-041, D-009 (opción A)
    ADD CONSTRAINT ck_usuarios_estado CHECK (estado IN ('ACTIVA', 'BLOQUEADA')),   -- D-040, D-044; otros estados: AG-05
    ADD CONSTRAINT ck_usuarios_intentos CHECK (intentos_fallidos >= 0);           -- PROVISIONAL (D-044)

-- cuentas
ALTER TABLE nayra.cuentas
    ADD CONSTRAINT fk_cuentas_titular_id FOREIGN KEY (titular_id)
        REFERENCES nayra.registro_identidad_simulado (id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    ADD CONSTRAINT fk_cuentas_propietario_id FOREIGN KEY (propietario_id)
        REFERENCES nayra.usuarios (id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    ADD CONSTRAINT fk_cuentas_entidad_bancaria_id FOREIGN KEY (entidad_bancaria_id)
        REFERENCES nayra.entidades_bancarias (id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    ADD CONSTRAINT uq_cuentas_titular_id UNIQUE (titular_id),          -- D-025, D-035
    ADD CONSTRAINT uq_cuentas_propietario_id UNIQUE (propietario_id),  -- D-025, D-028 (admite varios NULL)
    ADD CONSTRAINT uq_cuentas_codigo_cuenta UNIQUE (codigo_cuenta),
    ADD CONSTRAINT ck_cuentas_saldo CHECK (saldo >= 0),
    ADD CONSTRAINT ck_cuentas_estado CHECK (estado IN ('ACTIVA', 'BLOQUEADA', 'CERRADA'));

CREATE INDEX ix_cuentas_entidad_bancaria_id ON nayra.cuentas (entidad_bancaria_id);

-- dispositivos
ALTER TABLE nayra.dispositivos
    ADD CONSTRAINT fk_dispositivos_usuario_id FOREIGN KEY (usuario_id)
        REFERENCES nayra.usuarios (id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    ADD CONSTRAINT ck_dispositivos_estado CHECK (estado IN ('ACTIVO', 'REVOCADO')),       -- D-039, D-040
    ADD CONSTRAINT ck_dispositivos_plataforma CHECK (plataforma IN ('ANDROID', 'IOS')),
    ADD CONSTRAINT ck_dispositivos_algoritmo CHECK (algoritmo_clave = 'ECDSA_P256_SHA256'); -- D-048

-- Un solo dispositivo ACTIVO por usuario (D-039).
CREATE UNIQUE INDEX uq_dispositivos_usuario_activo ON nayra.dispositivos (usuario_id) WHERE estado = 'ACTIVO';
CREATE INDEX ix_dispositivos_usuario_id ON nayra.dispositivos (usuario_id);

-- auditoria
ALTER TABLE nayra.auditoria
    ADD CONSTRAINT fk_auditoria_actor_id FOREIGN KEY (actor_id)
        REFERENCES nayra.usuarios (id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    ADD CONSTRAINT fk_auditoria_usuario_afectado_id FOREIGN KEY (usuario_afectado_id)
        REFERENCES nayra.usuarios (id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    ADD CONSTRAINT fk_auditoria_dispositivo_id FOREIGN KEY (dispositivo_id)
        REFERENCES nayra.dispositivos (id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    ADD CONSTRAINT ck_auditoria_resultado CHECK (resultado IN ('EXITOSO', 'FALLIDO'));

CREATE INDEX ix_auditoria_fecha ON nayra.auditoria (fecha DESC);
CREATE INDEX ix_auditoria_actor_id ON nayra.auditoria (actor_id);
CREATE INDEX ix_auditoria_usuario_afectado_id ON nayra.auditoria (usuario_afectado_id);
