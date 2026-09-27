package upc.pe.nayrabackend.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.UUID;

/**
 * Notificación (nayra.notificaciones; modelo de datos v4 §2.11), del servicio de negocio. Solo el tipo OPERACION:
 * avisa al propietario de la cuenta de destino de una transferencia. El nombre del emisor y el monto no se guardan
 * en columnas propias; solo forman parte del texto generado.
 */
@Entity
@Table(name = "notificaciones", schema = "nayra")
public class Notificaciones {

    public enum Tipo { OPERACION }

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "destinatario_id", nullable = false)
    private UUID destinatarioId;

    @Column(name = "operacion_id", nullable = false)
    private UUID operacionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 30)
    private Tipo tipo = Tipo.OPERACION;

    @Column(name = "titulo", nullable = false, length = 150)
    private String titulo;

    @Column(name = "contenido", nullable = false, columnDefinition = "text")
    private String contenido;

    @Column(name = "leida", nullable = false)
    private boolean leida;

    @Column(name = "fecha_generacion", nullable = false)
    private Instant fechaGeneracion;

    @Column(name = "fecha_lectura")
    private Instant fechaLectura;

    protected Notificaciones() {
    }

    public Notificaciones(String id, String destinatarioId, String operacionId, String titulo, String contenido,
                          Instant fechaGeneracion) {
        this.id = Identificadores.uuid(id);
        this.destinatarioId = Identificadores.uuid(destinatarioId);
        this.operacionId = Identificadores.uuid(operacionId);
        this.titulo = titulo;
        this.contenido = contenido;
        this.fechaGeneracion = fechaGeneracion;
    }

    /**
     * Texto de una transferencia recibida (v4 §2.11): nombres completos, las 3 primeras letras de los apellidos y
     * "...", y el monto en soles. Ejemplo: "Juan Per... te realizó una transferencia de S/ 150.00."
     */
    public static String contenidoTransferencia(String nombres, String apellidos, BigDecimal monto) {
        String a = apellidos.strip();
        int corte = a.offsetByCodePoints(0, Math.min(3, a.codePointCount(0, a.length())));
        return nombres.strip() + " " + a.substring(0, corte) + "... te realizó una transferencia de S/ "
                + monto.setScale(2, RoundingMode.UNNECESSARY).toPlainString() + ".";
    }

    public String getId() { return Identificadores.texto(id); }
    public String getDestinatarioId() { return Identificadores.texto(destinatarioId); }
    public String getOperacionId() { return Identificadores.texto(operacionId); }
    public Tipo getTipo() { return tipo; }
    public String getTitulo() { return titulo; }
    public String getContenido() { return contenido; }
    public synchronized boolean isLeida() { return leida; }
    public Instant getFechaGeneracion() { return fechaGeneracion; }
    public synchronized Instant getFechaLectura() { return fechaLectura; }

    public synchronized void marcarLeida(Instant ahora) {
        if (!leida) {
            leida = true;
            fechaLectura = ahora;
        }
    }
}
