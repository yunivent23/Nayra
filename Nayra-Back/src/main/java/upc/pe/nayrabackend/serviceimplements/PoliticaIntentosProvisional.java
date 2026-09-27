package upc.pe.nayrabackend.serviceimplements;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Qué resultado cuenta como intento fallido.
 *
 * PROVISIONAL — el detalle de D-044 sigue PENDIENTE DE DECISIÓN. Se implementa la
 * propuesta registrada en D-044 (no aprobada) para que el prototipo funcione:
 * cuentan el PIN incorrecto, el contenido del desafío incorrecto, el posible spoofing
 * y la voz no coincidente; la calidad insuficiente y el formato inválido permiten
 * repetir sin contar (HU-44); los errores técnicos no cuentan; contador único por
 * cuenta de acceso. El valor de 3 intentos está aprobado (D-044).
 */
@Profile("prototipo")
@Component
public class PoliticaIntentosProvisional {

    public static final String PIN_INCORRECTO = "PIN_INCORRECTO";

    private static final Set<String> CUENTAN = Set.of(
            PIN_INCORRECTO, "CONTENIDO_INCORRECTO", "POSIBLE_SPOOFING", "NO_COINCIDE");

    public boolean cuentaComoIntentoFallido(String motivo) {
        return CUENTAN.contains(motivo);
    }
}
