package upc.pe.nayrabackend.entities;

import jakarta.persistence.*;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.UUID;

/** Sesión con token opaco (D-042). */
@Entity
@Table(name = "sesiones")
public class Sesiones implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "id_publico", nullable = false, unique = true, updatable = false)
    private UUID idPublico;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Users usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dispositivo_id")
    private Dispositivos dispositivo;

    // SHA-256 (hex) del token opaco; el token nunca se guarda (D-042)
    @Column(name = "token_hash", length = 64, nullable = false, unique = true)
    private String tokenHash;

    @Column(name = "fecha_inicio", nullable = false)
    private OffsetDateTime fechaInicio;

    @Column(name = "ultimo_acceso", nullable = false)
    private OffsetDateTime ultimoAcceso;

    @Column(name = "fecha_cierre")
    private OffsetDateTime fechaCierre;

    @Column(name = "motivo_cierre", length = 30)
    private String motivoCierre;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UUID getIdPublico() {
        return idPublico;
    }

    public void setIdPublico(UUID idPublico) {
        this.idPublico = idPublico;
    }

    public Users getUsuario() {
        return usuario;
    }

    public void setUsuario(Users usuario) {
        this.usuario = usuario;
    }

    public Dispositivos getDispositivo() {
        return dispositivo;
    }

    public void setDispositivo(Dispositivos dispositivo) {
        this.dispositivo = dispositivo;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public void setTokenHash(String tokenHash) {
        this.tokenHash = tokenHash;
    }

    public OffsetDateTime getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(OffsetDateTime fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public OffsetDateTime getUltimoAcceso() {
        return ultimoAcceso;
    }

    public void setUltimoAcceso(OffsetDateTime ultimoAcceso) {
        this.ultimoAcceso = ultimoAcceso;
    }

    public OffsetDateTime getFechaCierre() {
        return fechaCierre;
    }

    public void setFechaCierre(OffsetDateTime fechaCierre) {
        this.fechaCierre = fechaCierre;
    }

    public String getMotivoCierre() {
        return motivoCierre;
    }

    public void setMotivoCierre(String motivoCierre) {
        this.motivoCierre = motivoCierre;
    }
}
