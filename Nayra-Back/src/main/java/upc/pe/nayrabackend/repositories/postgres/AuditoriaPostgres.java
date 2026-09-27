package upc.pe.nayrabackend.repositories.postgres;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;
import upc.pe.nayrabackend.entities.Auditoria;
import upc.pe.nayrabackend.entities.Auditoria.Resultado;
import upc.pe.nayrabackend.entities.Identificadores;
import upc.pe.nayrabackend.repositories.IAuditoriaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Adaptador PostgreSQL de AUDITORÍA (nayra.auditoria, D-051). Solo inserta y consulta: el usuario de ejecución
 * no tiene permisos de UPDATE ni DELETE sobre la tabla (V004).
 */
@Repository
public class AuditoriaPostgres implements IAuditoriaRepository {

    private final AuditoriaJpaRepository jpa;

    AuditoriaPostgres(AuditoriaJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public void guardar(Auditoria evento) { jpa.saveAndFlush(evento); }

    @Override
    public List<Auditoria> consultar(String usuarioId, String accionPrefijo, Resultado resultado) {
        Specification<Auditoria> filtro = (raiz, consulta, cb) -> cb.conjunction();
        if (usuarioId != null) {
            Optional<UUID> usuario = Identificadores.leer(usuarioId);
            if (usuario.isEmpty()) {
                return List.of();
            }
            filtro = filtro.and((raiz, consulta, cb) -> cb.or(cb.equal(raiz.get("actorId"), usuario.get()),
                    cb.equal(raiz.get("usuarioAfectadoId"), usuario.get())));
        }
        if (accionPrefijo != null) {
            filtro = filtro.and((raiz, consulta, cb) -> cb.like(raiz.get("accion"), accionPrefijo + "%"));
        }
        if (resultado != null) {
            filtro = filtro.and((raiz, consulta, cb) -> cb.equal(raiz.get("resultado"), resultado));
        }
        return jpa.findAll(filtro, Sort.by(Sort.Direction.DESC, "fecha"));
    }
}
