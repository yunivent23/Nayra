package upc.pe.nayrabackend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import upc.pe.nayrabackend.entities.Dispositivos;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface IDispositivosRepositoritory extends JpaRepository<Dispositivos, Long> {

    Optional<Dispositivos> findByIdPublicoAndEstado(UUID idPublico, String estado);

    Optional<Dispositivos> findByUsuarioIdAndEstado(Long usuarioId, String estado);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update Dispositivos d set d.estado = 'REVOCADO', d.fechaRevocacion = :ahora " +
            "where d.usuario.id = :usuarioId and d.estado = 'ACTIVO'")
    int revocarActivos(@Param("usuarioId") Long usuarioId, @Param("ahora") OffsetDateTime ahora);
}
