package upc.pe.nayrabackend.entities;

import jakarta.persistence.*;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.UUID;

/** Dispositivo vinculado al usuario (D-041). Un único dispositivo ACTIVO por usuario. */
@Entity
@Table(name = "dispositivos")
public class Dispositivos implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "id_publico", nullable = false, unique = true, updatable = false)
    private UUID idPublico;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Users usuario;

    @Column(name = "plataforma", length = 30, nullable = false)
    private String plataforma;

    @Column(name = "nombre", length = 100)
    private String nombre;

    // Clave pública EC P-256 (X.509 SubjectPublicKeyInfo, base64). La clave privada nunca sale del teléfono.
    @Column(name = "clave_publica", nullable = false, columnDefinition = "TEXT")
    private String clavePublica;

    @Column(name = "algoritmo", length = 30, nullable = false)
    private String algoritmo;

    @Column(name = "estado", length = 20, nullable = false)
    private String estado;

    @Column(name = "fecha_registro", nullable = false)
    private OffsetDateTime fechaRegistro;

    @Column(name = "fecha_revocacion")
    private OffsetDateTime fechaRevocacion;

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

    public String getPlataforma() {
        return plataforma;
    }

    public void setPlataforma(String plataforma) {
        this.plataforma = plataforma;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getClavePublica() {
        return clavePublica;
    }

    public void setClavePublica(String clavePublica) {
        this.clavePublica = clavePublica;
    }

    public String getAlgoritmo() {
        return algoritmo;
    }

    public void setAlgoritmo(String algoritmo) {
        this.algoritmo = algoritmo;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public OffsetDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(OffsetDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public OffsetDateTime getFechaRevocacion() {
        return fechaRevocacion;
    }

    public void setFechaRevocacion(OffsetDateTime fechaRevocacion) {
        this.fechaRevocacion = fechaRevocacion;
    }
}
