-- D-051 (aprobada el 2026-09-27): esquema propio para las tablas generales del backend.
-- Flyway también crea el esquema al arrancar (spring.flyway.schemas=nayra); esta sentencia lo deja explícito
-- en el historial versionado. El esquema biométrico (biometria) tiene su propio historial (D-013, D-051).
CREATE SCHEMA IF NOT EXISTS nayra;

COMMENT ON SCHEMA nayra IS 'Tablas generales de Nayra (03 §16, D-051). No contiene datos biométricos (D-013).';
