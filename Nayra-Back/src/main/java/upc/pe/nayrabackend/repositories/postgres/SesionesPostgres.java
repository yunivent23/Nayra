package upc.pe.nayrabackend.repositories.postgres;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import upc.pe.nayrabackend.entities.Identificadores;
import upc.pe.nayrabackend.entities.Sesiones;
import upc.pe.nayrabackend.repositories.ISesionesRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Adaptador PostgreSQL de SESIONES (nayra.sesiones, v4 §2.7). Solo el alta guarda la entidad; los cambios posteriores
 * son UPDATE condicionales atómicos (ver {@link ISesionesRepository}, E-02).
 */
@Repository
public class SesionesPostgres implements ISesionesRepository {

    private final SesionJpaRepository jpa;

    SesionesPostgres(SesionJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    @Transactional
    public void crear(Sesiones sesion) {
        UUID id = UUID.fromString(sesion.getId());
        if (jpa.existsById(id)) {
            throw new IllegalStateException("La sesión ya existe; solo se modifica con registrarAcceso o revocar.");
        }
        jpa.saveAndFlush(sesion);
    }

    @Override
    public Optional<Sesiones> porId(String id) { return Identificadores.leer(id).flatMap(jpa::findById); }

    @Override
    public List<Sesiones> vigentesDeUsuario(String usuarioId) {
        return Identificadores.leer(usuarioId).map(jpa::findByUsuarioIdAndFechaRevocacionIsNull).orElse(List.of());
    }

    @Override
    @Transactional
    public boolean registrarAcceso(String id, Instant ahora, Instant limiteInactividad) {
        return Identificadores.leer(id).map(u -> jpa.registrarAcceso(u, ahora, limiteInactividad) == 1).orElse(false);
    }

    @Override
    @Transactional
    public boolean revocar(String id, Instant ahora) {
        return Identificadores.leer(id).map(u -> jpa.revocar(u, ahora) == 1).orElse(false);
    }

    @Override
    @Transactional
    public int revocarDeUsuario(String usuarioId, Instant ahora) {
        return Identificadores.leer(usuarioId).map(u -> jpa.revocarDeUsuario(u, ahora)).orElse(0);
    }
}
