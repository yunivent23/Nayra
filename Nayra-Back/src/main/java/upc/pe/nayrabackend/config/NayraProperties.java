package upc.pe.nayrabackend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * Parámetros del backend general.
 *
 * @param sesion      inactividad máxima de la sesión: 5 minutos, APROBADO (D-018); el resto del mecanismo es provisional
 * @param dispositivo vida del nonce de firma: PROVISIONAL DEL PROTOTIPO (D-048)
 * @param registro    vida del código de registro en curso: PROVISIONAL DEL PROTOTIPO; no es decisión de D-052
 */
@ConfigurationProperties(prefix = "nayra")
public record NayraProperties(Sesion sesion, Dispositivo dispositivo, Registro registro) {

    public record Sesion(Duration inactividadMaxima) {
    }

    public record Dispositivo(Duration vidaNonce) {
    }

    public record Registro(Duration vidaRegistroEnCurso) {
    }
}
