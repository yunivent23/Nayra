-- Esquema biométrico (D-013, D-051): historial de migraciones PROPIO, separado del esquema nayra.
-- Formato de Flyway (V<n>__<descripcion>.sql) para ejecutarlo con la misma herramienta y un historial propio
-- (biometria.flyway_schema_history). Quién lo ejecuta en el despliegue queda PENDIENTE (P-4).
CREATE SCHEMA IF NOT EXISTS biometria;

COMMENT ON SCHEMA biometria IS 'Referencias biométricas cifradas del servicio de voz (D-013). Sin audio. Sin FK hacia nayra.';
