-- Modelo de datos v4 (2026-09-27), §2.6 y restricción de plataforma (CERRADA): el primer entregable es solo
-- Android (APK). Si existiera algún dispositivo IOS, el CHECK falla y la migración se detiene sin cambiar datos.
ALTER TABLE nayra.dispositivos DROP CONSTRAINT ck_dispositivos_plataforma;
ALTER TABLE nayra.dispositivos ADD CONSTRAINT ck_dispositivos_plataforma CHECK (plataforma IN ('ANDROID'));
