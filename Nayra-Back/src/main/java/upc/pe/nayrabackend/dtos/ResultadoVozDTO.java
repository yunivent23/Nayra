package upc.pe.nayrabackend.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/** Resultado técnico del servicio de voz (04_API.md §3.2). Solo puntajes: la decisión la toma Java (D-040). */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ResultadoVozDTO(
        @JsonProperty("request_id") String requestId,
        Calidad calidad,
        Contenido contenido,
        Spoofing spoofing,
        Biometria biometria
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Calidad(@JsonProperty("duracion_s") Double duracionS,
                          @JsonProperty("voz_neta_s") Double vozNetaS,
                          @JsonProperty("snr_db") Double snrDb,
                          Double saturacion) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Contenido(String transcripcion, Boolean coincide, Double confianza) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Spoofing(Double puntaje) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Biometria(Double similitud, @JsonProperty("perfil_encontrado") Boolean perfilEncontrado) {
    }
}
