package upc.pe.nayrabackend.repositories.postgres;

import org.springframework.stereotype.Repository;
import upc.pe.nayrabackend.entities.Identificadores;
import upc.pe.nayrabackend.entities.Notificaciones;
import upc.pe.nayrabackend.repositories.INotificacionesRepository;

import java.util.List;
import java.util.Optional;

/** Adaptador PostgreSQL de NOTIFICACIONES (nayra.notificaciones, v4 §2.11). */
@Repository
public class NotificacionesPostgres implements INotificacionesRepository {

    private final NotificacionJpaRepository jpa;

    NotificacionesPostgres(NotificacionJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public void guardar(Notificaciones notificacion) { jpa.saveAndFlush(notificacion); }

    @Override
    public Optional<Notificaciones> porId(String id) { return Identificadores.leer(id).flatMap(jpa::findById); }

    @Override
    public List<Notificaciones> deDestinatario(String usuarioId) {
        return Identificadores.leer(usuarioId).map(jpa::findByDestinatarioIdOrderByFechaGeneracionDesc).orElse(List.of());
    }
}
