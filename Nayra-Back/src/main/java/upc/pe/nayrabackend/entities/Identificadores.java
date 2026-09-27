package upc.pe.nayrabackend.entities;

import java.util.Optional;
import java.util.UUID;

/**
 * Identificadores UUID v4 generados por la aplicación (D-051, aprobada el 2026-09-27). En la base de datos son
 * columnas {@code uuid}; el dominio y la API los siguen tratando como texto.
 */
public final class Identificadores {

    private Identificadores() {
    }

    public static String nuevo() {
        return UUID.randomUUID().toString();
    }

    /** Convierte un identificador de dominio; lanza IllegalArgumentException si no es un UUID. */
    public static UUID uuid(String id) {
        return id == null ? null : UUID.fromString(id);
    }

    /** Para valores recibidos del exterior: un texto que no es UUID no identifica ningún registro. */
    public static Optional<UUID> leer(String id) {
        if (id == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(UUID.fromString(id));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    public static String texto(UUID id) {
        return id == null ? null : id.toString();
    }
}
