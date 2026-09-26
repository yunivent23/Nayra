package upc.pe.nayrabackend.dtos;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/** Respuesta de 04_API.md §2.1. */
public record DesafioDTO(UUID desafioId, List<String> elementos, String texto, String nonce, OffsetDateTime expiraEn) {
}
