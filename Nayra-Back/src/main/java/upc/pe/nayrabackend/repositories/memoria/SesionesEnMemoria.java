package upc.pe.nayrabackend.repositories.memoria;

import org.springframework.stereotype.Repository;
import upc.pe.nayrabackend.entities.Sesiones;
import upc.pe.nayrabackend.repositories.ISesionesRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/** PROVISIONAL (D-018, D-051). */
@Repository
public class SesionesEnMemoria implements ISesionesRepository {

    private final Map<String, Sesiones> sesiones = new ConcurrentHashMap<>();

    @Override
    public void guardar(Sesiones sesion) { sesiones.put(sesion.getId(), sesion); }

    @Override
    public Optional<Sesiones> porTokenHash(String tokenHash) {
        return sesiones.values().stream().filter(s -> s.getTokenHash().equals(tokenHash)).findFirst();
    }

    @Override
    public Optional<Sesiones> porId(String id) { return Optional.ofNullable(id == null ? null : sesiones.get(id)); }

    @Override
    public List<Sesiones> deUsuario(String usuarioId) {
        return sesiones.values().stream().filter(s -> s.getUsuarioId().equals(usuarioId)).toList();
    }
}
