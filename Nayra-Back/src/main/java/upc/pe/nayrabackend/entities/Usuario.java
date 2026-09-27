package upc.pe.nayrabackend.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * Cuenta de acceso (nayra.usuarios; modelo de datos v4 §2.1), del servicio de negocio. Tabla física según D-051 y
 * rol como valor fijo según D-009 (opción A).
 *
 * El hash del PIN y el contador de intentos NO están aquí: pertenecen a {@link Credenciales} (servicio de
 * autenticación, v4 §2.2). La referencia biométrica tampoco (D-013).
 */
@Entity
@Table(name = "usuarios", schema = "nayra")
public class Usuario {

    /** Estados de la cuenta de acceso (v4 §2.1). Negocio los posee aunque el bloqueo lo origine Autenticación. */
    public enum Estado { ACTIVO, BLOQUEADO, INACTIVO }

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_documento_identidad", nullable = false, length = 20)
    private TipoDocumentoIdentidad tipoDocumentoIdentidad;

    @Column(name = "numero_documento", nullable = false, length = 30)
    private String numeroDocumento;

    @Column(name = "nombres", nullable = false, length = 100)
    private String nombres;

    @Column(name = "apellidos", nullable = false, length = 100)
    private String apellidos;

    @Column(name = "numero_celular", nullable = false, length = 20)
    private String celular;

    @Enumerated(EnumType.STRING)
    @Column(name = "rol", nullable = false, length = 10)
    private Rol rol;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private Estado estado = Estado.ACTIVO;

    @Column(name = "fecha_creacion", nullable = false)
    private Instant fechaCreacion;

    @Column(name = "fecha_actualizacion", nullable = false)
    private Instant fechaActualizacion;

    protected Usuario() {
    }

    public Usuario(String id, TipoDocumentoIdentidad tipoDocumentoIdentidad, String numeroDocumento, String nombres,
                   String apellidos, String celular, Rol rol, Instant fechaCreacion) {
        this.id = Identificadores.uuid(id);
        this.tipoDocumentoIdentidad = tipoDocumentoIdentidad;
        this.numeroDocumento = numeroDocumento;
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.celular = celular;
        this.rol = rol;
        this.fechaCreacion = fechaCreacion;
        this.fechaActualizacion = fechaCreacion;
    }

    public String getId() { return Identificadores.texto(id); }
    public TipoDocumentoIdentidad getTipoDocumentoIdentidad() { return tipoDocumentoIdentidad; }
    public String getNumeroDocumento() { return numeroDocumento; }
    public String getNombres() { return nombres; }
    public String getApellidos() { return apellidos; }
    public String getCelular() { return celular; }
    public Rol getRol() { return rol; }
    public Instant getFechaCreacion() { return fechaCreacion; }
    public synchronized Estado getEstado() { return estado; }
    public synchronized Instant getFechaActualizacion() { return fechaActualizacion; }

    public synchronized void bloquear(Instant ahora) {
        estado = Estado.BLOQUEADO;
        fechaActualizacion = ahora;
    }

    /** Desbloqueo por el administrador (HU-19). El contador de intentos lo posee Autenticación (v4 §2.2, P-11). */
    public synchronized void desbloquear(Instant ahora) {
        estado = Estado.ACTIVO;
        fechaActualizacion = ahora;
    }
}
