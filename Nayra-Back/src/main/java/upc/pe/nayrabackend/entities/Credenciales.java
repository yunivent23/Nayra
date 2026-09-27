package upc.pe.nayrabackend.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

/**
 * Credencial del PIN (nayra.credenciales; modelo de datos v4 §2.2, P-1 cerrado), del servicio de autenticación.
 * Una sola por usuario (UNIQUE usuario_id). El hash nunca sale de Autenticación ni se expone en la API de Negocio.
 *
 * Reglas (v4 §2.2): solo el PIN incorrecto incrementa el contador; al llegar al máximo (3) se bloquea la cuenta de
 * acceso; un PIN correcto lo devuelve a 0. Algoritmo del hash: D-047 PENDIENTE (BCrypt PROVISIONAL).
 */
@Entity
@Table(name = "credenciales", schema = "nayra")
public class Credenciales {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "usuario_id", nullable = false)
    private UUID usuarioId;

    @Column(name = "pin_hash", nullable = false, columnDefinition = "text")
    private String pinHash;

    @JdbcTypeCode(SqlTypes.SMALLINT)
    @Column(name = "intentos_fallidos", nullable = false)
    private int intentosFallidos;

    @Column(name = "fecha_creacion", nullable = false)
    private Instant fechaCreacion;

    @Column(name = "fecha_actualizacion", nullable = false)
    private Instant fechaActualizacion;

    protected Credenciales() {
    }

    public Credenciales(String id, String usuarioId, String pinHash, Instant fechaCreacion) {
        this.id = Identificadores.uuid(id);
        this.usuarioId = Identificadores.uuid(usuarioId);
        this.pinHash = pinHash;
        this.fechaCreacion = fechaCreacion;
        this.fechaActualizacion = fechaCreacion;
    }

    public String getId() { return Identificadores.texto(id); }
    public String getUsuarioId() { return Identificadores.texto(usuarioId); }
    public synchronized String getPinHash() { return pinHash; }
    public synchronized int getIntentosFallidos() { return intentosFallidos; }
    public Instant getFechaCreacion() { return fechaCreacion; }
    public synchronized Instant getFechaActualizacion() { return fechaActualizacion; }

    /** PIN incorrecto: incrementa el contador sin pasar del máximo y devuelve el nuevo valor. */
    public synchronized int registrarFallo(int maximo, Instant ahora) {
        intentosFallidos = Math.min(intentosFallidos + 1, maximo);
        fechaActualizacion = ahora;
        return intentosFallidos;
    }

    /** PIN correcto (o desbloqueo, PROVISIONAL P-11): el contador vuelve a 0. */
    public synchronized void reiniciarIntentos(Instant ahora) {
        if (intentosFallidos != 0) {
            intentosFallidos = 0;
            fechaActualizacion = ahora;
        }
    }
}
