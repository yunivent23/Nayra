package upc.pe.nayrabackend.dtos;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/** 04_API.md §2.1. El dispositivo identifica al usuario (opción recomendada, pendiente de confirmación). */
public record SolicitudDesafioDTO(@NotNull UUID dispositivoId) {
}
