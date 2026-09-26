package upc.pe.nayrabackend.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

/** Conexión con el servicio de voz por REST interno (D-040). */
@Validated
@ConfigurationProperties(prefix = "nayra.voz")
public record NayraVozProperties(
        @NotBlank String url,
        @NotBlank String token,
        @NotNull Duration timeoutConexion,
        @NotNull Duration timeoutLectura
) {
}
