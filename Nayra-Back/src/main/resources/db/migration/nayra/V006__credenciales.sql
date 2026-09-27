-- Modelo de datos v4 (2026-09-27), §2.2 (P-1 cerrado): credencial del PIN en su propia tabla, del servicio de
-- autenticación. pin_hash e intentos_fallidos salen de usuarios (servicio de negocio).
-- Los hashes existentes (solo datos de desarrollo) se copian primero, se verifica la copia y después se eliminan
-- las columnas de usuarios. Si la verificación falla, la migración se detiene sin borrar nada.

CREATE TABLE nayra.credenciales (
    id                  uuid        NOT NULL,
    usuario_id          uuid        NOT NULL,
    pin_hash            text        NOT NULL,
    intentos_fallidos   smallint    NOT NULL DEFAULT 0,
    fecha_creacion      timestamptz NOT NULL,
    fecha_actualizacion timestamptz NOT NULL,
    CONSTRAINT pk_credenciales PRIMARY KEY (id),
    -- FK entre dominios (Autenticación → Negocio), como dispositivos (v4 §3, §4.2).
    CONSTRAINT fk_credenciales_usuario_id FOREIGN KEY (usuario_id)
        REFERENCES nayra.usuarios (id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT uq_credenciales_usuario_id UNIQUE (usuario_id),
    -- (a confirmar) v4 §2.2: máximo 3 intentos de PIN.
    CONSTRAINT ck_credenciales_intentos CHECK (intentos_fallidos BETWEEN 0 AND 3)
);

COMMENT ON TABLE nayra.credenciales IS 'Credencial del PIN (servicio de autenticación, v4 §2.2). pin_hash no sale de Autenticación.';
COMMENT ON COLUMN nayra.credenciales.pin_hash IS 'Hash autodescriptivo del PIN de 6 dígitos (D-061); nunca el PIN. Algoritmo y pepper: D-047 (PENDIENTE; BCrypt PROVISIONAL).';
COMMENT ON COLUMN nayra.credenciales.intentos_fallidos IS 'PIN incorrectos consecutivos. Solo cuenta el PIN; vuelve a 0 tras un PIN correcto (v4 §2.2).';

-- Copia de los datos de desarrollo. El contador anterior podía superar 3 si contaba fallos de voz: se limita a 3,
-- que es el máximo con el que la cuenta ya quedaba bloqueada.
INSERT INTO nayra.credenciales (id, usuario_id, pin_hash, intentos_fallidos, fecha_creacion, fecha_actualizacion)
SELECT gen_random_uuid(), u.id, u.pin_hash, LEAST(u.intentos_fallidos, 3), u.fecha_creacion, u.fecha_actualizacion
FROM nayra.usuarios u;

-- Verificación previa al borrado: cada usuario tiene exactamente una credencial con el mismo hash.
DO $$
DECLARE
    usuarios_total  bigint;
    copiadas        bigint;
BEGIN
    SELECT count(*) INTO usuarios_total FROM nayra.usuarios;
    SELECT count(*) INTO copiadas
    FROM nayra.usuarios u JOIN nayra.credenciales c ON c.usuario_id = u.id AND c.pin_hash = u.pin_hash;
    IF copiadas <> usuarios_total THEN
        RAISE EXCEPTION 'Copia de credenciales incompleta: % usuarios, % credenciales verificadas. No se eliminan columnas.',
            usuarios_total, copiadas;
    END IF;
    RAISE NOTICE 'Credenciales copiadas y verificadas: %', copiadas;
END
$$;

ALTER TABLE nayra.usuarios DROP CONSTRAINT ck_usuarios_intentos;
ALTER TABLE nayra.usuarios DROP COLUMN pin_hash;
ALTER TABLE nayra.usuarios DROP COLUMN intentos_fallidos;
