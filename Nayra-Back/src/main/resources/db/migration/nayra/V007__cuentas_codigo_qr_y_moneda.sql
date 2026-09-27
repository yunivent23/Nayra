-- Modelo de datos v4 (2026-09-27), §2.5: moneda PEN y codigo_qr.
--
-- codigo_qr: fijo, guardado en la BD y UNIQUE (DEFINIDO). Su formato es PENDIENTE NO BLOQUEANTE (P-3).
-- PROVISIONAL: mientras P-3 siga abierto se guarda un identificador aleatorio (UUID v4 en texto), que no contiene
-- el número de documento ni otro dato personal (D-042). El tipo varchar(64) también es PROVISIONAL: se fija con el
-- formato final. Reemplazar los valores provisionales por el formato final requiere una migración, que debe
-- hacerse antes de implementar la generación y lectura del QR (HU-123, HU-124, no implementadas).

ALTER TABLE nayra.cuentas ADD COLUMN codigo_qr varchar(64);
UPDATE nayra.cuentas SET codigo_qr = gen_random_uuid()::text WHERE codigo_qr IS NULL;
ALTER TABLE nayra.cuentas ALTER COLUMN codigo_qr SET NOT NULL;
ALTER TABLE nayra.cuentas ADD CONSTRAINT uq_cuentas_codigo_qr UNIQUE (codigo_qr);

COMMENT ON COLUMN nayra.cuentas.codigo_qr IS 'PROVISIONAL (P-3): identificador aleatorio fijo y único; formato y tipo finales pendientes. Sin datos personales.';

-- Soles (v4 §2.5). Si hubiera filas con otra moneda, el CHECK falla y la migración se detiene sin cambiar datos.
ALTER TABLE nayra.cuentas ALTER COLUMN moneda SET DEFAULT 'PEN';   -- (a confirmar)
ALTER TABLE nayra.cuentas ADD CONSTRAINT ck_cuentas_moneda CHECK (moneda = 'PEN');
