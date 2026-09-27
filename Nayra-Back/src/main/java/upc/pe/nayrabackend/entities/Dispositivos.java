package upc.pe.nayrabackend.entities;

import java.security.PublicKey;
import java.time.Instant;

/**
 * Dispositivo vinculado (DISPOSITIVOS, 03 §16.5; D-039, D-048).
 * Solo se guarda la clave pública ECDSA P-256; la privada nunca sale del teléfono.
 * Regla: como máximo un dispositivo ACTIVO por usuario.
 * PROVISIONAL (D-051): sin mapeo JPA.
 */
public class Dispositivos {

    public enum Estado { ACTIVO, REVOCADO }

    public static final String ALGORITMO = "ECDSA_P256_SHA256";

    private final String id;
    private final String usuarioId;
    private final PublicKey clavePublica;
    private final String plataforma;
    private final Instant fechaVinculacion;
    private Estado estado = Estado.ACTIVO;
    private Instant fechaRevocacion;

    public Dispositivos(String id, String usuarioId, PublicKey clavePublica, String plataforma, Instant fechaVinculacion) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.clavePublica = clavePublica;
        this.plataforma = plataforma;
        this.fechaVinculacion = fechaVinculacion;
    }

    public String getId() { return id; }
    public String getUsuarioId() { return usuarioId; }
    public PublicKey getClavePublica() { return clavePublica; }
    public String getPlataforma() { return plataforma; }
    public Instant getFechaVinculacion() { return fechaVinculacion; }
    public synchronized Estado getEstado() { return estado; }
    public synchronized Instant getFechaRevocacion() { return fechaRevocacion; }
    public synchronized boolean activo() { return estado == Estado.ACTIVO; }

    public synchronized void revocar(Instant ahora) {
        if (estado == Estado.ACTIVO) {
            estado = Estado.REVOCADO;
            fechaRevocacion = ahora;
        }
    }
}
