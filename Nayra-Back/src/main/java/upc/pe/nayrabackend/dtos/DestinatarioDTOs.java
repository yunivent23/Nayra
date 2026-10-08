package upc.pe.nayrabackend.dtos;

import java.util.List;

/**
 * Búsqueda del destinatario de una transferencia por celular (G-1, modificada el 2026-09-28; HU-69).
 * Organización de la API PROVISIONAL hasta docs/04_API.md (D-014).
 */
public final class DestinatarioDTOs {

    private DestinatarioDTOs() {
    }

    /** Celular del destinatario tal como lo escribió el remitente; el backend lo normaliza. */
    public record SolicitudBusquedaDestinatario(String celular) {
    }

    /**
     * Celulares de la agenda del teléfono (D-042, 2026-10-08), como máximo {@code IOperacionesService.MAX_CELULARES_POR_CONSULTA}.
     * Son datos de terceros: se usan solo para resolver la consulta y no se guardan ni se registran.
     */
    public record SolicitudBusquedaMultiple(List<String> celulares) {
    }

    /** Un contacto de la agenda que puede recibir una transferencia: su celular canónico y el nombre parcial. */
    public record DestinatarioDisponible(String celular, String nombreVisible) {
    }

    /** Solo las coincidencias válidas; los números sin cuenta que pueda recibir no aparecen. */
    public record DestinatariosDisponibles(List<DestinatarioDisponible> destinatarios) {
    }

    /**
     * Solo lo necesario para que el remitente confirme a quién transfiere: primer nombre y las tres primeras letras
     * del primer apellido ("María Per..."). Sin identificadores internos, documento, celular completo ni estado.
     * Significa "existe una cuenta Nayra asociada a este número", no que la persona sea titular de la línea.
     */
    public record DestinatarioEncontrado(String nombreVisible) {
    }
}
