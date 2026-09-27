package upc.pe.nayrabackend.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * Sesión (nayra.sesiones; modelo de datos v4 §2.7 y §4.1), del servicio de autenticación. El id es el claim
 * {@code jti} del JWT que recibe la app. No se guarda el JWT, su hash, un refresh token, una expiración absoluta ni un
 * motivo de revocación.
 *
 * Válida mientras {@code fecha_revocacion} sea nula y no hayan pasado más de 5 minutos desde el último acceso
 * (D-018), comprobado en el servidor. Se revoca al cerrar sesión, al bloquear la cuenta, al revocar el dispositivo
 * o al detectar la inactividad. Sin renovación ni duración máxima absoluta.
 */
@Entity
@Table(name = "sesiones", schema = "nayra")
public class Sesiones {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "usuario_id", nullable = false)
    private UUID usuarioId;

    @Column(name = "dispositivo_id", nullable = false)
    private UUID dispositivoId;

    @Column(name = "fecha_creacion", nullable = false)
    private Instant fechaCreacion;

    @Column(name = "fecha_ultimo_acceso", nullable = false)
    private Instant fechaUltimoAcceso;

    @Column(name = "fecha_revocacion")
    private Instant fechaRevocacion;

    protected Sesiones() {
    }

    public Sesiones(String id, String usuarioId, String dispositivoId, Instant fechaCreacion) {
        this.id = Identificadores.uuid(id);
        this.usuarioId = Identificadores.uuid(usuarioId);
        this.dispositivoId = Identificadores.uuid(dispositivoId);
        this.fechaCreacion = fechaCreacion;
        this.fechaUltimoAcceso = fechaCreacion;
    }

    public String getId() { return Identificadores.texto(id); }
    public String getUsuarioId() { return Identificadores.texto(usuarioId); }
    public String getDispositivoId() { return Identificadores.texto(dispositivoId); }
    public Instant getFechaCreacion() { return fechaCreacion; }
    public synchronized Instant getFechaUltimoAcceso() { return fechaUltimoAcceso; }
    public synchronized Instant getFechaRevocacion() { return fechaRevocacion; }
    public synchronized boolean vigente() { return fechaRevocacion == null; }

    public synchronized void registrarAcceso(Instant ahora) {
        if (ahora.isAfter(fechaUltimoAcceso)) {
            fechaUltimoAcceso = ahora;
        }
    }

    /** Devuelve true si la sesión estaba vigente y quedó revocada ahora. */
    public synchronized boolean revocar(Instant ahora) {
        if (fechaRevocacion != null) {
            return false;
        }
        fechaRevocacion = ahora;
        return true;
    }
}
