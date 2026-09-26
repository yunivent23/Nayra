package upc.pe.nayrabackend.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

/**
 * Parámetros de seguridad (D-042, D-043, D-047, D-048). Los valores pendientes de definición
 * son obligatorios: si faltan, la aplicación no arranca (no se usan valores implícitos).
 */
@Validated
@ConfigurationProperties(prefix = "nayra.seguridad")
public record NayraSeguridadProperties(
        @Valid @NotNull Pin pin,
        @Valid @NotNull Intentos intentos,
        @Valid @NotNull Desafio desafio,
        @Valid @NotNull Sesion sesion
) {
    public record Pin(@NotBlank String pepper) {
    }

    public record Intentos(@NotNull @Min(1) Integer maximo) {
    }

    public record Desafio(@NotNull Duration vigencia) {
    }

    public record Sesion(
            @NotNull Duration inactividad,
            @NotNull Duration duracionMaxima,
            @NotNull Duration intervaloActualizacion
    ) {
    }
}
