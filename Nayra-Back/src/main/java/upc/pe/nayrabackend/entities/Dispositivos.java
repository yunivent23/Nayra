package upc.pe.nayrabackend.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.security.PublicKey;
import java.time.Instant;
import java.util.UUID;

/**
 * Dispositivo vinculado (nayra.dispositivos, 03 §16.5 y §17; D-039, D-048).
 * Solo se guarda la clave pública ECDSA P-256; la privada nunca sale del teléfono.
 * El id (PK) es también el identificador del dispositivo que la app firma junto al nonce; no se guarda
 * ningún identificador de hardware del teléfono (decisión del 2026-09-27).
 * Regla: como máximo un dispositivo ACTIVO por usuario (índice único parcial en la BD).
 */
@Entity
@Table(name = "dispositivos", schema = "nayra")
public class Dispositivos {

    public enum Estado { ACTIVO, REVOCADO }

    public static final String ALGORITMO = "ECDSA_P256_SHA256";

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "usuario_id", nullable = false)
    private UUID usuarioId;

    @Convert(converter = ClavePublicaConverter.class)
    @Column(name = "clave_publica", nullable = false)
    private PublicKey clavePublica;

    @Column(name = "algoritmo_clave", nullable = false, length = 30)
    private String algoritmoClave = ALGORITMO;

    @Column(name = "plataforma", nullable = false, length = 10)
    private String plataforma;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private Estado estado = Estado.ACTIVO;

    @Column(name = "fecha_vinculacion", nullable = false)
    private Instant fechaVinculacion;

    @Column(name = "fecha_revocacion")
    private Instant fechaRevocacion;

    protected Dispositivos() {
    }

    public Dispositivos(String id, String usuarioId, PublicKey clavePublica, String plataforma, Instant fechaVinculacion) {
        this.id = Identificadores.uuid(id);
        this.usuarioId = Identificadores.uuid(usuarioId);
        this.clavePublica = clavePublica;
        this.plataforma = plataforma;
        this.fechaVinculacion = fechaVinculacion;
    }

    public String getId() { return Identificadores.texto(id); }
    public String getUsuarioId() { return Identificadores.texto(usuarioId); }
    public PublicKey getClavePublica() { return clavePublica; }
    public String getAlgoritmoClave() { return algoritmoClave; }
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
