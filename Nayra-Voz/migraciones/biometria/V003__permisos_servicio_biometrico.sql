-- Permisos del usuario de ejecución del servicio biométrico (D-013: usuario exclusivo del servicio Python).
-- Borrar es necesario: B-13 define la eliminación física y el reenrolamiento borra el perfil anterior.
-- Como en nayra V004, si el rol no existe o es el usuario que migra, no se concede nada y se avisa (D-016, D-017).
DO $$
DECLARE
    rol text := '${rol_biometria}';
BEGIN
    IF rol = current_user OR NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = rol) THEN
        RAISE WARNING 'El rol del servicio biométrico (%) no existe o es el usuario de migración: no se concedieron permisos.', rol;
    ELSE
        EXECUTE format('GRANT USAGE ON SCHEMA biometria TO %I', rol);
        EXECUTE format('GRANT SELECT, INSERT, UPDATE, DELETE ON biometria.perfiles_voz TO %I', rol);
    END IF;
END
$$;
