package upc.pe.nayrabackend.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

/**
 * Entidad bancaria simulada (nayra.entidades_bancarias; D-026): catálogo con id y nombre, sin gestión por el
 * administrador (D-033). Tabla física según D-051 (03 §17).
 */
@Entity
@Table(name = "entidades_bancarias", schema = "nayra")
public class EntidadBancaria {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    protected EntidadBancaria() {
    }

    public EntidadBancaria(String id, String nombre) {
        this.id = Identificadores.uuid(id);
        this.nombre = nombre;
    }

    public String id() { return Identificadores.texto(id); }
    public String nombre() { return nombre; }
}
