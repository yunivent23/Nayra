-- Ejecutar UNA vez como superusuario de PostgreSQL (psql -v ...). Separa privilegios (D-039, 06_SEGURIDAD):
--   nayra_app : backend Spring Boot, dueño del esquema public.
--   nayra_voz : servicio de voz, dueño del esquema biometria. El backend no tiene acceso a biometria.
-- Las contraseñas se pasan como variables de psql, nunca se escriben en el repositorio:
--   psql -d nayra -v app_pass="'...'" -v voz_pass="'...'" -f db/01_roles_y_esquemas.sql

CREATE ROLE nayra_app LOGIN PASSWORD :app_pass;
CREATE ROLE nayra_voz LOGIN PASSWORD :voz_pass;

GRANT CONNECT ON DATABASE nayra TO nayra_app, nayra_voz;
ALTER SCHEMA public OWNER TO nayra_app;
REVOKE ALL ON SCHEMA public FROM PUBLIC;

CREATE SCHEMA biometria AUTHORIZATION nayra_voz;
REVOKE ALL ON SCHEMA biometria FROM PUBLIC;
