package upc.pe.nayrabackend.repositories;

import upc.pe.nayrabackend.entities.Sesiones;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Puerto de persistencia de SESIONES (servicio de autenticación, v4 §2.7). Adaptador: PostgreSQL.
 *
 * Concurrencia (E-02): una sesión solo se da de alta con {@link #crear}. Después cambia únicamente con sentencias
 * condicionales ({@link #registrarAcceso}, {@link #revocar}, {@link #revocarDeUsuario}) que exigen
 * {@code fecha_revocacion IS NULL}. Ninguna guarda una copia completa leída antes, así que una revocación no puede
 * ser sobrescrita por una petición concurrente y una sesión revocada nunca vuelve a estar vigente.
 */
public interface ISesionesRepository {

    /** Alta de una sesión nueva. Rechaza un id que ya exista: no sirve para modificar una sesión. */
    void crear(Sesiones sesion);

    /** Sesión por su id, que es el claim jti del JWT. */
    Optional<Sesiones> porId(String id);

    /** Sesiones sin revocar del usuario. */
    List<Sesiones> vigentesDeUsuario(String usuarioId);

    /**
     * Registra la actividad solo si la sesión sigue vigente y su último acceso no es anterior a
     * {@code limiteInactividad} (5 minutos, D-018). La fecha nunca retrocede. Devuelve false si la sesión ya estaba
     * revocada, vencida o no existe: en ese caso no cambia nada.
     */
    boolean registrarAcceso(String id, Instant ahora, Instant limiteInactividad);

    /** Revoca la sesión si seguía vigente. Devuelve true solo para la llamada que la revocó. */
    boolean revocar(String id, Instant ahora);

    /** Revoca todas las sesiones vigentes del usuario en una sola sentencia. Devuelve cuántas revocó. */
    int revocarDeUsuario(String usuarioId, Instant ahora);
}
