package upc.pe.nayrabackend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import upc.pe.nayrabackend.entities.DesafioAutenticacion;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface IDesafiosRepository extends JpaRepository<DesafioAutenticacion, Long> {

    @Query("select d from DesafioAutenticacion d left join fetch d.usuario left join fetch d.dispositivo " +
            "where d.idPublico = :idPublico")
    Optional<DesafioAutenticacion> findByIdPublico(@Param("idPublico") UUID idPublico);

    /** Consumo atómico de un solo uso (D-048): solo una solicitud concurrente obtiene 1. */
    @Modifying
    @Query("update DesafioAutenticacion d set d.fechaUso = :ahora " +
            "where d.id = :id and d.fechaUso is null and d.fechaExpiracion > :ahora")
    int consumir(@Param("id") Long id, @Param("ahora") OffsetDateTime ahora);
}
