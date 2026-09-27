package upc.pe.nayrabackend.repositories;

import upc.pe.nayrabackend.entities.Sesiones;

import java.util.List;
import java.util.Optional;

/** Puerto de persistencia de SESIONES. Adaptador actual: en memoria (PROVISIONAL, D-018, D-051). */
public interface ISesionesRepository {
    void guardar(Sesiones sesion);
    Optional<Sesiones> porTokenHash(String tokenHash);
    Optional<Sesiones> porId(String id);
    List<Sesiones> deUsuario(String usuarioId);
}
