-- biometria.perfiles_voz (modelo de datos v4 §2.12, B-1 a B-13). Solo el embedding de referencia cifrado:
-- nunca audio (D-013).
--
-- NO se aplica todavía el CHECK de longitud de embedding_cifrado (784 bytes = 192 × float32 + tag GCM de 16):
-- v4 §4.4 exige fijarlo después de la primera ejecución real del extractor ECAPA, que aún no fue posible.
CREATE TABLE biometria.perfiles_voz (
    id                  uuid         NOT NULL,
    usuario_id          uuid         NOT NULL,   -- nayra.usuarios.id, referencia lógica SIN FK física (B-6, D-051)
    embedding_cifrado   bytea        NOT NULL,   -- cifrado ‖ tag GCM (B-1, B-3)
    iv                  bytea        NOT NULL,   -- nonce de 96 bits, nuevo en cada cifrado (B-2)
    clave_version       smallint     NOT NULL,   -- versión de la clave; la clave nunca está en la BD (B-5)
    modelo              varchar(100) NOT NULL,   -- B-9
    version_modelo      varchar(64)  NOT NULL,   -- B-9
    numero_muestras     smallint     NOT NULL,   -- PROVISIONAL: sin máximo físico hasta D-059 (B-12)
    estado              varchar(10)  NOT NULL DEFAULT 'ACTIVO',
    fecha_creacion      timestamptz  NOT NULL,
    fecha_actualizacion timestamptz  NOT NULL,
    fecha_revocacion    timestamptz  NULL,
    CONSTRAINT pk_perfiles_voz PRIMARY KEY (id),
    CONSTRAINT uq_perfiles_voz_usuario UNIQUE (usuario_id),                                  -- B-7
    CONSTRAINT ck_perfiles_voz_iv CHECK (octet_length(iv) = 12),                             -- B-2
    CONSTRAINT ck_perfiles_voz_clave_version CHECK (clave_version > 0),                      -- B-5
    CONSTRAINT ck_perfiles_voz_numero_muestras CHECK (numero_muestras > 0),                  -- B-12
    CONSTRAINT ck_perfiles_voz_estado CHECK (estado IN ('ACTIVO', 'REVOCADO')),              -- B-8
    CONSTRAINT ck_perfiles_voz_revocacion CHECK ((estado = 'REVOCADO') = (fecha_revocacion IS NOT NULL))  -- B-11
);

COMMENT ON TABLE biometria.perfiles_voz IS 'Un perfil por usuario (B-7). Volver a enrolar borra el anterior e inserta uno nuevo (B-13).';
