package upc.pe.nayrabackend.repositories.memoria;

import org.springframework.stereotype.Repository;
import upc.pe.nayrabackend.entities.Rol;
import upc.pe.nayrabackend.entities.Usuario;
import upc.pe.nayrabackend.repositories.IUsuariosRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/** PROVISIONAL (D-051): los datos se pierden al reiniciar y no se comparten entre instancias (D-023). */
@Repository
public class UsuariosEnMemoria implements IUsuariosRepository {

    private final Map<String, Usuario> usuarios = new ConcurrentHashMap<>();

    @Override
    public void guardar(Usuario usuario) { usuarios.put(usuario.getId(), usuario); }

    @Override
    public Optional<Usuario> porId(String id) { return Optional.ofNullable(id == null ? null : usuarios.get(id)); }

    @Override
    public Optional<Usuario> porDni(String dni) {
        return usuarios.values().stream().filter(u -> u.getDni().equals(dni)).findFirst();
    }

    @Override
    public List<Usuario> todos() { return List.copyOf(usuarios.values()); }

    @Override
    public boolean existeConRol(Rol rol) { return usuarios.values().stream().anyMatch(u -> u.getRol() == rol); }
}
