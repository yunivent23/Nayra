package upc.pe.nayrabackend.entities;

import jakarta.persistence.*;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.UUID;

/** Desafío de voz y nonce de un solo uso (D-037, D-041, D-048). */
@Entity
@Table(name = "desafios_autenticacion")
public class DesafioAutenticacion implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "id_publico", nullable = false, unique = true, updatable = false)
    private UUID idPublico;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Users usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dispositivo_id")
    private Dispositivos dispositivo;

    @Column(name = "proposito", length = 30, nullable = false)
    private String proposito;

    @Column(name = "elementos", length = 200)
    private String elementos;

    @Column(name = "nonce", length = 64, nullable = false, unique = true)
    private String nonce;

    @Column(name = "fecha_emision", nullable = false)
    private OffsetDateTime fechaEmision;

    @Column(name = "fecha_expiracion", nullable = false)
    private OffsetDateTime fechaExpiracion;

    @Column(name = "fecha_uso")
    private OffsetDateTime fechaUso;

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

    public String getProposito() {
        return proposito;
    }

    public void setProposito(String proposito) {
        this.proposito = proposito;
    }

    public String getElementos() {
        return elementos;
    }

    public void setElementos(String elementos) {
        this.elementos = elementos;
    }

    public String getNonce() {
        return nonce;
    }

    public void setNonce(String nonce) {
        this.nonce = nonce;
    }

    public OffsetDateTime getFechaEmision() {
        return fechaEmision;
    }

    public void setFechaEmision(OffsetDateTime fechaEmision) {
        this.fechaEmision = fechaEmision;
    }

    public OffsetDateTime getFechaExpiracion() {
        return fechaExpiracion;
    }

    public void setFechaExpiracion(OffsetDateTime fechaExpiracion) {
        this.fechaExpiracion = fechaExpiracion;
    }

    public OffsetDateTime getFechaUso() {
        return fechaUso;
    }

    public void setFechaUso(OffsetDateTime fechaUso) {
        this.fechaUso = fechaUso;
    }
}
