package upc.pe.nayrabackend.dtos;

/** DTOs de la cuenta de acceso. Organización de la API PROVISIONAL hasta docs/04_API.md (D-014). */
public final class UsuarioDTOs {

    private UsuarioDTOs() {
    }

    /** Datos propios (HU-09) y estado de la cuenta de acceso (HU-15). Sin hash del PIN ni datos biométricos. */
    public record DatosPropios(String nombres, String apellidos, String celular, String rol, String estado,
                               boolean dispositivoActivo) {
    }
}
