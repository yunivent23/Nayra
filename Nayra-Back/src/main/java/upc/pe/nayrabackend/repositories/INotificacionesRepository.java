package upc.pe.nayrabackend.repositories;

import upc.pe.nayrabackend.entities.Notificaciones;

import java.util.List;
import java.util.Optional;

/** Puerto de persistencia de NOTIFICACIONES (servicio de negocio, v4 §2.11). Adaptador: PostgreSQL. */
public interface INotificacionesRepository {
    void guardar(Notificaciones notificacion);
    Optional<Notificaciones> porId(String id);
    List<Notificaciones> deDestinatario(String usuarioId);
}
