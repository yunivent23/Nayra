package upc.pe.nayrabackend.dtos;

import java.time.OffsetDateTime;
import java.util.UUID;

/** Sesión activa para HU-14. No incluye el token ni su hash. */
public record SesionDTO(UUID id, String dispositivo, OffsetDateTime inicio, OffsetDateTime ultimoAcceso, boolean actual) {
}
