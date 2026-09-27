package upc.pe.nayrabackend.dtos;

/** DTOs del registro inicial asistido (D-052). Organización PROVISIONAL hasta docs/04_API.md (D-014). */
public final class RegistroDTOs {

    private RegistroDTOs() {
    }

    /**
     * Paso 3: documento de identidad proporcionado durante la asistencia (DNI o CE, modelo de datos v4 §2.1).
     * Si no se indica el tipo, se asume DNI (PROVISIONAL, D-014).
     */
    public record SolicitudInicioRegistro(String tipoDocumentoIdentidad, String numeroDocumento) {
    }

    /**
     * Pasos 4–5: datos del registro de identidad simulado y código de un solo uso para continuar en el
     * celular de la persona (mecanismo PROVISIONAL del prototipo, no decisión de D-052).
     */
    public record RegistroIniciado(String codigoRegistro, String nombres, String apellidos) {
    }

    /** Datos que la persona confirma en su celular (paso 7). */
    public record DatosParaConfirmar(String nombres, String apellidos, String paso) {
    }

    /** Pasos 7–10: confirmación de datos, celular, PIN y clave pública del dispositivo. */
    public record SolicitudDatosRegistro(Boolean confirmaDatos, String celular, String pin, String clavePublicaDispositivo) {
    }

    public record EstadoRegistro(String paso) {
    }

    /** Paso 13: confirmación de la cuenta de acceso (HU-04). */
    public record RegistroFinalizado(String usuarioId, String dispositivoId) {
    }
}
