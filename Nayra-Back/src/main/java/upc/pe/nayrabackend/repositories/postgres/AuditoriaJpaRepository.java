package upc.pe.nayrabackend.repositories.postgres;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import upc.pe.nayrabackend.entities.Auditoria;

import java.util.UUID;

interface AuditoriaJpaRepository extends JpaRepository<Auditoria, UUID>, JpaSpecificationExecutor<Auditoria> {
}
