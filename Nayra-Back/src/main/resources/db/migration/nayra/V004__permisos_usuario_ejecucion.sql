-- Permisos del usuario de ejecución de Spring Boot (D-051, 2026-09-27; mínimo privilegio, 06 §22).
-- Las migraciones las ejecuta el usuario de migración (spring.flyway.user); la aplicación se conecta con
-- el usuario de ejecución (spring.datasource.username), cuyo nombre llega por el marcador ${rol_ejecucion}.
-- La auditoría es de solo inserción: el usuario de ejecución no recibe UPDATE ni DELETE sobre ella.
-- Si el rol no existe o es el mismo usuario que migra (entorno local con un solo usuario), no se concede
-- nada y se avisa: crear los usuarios y sus credenciales corresponde al despliegue (D-016, D-017).
DO $$
DECLARE
    rol text := '${rol_ejecucion}';
BEGIN
    IF rol = current_user THEN
        RAISE WARNING 'El usuario de ejecución (%) es el mismo que ejecuta las migraciones: no se aplica la separación de permisos.', rol;
    ELSIF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = rol) THEN
        RAISE WARNING 'El rol de ejecución % no existe: no se concedieron permisos.', rol;
    ELSE
        EXECUTE format('GRANT USAGE ON SCHEMA nayra TO %I', rol);
        EXECUTE format('GRANT SELECT, INSERT, UPDATE ON nayra.entidades_bancarias, nayra.registro_identidad_simulado, '
                       'nayra.usuarios, nayra.cuentas, nayra.dispositivos TO %I', rol);
        EXECUTE format('GRANT SELECT, INSERT ON nayra.auditoria TO %I', rol);
        EXECUTE format('REVOKE UPDATE, DELETE, TRUNCATE ON nayra.auditoria FROM %I', rol);
    END IF;
END
$$;
