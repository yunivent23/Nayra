package upc.pe.nayrabackend.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Operación simulada (nayra.operaciones; modelo de datos v4 §2.8), del servicio de negocio. Entorno bancario
 * simulado: no es una transacción real (D-021, D-022).
 *
 * Solo transferencias en soles por el canal móvil, de S/ 0.01 a S/ 499.99, entre dos cuentas distintas, con un
 * código de referencia de 6 dígitos único. La cuenta de destino se guarda por su id interno: qué dato la identifica
 * en la interfaz de HU-69 es G-1, PENDIENTE BLOQUEANTE, y no se decide aquí.
 */
@Entity
@Table(name = "operaciones", schema = "nayra")
public class Operaciones {

    public enum Tipo { TRANSFERENCIA }

    public enum Estado { EXITOSO, FALLIDO, CANCELADO }

    public enum Canal { MOVIL }

    public static final String MONEDA = "PEN";
    /** Límite superior excluido (v4 §2.8: {@code monto > 0 AND monto < 500}). */
    public static final BigDecimal MONTO_LIMITE = new BigDecimal("500");
    /** Escala y precisión de la columna {@code monto numeric(15,2)} (v4 §2.8, V010). */
    public static final int MONTO_ESCALA = 2;
    public static final int MONTO_PRECISION = 15;

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "cuenta_origen_id", nullable = false)
    private UUID cuentaOrigenId;

    @Column(name = "cuenta_destino_id", nullable = false)
    private UUID cuentaDestinoId;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 30)
    private Tipo tipo;

    @Column(name = "monto", nullable = false, precision = 15, scale = 2)
    private BigDecimal monto;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "moneda", nullable = false, length = 3)
    private String moneda = MONEDA;

    @Column(name = "detalle", length = 255)
    private String detalle;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "codigo_referencia", nullable = false, length = 6)
    private String codigoReferencia;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private Estado estado;

    @Enumerated(EnumType.STRING)
    @Column(name = "canal", nullable = false, length = 10)
    private Canal canal = Canal.MOVIL;

    @Column(name = "fecha", nullable = false)
    private Instant fecha;

    @Column(name = "fecha_actualizacion", nullable = false)
    private Instant fechaActualizacion;

    protected Operaciones() {
    }

    public Operaciones(String id, String cuentaOrigenId, String cuentaDestinoId, BigDecimal monto, String detalle,
                       String codigoReferencia, Estado estado, Instant fecha) {
        if (Objects.equals(cuentaOrigenId, cuentaDestinoId)) {
            throw new IllegalArgumentException("CUENTAS_IGUALES");
        }
        monto = validarMonto(monto);
        this.id = Identificadores.uuid(id);
        this.cuentaOrigenId = Identificadores.uuid(cuentaOrigenId);
        this.cuentaDestinoId = Identificadores.uuid(cuentaDestinoId);
        this.tipo = Tipo.TRANSFERENCIA;
        this.monto = monto;
        this.detalle = detalle;
        this.codigoReferencia = codigoReferencia;
        this.estado = estado;
        this.fecha = fecha;
        this.fechaActualizacion = fecha;
    }

    /**
     * Valida el monto en la aplicación antes de persistir (E-03), con la definición de v4 §2.8: {@code numeric(15,2)} y
     * {@code monto > 0 AND monto < 500}. Un monto que no cabe exactamente en 2 decimales se rechaza
     * (MONTO_ESCALA_INVALIDA) en lugar de dejar que PostgreSQL lo redondee; los ceros finales no cuentan
     * (150.000 = 150.00). Devuelve el monto con escala 2, sin redondear.
     */
    public static BigDecimal validarMonto(BigDecimal monto) {
        if (monto == null) {
            throw new IllegalArgumentException("MONTO_FUERA_DE_RANGO");
        }
        BigDecimal normalizado = monto.stripTrailingZeros();
        if (normalizado.scale() > MONTO_ESCALA) {
            throw new IllegalArgumentException("MONTO_ESCALA_INVALIDA");
        }
        BigDecimal exacto = normalizado.setScale(MONTO_ESCALA);   // sin redondeo: la escala ya es <= 2
        if (exacto.precision() > MONTO_PRECISION || exacto.signum() <= 0 || exacto.compareTo(MONTO_LIMITE) >= 0) {
            throw new IllegalArgumentException("MONTO_FUERA_DE_RANGO");
        }
        return exacto;
    }

    /** Código de referencia de 6 dígitos (v4 §2.8). El UNIQUE de la BD rechaza un repetido y el llamador reintenta. */
    public static String nuevoCodigoReferencia(SecureRandom random) {
        return String.format("%06d", random.nextInt(1_000_000));
    }

    public String getId() { return Identificadores.texto(id); }
    public String getCuentaOrigenId() { return Identificadores.texto(cuentaOrigenId); }
    public String getCuentaDestinoId() { return Identificadores.texto(cuentaDestinoId); }
    public Tipo getTipo() { return tipo; }
    public BigDecimal getMonto() { return monto; }
    public String getMoneda() { return moneda; }
    public String getDetalle() { return detalle; }
    public String getCodigoReferencia() { return codigoReferencia; }
    public Estado getEstado() { return estado; }
    public Canal getCanal() { return canal; }
    public Instant getFecha() { return fecha; }
    public Instant getFechaActualizacion() { return fechaActualizacion; }
}
