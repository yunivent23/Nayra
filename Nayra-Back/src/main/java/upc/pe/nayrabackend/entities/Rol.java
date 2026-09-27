package upc.pe.nayrabackend.entities;

/**
 * Roles del sistema (D-041): solo USER y ADMIN. El rol nunca lo elige el cliente (06 §36.2).
 * D-009 (aprobada el 2026-09-27, opción A): valor fijo, un rol por usuario, guardado como texto en
 * nayra.usuarios.rol con CHECK (USER, ADMIN). No existe tabla ROLES ni varios roles por usuario.
 */
public enum Rol {
    USER,
    ADMIN
}
