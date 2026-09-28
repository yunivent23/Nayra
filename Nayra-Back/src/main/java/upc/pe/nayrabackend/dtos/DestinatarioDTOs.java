package upc.pe.nayrabackend.dtos;

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
     * Solo lo necesario para que el remitente confirme a quién transfiere: primer nombre y las tres primeras letras
     * del primer apellido ("María Per..."). Sin identificadores internos, documento, celular completo ni estado.
     * Significa "existe una cuenta Nayra asociada a este número", no que la persona sea titular de la línea.
     */
    public record DestinatarioEncontrado(String nombreVisible) {
    }
}
