package upc.pe.nayrabackend.repositories.postgres;

import org.springframework.data.jpa.repository.JpaRepository;
import upc.pe.nayrabackend.entities.Notificaciones;

import java.util.List;
import java.util.UUID;

interface NotificacionJpaRepository extends JpaRepository<Notificaciones, UUID> {
    List<Notificaciones> findByDestinatarioIdOrderByFechaGeneracionDesc(UUID destinatarioId);
}
