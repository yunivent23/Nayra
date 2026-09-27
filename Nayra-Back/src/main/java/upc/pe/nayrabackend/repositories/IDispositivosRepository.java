package upc.pe.nayrabackend.repositories;

import upc.pe.nayrabackend.entities.Dispositivos;

import java.util.Optional;

/** Puerto de persistencia de DISPOSITIVOS. Adaptador actual: en memoria (PROVISIONAL, D-051). */
public interface IDispositivosRepository {
    void guardar(Dispositivos dispositivo);
    Optional<Dispositivos> porId(String id);
    Optional<Dispositivos> activoDeUsuario(String usuarioId);
}
