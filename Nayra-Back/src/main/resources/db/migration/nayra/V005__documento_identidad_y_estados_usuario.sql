-- Modelo de datos v4 (2026-09-27), §2.1 y §2.3: documento de identidad (DNI o CE) y estados del usuario.
-- La columna dni pasa a numero_documento y se agrega tipo_documento_identidad. Las filas existentes son DNI,
-- porque hasta ahora solo se admitía DNI. Formato del número por tipo: PENDIENTE NO BLOQUEANTE (P-2).

-- registro_identidad_simulado
ALTER TABLE nayra.registro_identidad_simulado DROP CONSTRAINT uq_registro_identidad_simulado_dni;
ALTER TABLE nayra.registro_identidad_simulado RENAME COLUMN dni TO numero_documento;
ALTER TABLE nayra.registro_identidad_simulado ALTER COLUMN numero_documento TYPE varchar(30);
ALTER TABLE nayra.registro_identidad_simulado ADD COLUMN tipo_documento_identidad varchar(20) NOT NULL DEFAULT 'DNI';
-- El valor por defecto solo sirve para completar las filas existentes; v4 no define un valor por defecto.
ALTER TABLE nayra.registro_identidad_simulado ALTER COLUMN tipo_documento_identidad DROP DEFAULT;
ALTER TABLE nayra.registro_identidad_simulado
    ADD CONSTRAINT uq_registro_identidad_simulado_documento UNIQUE (tipo_documento_identidad, numero_documento),
    ADD CONSTRAINT ck_registro_identidad_simulado_tipo_documento CHECK (tipo_documento_identidad IN ('DNI', 'CE'));

-- usuarios: documento
ALTER TABLE nayra.usuarios DROP CONSTRAINT uq_usuarios_dni;
ALTER TABLE nayra.usuarios RENAME COLUMN dni TO numero_documento;
ALTER TABLE nayra.usuarios ALTER COLUMN numero_documento TYPE varchar(30);
ALTER TABLE nayra.usuarios ADD COLUMN tipo_documento_identidad varchar(20) NOT NULL DEFAULT 'DNI';
ALTER TABLE nayra.usuarios ALTER COLUMN tipo_documento_identidad DROP DEFAULT;
ALTER TABLE nayra.usuarios
    ADD CONSTRAINT uq_usuarios_documento UNIQUE (tipo_documento_identidad, numero_documento),
    ADD CONSTRAINT ck_usuarios_tipo_documento CHECK (tipo_documento_identidad IN ('DNI', 'CE'));

-- usuarios: estados ACTIVO, BLOQUEADO, INACTIVO (v4 §2.1). Se traducen los valores anteriores.
ALTER TABLE nayra.usuarios DROP CONSTRAINT ck_usuarios_estado;
UPDATE nayra.usuarios SET estado = CASE estado WHEN 'ACTIVA' THEN 'ACTIVO' WHEN 'BLOQUEADA' THEN 'BLOQUEADO' ELSE estado END;
ALTER TABLE nayra.usuarios ALTER COLUMN estado SET DEFAULT 'ACTIVO';
ALTER TABLE nayra.usuarios ADD CONSTRAINT ck_usuarios_estado CHECK (estado IN ('ACTIVO', 'BLOQUEADO', 'INACTIVO'));

COMMENT ON COLUMN nayra.usuarios.numero_documento IS 'Número del documento de identidad. Formato por tipo: PENDIENTE NO BLOQUEANTE (P-2).';
COMMENT ON COLUMN nayra.usuarios.estado IS 'Estado de la cuenta de acceso; lo posee Negocio aunque el bloqueo lo origine Autenticación (v4 §2.1).';
