package upc.pe.nayrabackend.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

/**
 * Registro de identidad simulado (nayra.registro_identidad_simulado, 03 §16.3; D-035).
 * Datos ficticios del entorno controlado; no es una API real ni representa personas reales.
 * Consultarlo recupera datos, pero no prueba la identidad (D-052).
 * El id es la PK; (tipo de documento, número) es clave alternativa única (modelo de datos v4 §2.3).
 */
@Entity
@Table(name = "registro_identidad_simulado", schema = "nayra")
public class RegistroIdentidadSimulado {

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

    protected RegistroIdentidadSimulado() {
    }

    public RegistroIdentidadSimulado(String id, TipoDocumentoIdentidad tipoDocumentoIdentidad, String numeroDocumento,
                                     String nombres, String apellidos) {
        this.id = Identificadores.uuid(id);
        this.tipoDocumentoIdentidad = tipoDocumentoIdentidad;
        this.numeroDocumento = numeroDocumento;
        this.nombres = nombres;
        this.apellidos = apellidos;
    }

    public String id() { return Identificadores.texto(id); }
    public TipoDocumentoIdentidad tipoDocumentoIdentidad() { return tipoDocumentoIdentidad; }
    public String numeroDocumento() { return numeroDocumento; }
    public String nombres() { return nombres; }
    public String apellidos() { return apellidos; }
}
