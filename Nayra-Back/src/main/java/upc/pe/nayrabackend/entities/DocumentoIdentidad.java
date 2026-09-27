package upc.pe.nayrabackend.entities;

import upc.pe.nayrabackend.excepciones.NayraException;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Lectura y validación del documento de identidad recibido del exterior (modelo de datos v4 §2.1 y §2.3).
 *
 * PROVISIONAL (P-2, formato del número por tipo, PENDIENTE NO BLOQUEANTE):
 * - DNI: 8 dígitos, como hasta ahora (D-035 no fija un formato);
 * - CE: de 1 a 30 letras o dígitos (longitud de la columna), sin otra regla hasta cerrar P-2.
 */
public record DocumentoIdentidad(TipoDocumentoIdentidad tipo, String numero) {

    private static final Pattern DNI = Pattern.compile("\\d{8}");
    private static final Pattern CE = Pattern.compile("[A-Za-z0-9]{1,30}");

    /** Tipo recibido; sin tipo se asume DNI (PROVISIONAL: compatibilidad con la API anterior, D-014). */
    public static TipoDocumentoIdentidad tipo(String tipo) {
        if (tipo == null || tipo.isBlank()) {
            return TipoDocumentoIdentidad.DNI;
        }
        try {
            return TipoDocumentoIdentidad.valueOf(tipo.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw NayraException.solicitudInvalida("TIPO_DOCUMENTO_INVALIDO");
        }
    }

    /** Documento válido o NayraException (400) con TIPO_DOCUMENTO_INVALIDO o DOCUMENTO_INVALIDO. */
    public static DocumentoIdentidad leer(String tipo, String numero) {
        TipoDocumentoIdentidad t = tipo(tipo);
        String n = numero == null ? null : numero.trim();
        Pattern formato = t == TipoDocumentoIdentidad.DNI ? DNI : CE;
        if (n == null || !formato.matcher(n).matches()) {
            throw NayraException.solicitudInvalida("DOCUMENTO_INVALIDO");
        }
        return new DocumentoIdentidad(t, t == TipoDocumentoIdentidad.CE ? n.toUpperCase(Locale.ROOT) : n);
    }
}
