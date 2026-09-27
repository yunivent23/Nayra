package upc.pe.nayrabackend.repositories.postgres;

import org.springframework.data.jpa.repository.JpaRepository;
import upc.pe.nayrabackend.entities.Operaciones;

import java.util.Optional;
import java.util.UUID;

interface OperacionJpaRepository extends JpaRepository<Operaciones, UUID> {
    Optional<Operaciones> findByCodigoReferencia(String codigoReferencia);
}
