package upc.pe.nayrabackend.repositories.postgres;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import upc.pe.nayrabackend.entities.Credenciales;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

interface CredencialJpaRepository extends JpaRepository<Credenciales, UUID> {

    Optional<Credenciales> findByUsuarioId(UUID usuarioId);

    /** Incremento atómico: PostgreSQL bloquea la fila, así que dos fallos simultáneos cuentan como dos (E-01). */
    @Modifying
    @Query("update Credenciales c set c.intentosFallidos = c.intentosFallidos + 1, c.fechaActualizacion = :ahora "
            + "where c.usuarioId = :usuarioId and c.intentosFallidos < :maximo")
    int incrementarIntentos(@Param("usuarioId") UUID usuarioId, @Param("maximo") int maximo, @Param("ahora") Instant ahora);

    @Query("select c.intentosFallidos from Credenciales c where c.usuarioId = :usuarioId")
    Optional<Integer> intentos(@Param("usuarioId") UUID usuarioId);

    @Modifying
    @Query("update Credenciales c set c.intentosFallidos = 0, c.fechaActualizacion = :ahora "
            + "where c.usuarioId = :usuarioId and c.intentosFallidos <> 0")
    int reiniciarIntentos(@Param("usuarioId") UUID usuarioId, @Param("ahora") Instant ahora);
}
