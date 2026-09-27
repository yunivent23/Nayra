package upc.pe.nayrabackend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * Parámetros del flujo de autenticación por voz (AG-13).
 *
 * Los valores viven en application-prototipo.properties. Las duraciones del desafío y de la
 * transacción y los timeouts son PROVISIONALES DEL PROTOTIPO. La vida del nonce del dispositivo
 * pasó a los parámetros generales (NayraProperties).
 * El número de intentos (3) está aprobado en D-044.
 */
@ConfigurationProperties(prefix = "nayra.voz")
public record VozProperties(
        String servicioUrl,
        String servicioToken,
        Duration servicioTimeoutConexion,
        Duration servicioTimeoutLectura,
        String vocabularioRuta,
        Duration vidaDesafio,
        Duration vidaTransaccion,
        int intentosMaximos,
        int muestrasEnrolamiento
) {
}
