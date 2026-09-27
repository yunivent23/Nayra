package upc.pe.nayrabackend.entities;

import java.time.Instant;

/**
 * Cuenta de acceso (USUARIOS, 03 §16.1).
 *
 * PROVISIONAL (D-051): objeto de dominio sin mapeo JPA; nombres físicos, tipos e identificadores
 * se fijarán con la estrategia de migraciones. El contador de intentos vive aquí mientras el
 * detalle de D-044 (contador, ventana, reinicio) siga pendiente.
 * La referencia biométrica NO forma parte de esta entidad (D-013, 03 §16.11).
 */
public class Usuario {

    /** Estados aprobados (D-040, D-044). Otros estados siguen pendientes (AG-05). */
    public enum Estado { ACTIVA, BLOQUEADA }

    private final String id;
    private final String dni;
    private final String nombres;
    private final String apellidos;
    private final String celular;
    private final String pinHash;
    private final Rol rol;
    private final Instant fechaCreacion;
    private Estado estado = Estado.ACTIVA;
    private int intentosFallidos;
    private Instant fechaActualizacion;

    public Usuario(String id, String dni, String nombres, String apellidos, String celular, String pinHash,
                   Rol rol, Instant fechaCreacion) {
        this.id = id;
        this.dni = dni;
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.celular = celular;
        this.pinHash = pinHash;
        this.rol = rol;
        this.fechaCreacion = fechaCreacion;
        this.fechaActualizacion = fechaCreacion;
    }

    public String getId() { return id; }
    public String getDni() { return dni; }
    public String getNombres() { return nombres; }
    public String getApellidos() { return apellidos; }
    public String getCelular() { return celular; }
    public String getPinHash() { return pinHash; }
    public Rol getRol() { return rol; }
    public Instant getFechaCreacion() { return fechaCreacion; }
    public synchronized Estado getEstado() { return estado; }
    public synchronized int getIntentosFallidos() { return intentosFallidos; }
    public synchronized Instant getFechaActualizacion() { return fechaActualizacion; }

    public synchronized int registrarFallo() { return ++intentosFallidos; }

    public synchronized void reiniciarIntentos() { intentosFallidos = 0; }

    public synchronized void bloquear(Instant ahora) {
        estado = Estado.BLOQUEADA;
        fechaActualizacion = ahora;
    }

    /** Desbloqueo por el administrador (HU-19): reinicia el contador de intentos. */
    public synchronized void desbloquear(Instant ahora) {
        estado = Estado.ACTIVA;
        intentosFallidos = 0;
        fechaActualizacion = ahora;
    }
}
