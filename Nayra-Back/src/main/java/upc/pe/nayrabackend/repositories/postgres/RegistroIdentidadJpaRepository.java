package upc.pe.nayrabackend.repositories.postgres;

import org.springframework.data.jpa.repository.JpaRepository;
import upc.pe.nayrabackend.entities.RegistroIdentidadSimulado;
import upc.pe.nayrabackend.entities.TipoDocumentoIdentidad;

import java.util.Optional;
import java.util.UUID;

interface RegistroIdentidadJpaRepository extends JpaRepository<RegistroIdentidadSimulado, UUID> {
    Optional<RegistroIdentidadSimulado> findByTipoDocumentoIdentidadAndNumeroDocumento(TipoDocumentoIdentidad tipo, String numero);
}
