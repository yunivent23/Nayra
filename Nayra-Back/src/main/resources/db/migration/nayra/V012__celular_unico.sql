-- G-1 (modificada el 2026-09-28): el destinatario de una transferencia se busca por el número de celular
-- registrado en Nayra, así que un número debe llevar a una sola cuenta de acceso.
-- Formato canónico (decisión del 2026-09-28): celular de Perú, 9 dígitos que empiezan por 9, sin "+51".
--
-- Antes de agregar las restricciones se comprueban los datos existentes. Si hay números con otro formato o
-- repetidos, la migración se detiene SIN modificar ni eliminar usuarios: corregir esos datos requiere una
-- decisión del equipo. No se incluyen los números en el mensaje (dato personal).
DO $$
DECLARE
    fuera_de_formato bigint;
    repetidos        bigint;
BEGIN
    SELECT count(*) INTO fuera_de_formato FROM nayra.usuarios WHERE numero_celular !~ '^9[0-9]{8}$';
    SELECT count(*) INTO repetidos FROM (
        SELECT 1 FROM nayra.usuarios
        GROUP BY regexp_replace(numero_celular, '^\+51', '')
        HAVING count(*) > 1) r;
    IF fuera_de_formato > 0 OR repetidos > 0 THEN
        RAISE EXCEPTION 'V012 detenida: % usuario(s) con celular fuera del formato canónico y % número(s) repetido(s). '
            'Se requiere una decisión sobre esos datos antes de aplicar la unicidad (G-1).', fuera_de_formato, repetidos;
    END IF;
END $$;

ALTER TABLE nayra.usuarios
    ADD CONSTRAINT uq_usuarios_numero_celular UNIQUE (numero_celular),
    ADD CONSTRAINT ck_usuarios_numero_celular CHECK (numero_celular ~ '^9[0-9]{8}$');

COMMENT ON COLUMN nayra.usuarios.numero_celular IS
    'Celular de Perú en formato canónico (9 dígitos, empieza por 9, sin +51). Único: localiza al destinatario de una '
    'transferencia (G-1). No prueba la titularidad del número (sin validación con operadores).';
