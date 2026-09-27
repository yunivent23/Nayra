package upc.pe.nayrabackend.repositories.postgres;

import org.springframework.data.jpa.repository.JpaRepository;
import upc.pe.nayrabackend.entities.Cuentas;

import java.util.Optional;
import java.util.UUID;

interface CuentaJpaRepository extends JpaRepository<Cuentas, UUID> {
    Optional<Cuentas> findByTitularId(UUID titularId);
    Optional<Cuentas> findByPropietarioId(UUID propietarioId);
}
