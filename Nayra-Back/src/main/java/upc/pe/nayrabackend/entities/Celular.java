package upc.pe.nayrabackend.entities;

import upc.pe.nayrabackend.excepciones.NayraException;

import java.util.regex.Pattern;

/**
 * Número de celular recibido del exterior (D-043, G-1 modificada el 2026-09-28).
 *
 * Formato canónico: celular de Perú, 9 dígitos que empiezan por 9. Si llega con el prefijo "+51" se quita antes de
 * validar, y los espacios se ignoran, para que nunca se guarden ni se busquen variantes del mismo número.
 * El número NO prueba que la persona sea titular de la línea: no hay validación con operadores ni OSIPTEL.
 */
public record Celular(String numero) {

    private static final Pattern PERU = Pattern.compile("9\\d{8}");
    private static final String PREFIJO_PERU = "+51";

    /** Celular normalizado o NayraException (400) con CELULAR_INVALIDO. */
    public static Celular leer(String texto) {
        String n = texto == null ? "" : texto.replaceAll("\\s", "");
        if (n.startsWith(PREFIJO_PERU)) {
            n = n.substring(PREFIJO_PERU.length());
        }
        if (!PERU.matcher(n).matches()) {
            throw NayraException.solicitudInvalida("CELULAR_INVALIDO");
        }
        return new Celular(n);
    }
}
