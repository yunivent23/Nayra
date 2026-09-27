package upc.pe.nayrabackend.serviceinterfaces;

import upc.pe.nayrabackend.entities.Rol;

import java.util.Optional;

/**
 * Sesiones (D-018, D-037, D-040).
 * APROBADO: cierre automático tras 5 minutos de inactividad controlado en el servidor, sin aviso.
 * PROVISIONAL (autorizado para el prototipo el 2026-09-27; no es la decisión definitiva de D-018): token opaco
 * aleatorio enviado en "Authorization: Bearer", del que solo se guarda el hash; por ahora sin duración máxima
 * absoluta ni renovación.
 * Reemplazar esta implementación cuando se cierre D-018.
 */
public interface ISesionesService {

    /** Sesión válida resuelta a partir del token presentado. */
    record SesionActiva(String sesionId, String usuarioId, String dispositivoId, Rol rol) {
    }

    /** Crea la sesión tras una autenticación completa y devuelve el token (solo esta vez). */
    String crear(String usuarioId, String dispositivoId);

    /** Valida el token y registra la actividad; vacío si no existe, fue revocada o venció por inactividad. */
    Optional<SesionActiva> validar(String token);

    /** Cierre de sesión por el usuario (HU-13). */
    void cerrar(String sesionId);

    /** Revoca todas las sesiones del usuario (bloqueo de cuenta o revocación del dispositivo, D-040). */
    void revocarDeUsuario(String usuarioId, String motivo);
}
