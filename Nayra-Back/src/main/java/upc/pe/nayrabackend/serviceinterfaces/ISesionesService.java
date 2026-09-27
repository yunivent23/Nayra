package upc.pe.nayrabackend.serviceinterfaces;

import upc.pe.nayrabackend.entities.Rol;

import java.util.Optional;

/**
 * Sesiones (servicio de autenticación; D-018, D-037, D-040 y modelo de datos v4 §2.7 y §4.1).
 * La app recibe un JWT cuyo jti es el id de la fila de nayra.sesiones. Autenticación es la única autoridad sobre la
 * sesión: Negocio (hoy, FiltroSesion) le pregunta si es válida.
 * APROBADO: cierre tras 5 minutos de inactividad controlado en el servidor, sin aviso; sin renovación ni duración
 * máxima absoluta. PROVISIONAL (P-5): algoritmo, clave y claims del JWT.
 */
public interface ISesionesService {

    /** Sesión válida resuelta a partir del JWT presentado. */
    record SesionActiva(String sesionId, String usuarioId, String dispositivoId, Rol rol) {
    }

    /** Crea la sesión tras una autenticación completa y devuelve el JWT (solo esta vez). */
    String crear(String usuarioId, String dispositivoId);

    /** Valida el JWT contra la tabla y registra la actividad; vacío si no es válido, fue revocada o venció por inactividad. */
    Optional<SesionActiva> validar(String token);

    /** Cierre de sesión por el usuario (HU-13). */
    void cerrar(String sesionId);

    /** Revoca todas las sesiones del usuario (bloqueo de cuenta o revocación del dispositivo, D-040). */
    void revocarDeUsuario(String usuarioId);
}
