package upc.pe.nayrabackend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import upc.pe.nayrabackend.entities.Sesiones;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ISesionesRepository extends JpaRepository<Sesiones, Long> {

    @Query("select s from Sesiones s join fetch s.usuario u where s.tokenHash = :tokenHash and s.fechaCierre is null")
    Optional<Sesiones> buscarAbiertaPorTokenHash(@Param("tokenHash") String tokenHash);

    @Query("select s from Sesiones s left join fetch s.dispositivo where s.usuario.id = :usuarioId and s.fechaCierre is null " +
            "order by s.fechaInicio desc")
    List<Sesiones> listarAbiertas(@Param("usuarioId") Long usuarioId);

    Optional<Sesiones> findByIdPublicoAndUsuarioIdAndFechaCierreIsNull(UUID idPublico, Long usuarioId);

    @Modifying(flushAutomatically = true)
    @Query("update Sesiones s set s.fechaCierre = :ahora, s.motivoCierre = :motivo " +
            "where s.usuario.id = :usuarioId and s.fechaCierre is null")
    int cerrarTodasDelUsuario(@Param("usuarioId") Long usuarioId, @Param("motivo") String motivo,
                              @Param("ahora") OffsetDateTime ahora);

    @Modifying
    @Query("update Sesiones s set s.ultimoAcceso = :ahora where s.id = :id and s.fechaCierre is null")
    int registrarAcceso(@Param("id") Long id, @Param("ahora") OffsetDateTime ahora);
}
