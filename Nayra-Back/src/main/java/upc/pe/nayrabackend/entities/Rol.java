package upc.pe.nayrabackend.entities;

/**
 * Roles del sistema (D-041): solo USER y ADMIN. El rol nunca lo elige el cliente (06 §36.2).
 * PROVISIONAL (D-009): un rol por usuario, representado como valor fijo; si un usuario puede tener
 * varios roles y si ROLES será catálogo con FK sigue pendiente.
 */
public enum Rol {
    USER,
    ADMIN
}
