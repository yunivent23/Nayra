package upc.pe.nayrabackend.repositories.postgres;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import upc.pe.nayrabackend.entities.Sesiones;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

interface SesionJpaRepository extends JpaRepository<Sesiones, UUID> {

    List<Sesiones> findByUsuarioIdAndFechaRevocacionIsNull(UUID usuarioId);

    /** Actividad: solo sobre una sesión vigente y no vencida; la fecha nunca retrocede (E-02). */
    @Modifying
    @Query("update Sesiones s set s.fechaUltimoAcceso = case when s.fechaUltimoAcceso < :ahora then :ahora "
            + "else s.fechaUltimoAcceso end "
            + "where s.id = :id and s.fechaRevocacion is null and s.fechaUltimoAcceso >= :limite")
    int registrarAcceso(@Param("id") UUID id, @Param("ahora") Instant ahora, @Param("limite") Instant limite);

    /** Revocación: solo la primera escritura gana; nada vuelve a poner fecha_revocacion en NULL (E-02). */
    @Modifying
    @Query("update Sesiones s set s.fechaRevocacion = :ahora where s.id = :id and s.fechaRevocacion is null")
    int revocar(@Param("id") UUID id, @Param("ahora") Instant ahora);

    @Modifying
    @Query("update Sesiones s set s.fechaRevocacion = :ahora where s.usuarioId = :usuarioId and s.fechaRevocacion is null")
    int revocarDeUsuario(@Param("usuarioId") UUID usuarioId, @Param("ahora") Instant ahora);
}
