package upc.pe.nayrabackend.repositories;

import upc.pe.nayrabackend.entities.RegistroIdentidadSimulado;
import upc.pe.nayrabackend.entities.TipoDocumentoIdentidad;

import java.util.Optional;

/** REGISTRO_IDENTIDAD_SIMULADO (D-035). Adaptador: PostgreSQL (D-051). */
public interface IRegistroIdentidadRepository {
    void guardar(RegistroIdentidadSimulado registro);
    Optional<RegistroIdentidadSimulado> porDocumento(TipoDocumentoIdentidad tipo, String numero);
}
