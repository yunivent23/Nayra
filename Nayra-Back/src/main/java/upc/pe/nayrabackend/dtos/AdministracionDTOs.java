package upc.pe.nayrabackend.dtos;

import java.time.Instant;

/**
 * DTOs de la API del administrador (D-041). Nunca incluyen el hash del PIN ni datos biométricos.
 * Organización PROVISIONAL hasta docs/04_API.md (D-014).
 */
public final class AdministracionDTOs {

    private AdministracionDTOs() {
    }

    /** HU-20, HU-21. */
    public record UsuarioResumen(String id, String tipoDocumentoIdentidad, String numeroDocumento, String nombres, String apellidos, String rol, String estado) {
    }

    /** HU-125. */
    public record DispositivoResumen(String id, String plataforma, String estado, Instant fechaVinculacion,
                                     Instant fechaRevocacion) {
    }

    /** HU-22, HU-101. */
    public record UsuarioDetalle(String id, String tipoDocumentoIdentidad, String numeroDocumento, String nombres, String apellidos, String celular, String rol,
                                 String estado, Instant fechaCreacion, DispositivoResumen dispositivoActivo) {
    }

    /** HU-89, HU-80, HU-94. */
    public record EventoAuditoriaDTO(Instant fecha, String accion, String resultado, String actorId,
                                     String usuarioAfectadoId, String motivo, String dispositivoId) {
    }
}
