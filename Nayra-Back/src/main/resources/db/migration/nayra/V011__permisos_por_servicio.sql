-- Permisos sobre las tablas nuevas (v4) y usuarios de base de datos por servicio (P-4, v4 §4.2).
--
-- 1) Usuario de ejecución actual (${rol_ejecucion}). Hoy Negocio y Autenticación son UNA sola aplicación Spring Boot
--    con una sola conexión, así que ese usuario necesita las tablas de ambos servicios. Igual que en V004, la
--    auditoría sigue siendo de solo inserción.
--
-- 2) Usuarios por servicio, para cuando Negocio y Autenticación se ejecuten por separado (P-4 PENDIENTE NO
--    BLOQUEANTE: falta decidir qué servicio ejecuta Flyway, los usuarios definitivos y el orden). Cada uno recibe
--    permisos solo sobre las tablas de su servicio (v4 §4.2). Autenticación NO recibe SELECT sobre usuarios: las FK
--    credenciales/dispositivos/sesiones → usuarios las comprueba PostgreSQL con los privilegios del propietario de
--    la tabla referenciada. Comprobado en PermisosPorServicioTest.
--    La auditoría no tiene servicio propietario en v4 (§2.10); ambos servicios la escriben hoy, así que ambos
--    reciben solo INSERT (a confirmar con P-4).
--
-- Como en V004, crear los usuarios y sus credenciales corresponde al despliegue (D-016, D-017): si un rol no
-- existe, no se concede nada y se avisa.
DO $$
DECLARE
    ejecucion      text := '${rol_ejecucion}';
    negocio        text := '${rol_negocio}';
    autenticacion  text := '${rol_autenticacion}';
BEGIN
    IF ejecucion = current_user THEN
        RAISE WARNING 'El usuario de ejecución (%) es el mismo que ejecuta las migraciones: no se aplica la separación de permisos.', ejecucion;
    ELSIF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = ejecucion) THEN
        RAISE WARNING 'El rol de ejecución % no existe: no se concedieron permisos.', ejecucion;
    ELSE
        EXECUTE format('GRANT SELECT, INSERT, UPDATE ON nayra.credenciales, nayra.sesiones, nayra.operaciones, '
                       'nayra.notificaciones TO %I', ejecucion);
    END IF;

    IF negocio = current_user OR NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = negocio) THEN
        RAISE WARNING 'El rol del servicio de negocio (%) no existe o es el usuario de migración: no se concedieron permisos.', negocio;
    ELSE
        EXECUTE format('GRANT USAGE ON SCHEMA nayra TO %I', negocio);
        EXECUTE format('GRANT SELECT, INSERT, UPDATE ON nayra.usuarios, nayra.registro_identidad_simulado, '
                       'nayra.entidades_bancarias, nayra.cuentas, nayra.operaciones, nayra.notificaciones TO %I', negocio);
        EXECUTE format('GRANT INSERT ON nayra.auditoria TO %I', negocio);
    END IF;

    IF autenticacion = current_user OR NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = autenticacion) THEN
        RAISE WARNING 'El rol del servicio de autenticación (%) no existe o es el usuario de migración: no se concedieron permisos.', autenticacion;
    ELSE
        EXECUTE format('GRANT USAGE ON SCHEMA nayra TO %I', autenticacion);
        EXECUTE format('GRANT SELECT, INSERT, UPDATE ON nayra.credenciales, nayra.dispositivos, nayra.sesiones TO %I', autenticacion);
        EXECUTE format('GRANT INSERT ON nayra.auditoria TO %I', autenticacion);
    END IF;
END
$$;
