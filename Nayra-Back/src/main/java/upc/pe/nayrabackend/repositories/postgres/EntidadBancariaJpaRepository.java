package upc.pe.nayrabackend.repositories.postgres;

import org.springframework.data.jpa.repository.JpaRepository;
import upc.pe.nayrabackend.entities.EntidadBancaria;

import java.util.UUID;

interface EntidadBancariaJpaRepository extends JpaRepository<EntidadBancaria, UUID> {
}
