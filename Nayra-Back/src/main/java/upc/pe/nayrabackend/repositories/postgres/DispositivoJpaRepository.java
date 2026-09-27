package upc.pe.nayrabackend.repositories.postgres;

import org.springframework.data.jpa.repository.JpaRepository;
import upc.pe.nayrabackend.entities.Dispositivos;

import java.util.Optional;
import java.util.UUID;

interface DispositivoJpaRepository extends JpaRepository<Dispositivos, UUID> {
    Optional<Dispositivos> findByUsuarioIdAndEstado(UUID usuarioId, Dispositivos.Estado estado);
}
