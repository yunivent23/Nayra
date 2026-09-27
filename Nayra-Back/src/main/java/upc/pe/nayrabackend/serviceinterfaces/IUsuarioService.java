package upc.pe.nayrabackend.serviceinterfaces;

import upc.pe.nayrabackend.dtos.UsuarioDTOs.DatosPropios;
import upc.pe.nayrabackend.entities.Usuario;

/** Cuenta de acceso (USUARIOS): consulta y cambios de estado. */
public interface IUsuarioService {

    Usuario obtener(String usuarioId);

    /** Datos propios y estado de la cuenta de acceso (HU-09, HU-15). Nunca incluye el hash del PIN. */
    DatosPropios datosPropios(String usuarioId);

    /**
     * Bloquea la cuenta de acceso y revoca sus sesiones (D-040, D-044).
     *
     * @param actorId administrador que bloquea, o null cuando lo hace el sistema (intentos agotados)
     */
    void bloquear(String usuarioId, String actorId, String motivo);

    /** Desbloqueo por el administrador (HU-19); reinicia los intentos de PIN (PROVISIONAL, P-11). */
    void desbloquear(String usuarioId, String actorId);
}
