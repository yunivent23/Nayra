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
import java.time.Instant;
import java.util.UUID;

/**
 * Cuenta financiera simulada (nayra.cuentas, 03 §3.3, §16.4 y §17; D-025 a D-029, D-035).
 * El titular es una FK al registro de identidad simulado (una cuenta por titular); el propietario (usuario de
 * Nayra) es opcional hasta la vinculación durante el registro y único una vez vinculado.
 * Entorno bancario simulado: no representa cuentas reales (D-021, D-022). El administrador no modifica saldos
 * (D-041).
 * codigo_qr (v4 §2.5): fijo, guardado y único. PROVISIONAL (P-3): mientras el formato siga pendiente se guarda un
 * UUID v4 aleatorio en texto, sin datos personales. HU-123 y HU-124 no están implementadas.
 */
@Entity
@Table(name = "cuentas", schema = "nayra")
public class Cuentas {

    /** Valores de la propuesta original (03 §3.3). Su relación con los estados de la cuenta de acceso: AG-05. */
    public enum Estado { ACTIVA, BLOQUEADA, CERRADA }

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "titular_id", nullable = false)
    private UUID titularId;

    @Column(name = "propietario_id")
    private UUID propietarioId;

    @Column(name = "entidad_bancaria_id", nullable = false)
    private UUID entidadBancariaId;

    @Column(name = "codigo_cuenta", nullable = false, length = 30)
    private String codigoCuenta;

    /** PROVISIONAL (P-3): formato y tipo físico finales pendientes. */
    @Column(name = "codigo_qr", nullable = false, length = 64)
    private String codigoQr;

    @Column(name = "saldo", nullable = false, precision = 15, scale = 2)
    private BigDecimal saldo;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "moneda", nullable = false, length = 3)
    private String moneda;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private Estado estado = Estado.ACTIVA;

    @Column(name = "fecha_creacion", nullable = false)
    private Instant fechaCreacion;

    @Column(name = "fecha_actualizacion", nullable = false)
    private Instant fechaActualizacion;

    protected Cuentas() {
    }

    public Cuentas(String id, String titularId, String entidadBancariaId, String codigoCuenta,
                   BigDecimal saldo, String moneda, Instant fechaCreacion) {
        this.id = Identificadores.uuid(id);
        this.titularId = Identificadores.uuid(titularId);
        this.entidadBancariaId = Identificadores.uuid(entidadBancariaId);
        this.codigoCuenta = codigoCuenta;
        this.codigoQr = Identificadores.nuevo();   // PROVISIONAL (P-3)
        this.saldo = saldo;
        this.moneda = moneda;
        this.fechaCreacion = fechaCreacion;
        this.fechaActualizacion = fechaCreacion;
    }

    public String getId() { return Identificadores.texto(id); }
    public String getTitularId() { return Identificadores.texto(titularId); }
    public String getEntidadBancariaId() { return Identificadores.texto(entidadBancariaId); }
    public String getCodigoCuenta() { return codigoCuenta; }
    public String getCodigoQr() { return codigoQr; }
    public BigDecimal getSaldo() { return saldo; }
    public String getMoneda() { return moneda; }
    public Estado getEstado() { return estado; }
    public Instant getFechaCreacion() { return fechaCreacion; }
    public synchronized Instant getFechaActualizacion() { return fechaActualizacion; }
    public synchronized String getPropietarioUsuarioId() { return Identificadores.texto(propietarioId); }

    /** Vinculación única con la cuenta de acceso (D-028). */
    public synchronized void vincular(String usuarioId, Instant ahora) {
        UUID nuevo = Identificadores.uuid(usuarioId);
        if (propietarioId != null && !propietarioId.equals(nuevo)) {
            throw new IllegalStateException("CUENTA_FINANCIERA_YA_VINCULADA");
        }
        propietarioId = nuevo;
        fechaActualizacion = ahora;
    }
}
