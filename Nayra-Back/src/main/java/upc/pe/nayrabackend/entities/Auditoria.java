package upc.pe.nayrabackend.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.net.InetAddress;
import java.time.Instant;
import java.util.UUID;

/**
 * Evento de auditoría (nayra.auditoria, 03 §16.9 y §17; D-041). Solo inserción: no se modifica ni se elimina.
 * Nunca contiene PIN, audio, embeddings, puntajes biométricos ni secretos (03 §16.9, D-013, D-061).
 * La columna ip existe (D-051) pero la aplicación todavía no la captura; el catálogo definitivo de eventos
 * y campos sigue pendiente (D-019).
 *
 * @param actorId           usuario que ejecuta la acción (null si la ejecuta el sistema o aún no hay cuenta)
 * @param usuarioAfectadoId usuario sobre el que recae la acción, cuando corresponde
 * @param accion            código de la acción (p. ej. AUTENTICACION_VOZ, ADMIN_BLOQUEO)
 * @param motivo            código del motivo del fallo o del detalle, nunca texto libre
 */
@Entity
@Table(name = "auditoria", schema = "nayra")
public class Auditoria {

    /** Valores de la propuesta original (03 §3.9), adoptados el 2026-09-27 en lugar de EXITO/FALLO. */
    public enum Resultado { EXITOSO, FALLIDO }

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "fecha", nullable = false)
    private Instant fecha;

    @Column(name = "actor_id")
    private UUID actorId;

    @Column(name = "usuario_afectado_id")
    private UUID usuarioAfectadoId;

    @Column(name = "accion", nullable = false, length = 50)
    private String accion;

    @Enumerated(EnumType.STRING)
    @Column(name = "resultado", nullable = false, length = 10)
    private Resultado resultado;

    @Column(name = "motivo", length = 50)
    private String motivo;

    @JdbcTypeCode(SqlTypes.INET)
    @Column(name = "ip")
    private InetAddress ip;

    @Column(name = "dispositivo_id")
    private UUID dispositivoId;

    protected Auditoria() {
    }

    public Auditoria(String id, Instant fecha, String actorId, String usuarioAfectadoId, String accion,
                     Resultado resultado, String motivo, String dispositivoId) {
        this.id = Identificadores.uuid(id);
        this.fecha = fecha;
        this.actorId = Identificadores.uuid(actorId);
        this.usuarioAfectadoId = Identificadores.uuid(usuarioAfectadoId);
        this.accion = accion;
        this.resultado = resultado;
        this.motivo = motivo;
        this.dispositivoId = Identificadores.uuid(dispositivoId);
    }

    public String id() { return Identificadores.texto(id); }
    public Instant fecha() { return fecha; }
    public String actorId() { return Identificadores.texto(actorId); }
    public String usuarioAfectadoId() { return Identificadores.texto(usuarioAfectadoId); }
    public String accion() { return accion; }
    public Resultado resultado() { return resultado; }
    public String motivo() { return motivo; }
    public InetAddress ip() { return ip; }
    public String dispositivoId() { return Identificadores.texto(dispositivoId); }

    @Override
    public String toString() {
        return "Auditoria[id=" + id + ", fecha=" + fecha + ", actorId=" + actorId + ", usuarioAfectadoId=" + usuarioAfectadoId
                + ", accion=" + accion + ", resultado=" + resultado + ", motivo=" + motivo + ", ip=" + ip
                + ", dispositivoId=" + dispositivoId + "]";
    }
}
