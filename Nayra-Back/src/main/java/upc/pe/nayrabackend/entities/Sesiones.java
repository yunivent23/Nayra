package upc.pe.nayrabackend.entities;

import java.time.Instant;

/**
 * Sesión (SESIONES, 03 §16.6). Se crea solo tras dispositivo + PIN + desafío + anti-spoofing + 1:1 (D-037).
 * Se cierra tras 5 minutos de inactividad controlados en el servidor (D-018, aprobado) y se revoca al
 * bloquear la cuenta o revocar el dispositivo (D-040).
 *
 * PROVISIONAL (D-018): el formato del token, su almacenamiento, la duración máxima absoluta y la
 * renovación siguen pendientes. Solo se guarda el hash del token, nunca el token.
 */
public class Sesiones {

    private final String id;
    private final String tokenHash;
    private final String usuarioId;
    private final String dispositivoId;
    private final Instant fechaCreacion;
    private Instant ultimoAcceso;
    private Instant fechaRevocacion;
    private String motivoRevocacion;

    public Sesiones(String id, String tokenHash, String usuarioId, String dispositivoId, Instant fechaCreacion) {
        this.id = id;
        this.tokenHash = tokenHash;
        this.usuarioId = usuarioId;
        this.dispositivoId = dispositivoId;
        this.fechaCreacion = fechaCreacion;
        this.ultimoAcceso = fechaCreacion;
    }

    public String getId() { return id; }
    public String getTokenHash() { return tokenHash; }
    public String getUsuarioId() { return usuarioId; }
    public String getDispositivoId() { return dispositivoId; }
    public Instant getFechaCreacion() { return fechaCreacion; }
    public synchronized Instant getUltimoAcceso() { return ultimoAcceso; }
    public synchronized Instant getFechaRevocacion() { return fechaRevocacion; }
    public synchronized String getMotivoRevocacion() { return motivoRevocacion; }
    public synchronized boolean vigente() { return fechaRevocacion == null; }

    public synchronized void registrarAcceso(Instant ahora) { ultimoAcceso = ahora; }

    public synchronized void revocar(Instant ahora, String motivo) {
        if (fechaRevocacion == null) {
            fechaRevocacion = ahora;
            motivoRevocacion = motivo;
        }
    }
}
