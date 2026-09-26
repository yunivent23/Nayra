package upc.pe.nayrabackend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Umbrales de decisión (D-038). Todos son opcionales: mientras un umbral no tenga valor aprobado
 * el sistema permanece en modo calibración y no autentica por voz.
 */
@ConfigurationProperties(prefix = "nayra.umbrales")
public record NayraUmbralesProperties(
        String version,
        Double similitudMinima,
        Double spoofingMinimo,
        Double vozNetaMinimaSegundos,
        Double snrMinimoDb,
        Double saturacionMaxima,
        Double confianzaContenidoMinima,
        Double consistenciaEnrolamientoMinima
) {
    /** Umbrales necesarios para decidir sobre una muestra de voz. */
    public boolean completosParaVerificacion() {
        return similitudMinima != null && spoofingMinimo != null && vozNetaMinimaSegundos != null
                && snrMinimoDb != null && saturacionMaxima != null && confianzaContenidoMinima != null;
    }

    public boolean completosParaEnrolamiento() {
        return completosParaVerificacion() && consistenciaEnrolamientoMinima != null;
    }
}
