package upc.pe.nayrabackend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * Parámetros del flujo de autenticación por voz (AG-13).
 *
 * Los valores viven en application-prototipo.properties. Las duraciones del desafío y de la
 * transacción y los timeouts son PROVISIONALES DEL PROTOTIPO. La vida del nonce del dispositivo
 * pasó a los parámetros generales (NayraProperties).
 * El máximo de intentos de PIN (3, D-044) está en ICredencialesService: solo cuenta el PIN (modelo de datos v4 §2.2).
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
        int muestrasEnrolamiento
) {
}
