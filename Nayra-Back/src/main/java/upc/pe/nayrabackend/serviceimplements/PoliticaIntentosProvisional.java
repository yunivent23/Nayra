package upc.pe.nayrabackend.serviceimplements;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Qué resultado cuenta como intento fallido (modelo de datos v4 §2.2 y §4.3; D-044).
 *
 * Solo el PIN incorrecto cuenta: la voz, el contenido del desafío, el anti-spoofing y la firma del dispositivo no
 * cuentan. El máximo de 3 está aprobado (D-044). Un límite propio para la biometría es P-8, PENDIENTE NO BLOQUEANTE.
 */
@Profile("prototipo")
@Component
public class PoliticaIntentosProvisional {

    public static final String PIN_INCORRECTO = "PIN_INCORRECTO";

    private static final Set<String> CUENTAN = Set.of(PIN_INCORRECTO);

    public boolean cuentaComoIntentoFallido(String motivo) {
        return CUENTAN.contains(motivo);
    }
}
