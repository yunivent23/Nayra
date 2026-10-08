package upc.pe.nayrabackend.repositories.memoria;

import upc.pe.nayrabackend.entities.Rol;
import upc.pe.nayrabackend.entities.TipoDocumentoIdentidad;
import upc.pe.nayrabackend.entities.Usuario;
import upc.pe.nayrabackend.repositories.IUsuariosRepository;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Adaptador en memoria para pruebas sin base de datos. No es un bean: la aplicación usa PostgreSQL (D-051).
 * No aplica las restricciones de la base de datos.
 */
public class UsuariosEnMemoria implements IUsuariosRepository {

    private final Map<String, Usuario> usuarios = new ConcurrentHashMap<>();

    @Override
    public void guardar(Usuario usuario) { usuarios.put(usuario.getId(), usuario); }

    @Override
    public Optional<Usuario> porId(String id) { return Optional.ofNullable(id == null ? null : usuarios.get(id)); }

    @Override
    public Optional<Usuario> porDocumento(TipoDocumentoIdentidad tipo, String numero) {
        return usuarios.values().stream()
                .filter(u -> u.getTipoDocumentoIdentidad() == tipo && u.getNumeroDocumento().equals(numero)).findFirst();
    }

    @Override
    public Optional<Usuario> porCelular(String celular) {
        return usuarios.values().stream().filter(u -> u.getCelular().equals(celular)).findFirst();
    }

    @Override
    public List<Usuario> porCelulares(Collection<String> celulares) {
        return usuarios.values().stream().filter(u -> celulares.contains(u.getCelular())).toList();
    }

    @Override
    public List<Usuario> todos() { return List.copyOf(usuarios.values()); }

    @Override
    public boolean existeConRol(Rol rol) { return usuarios.values().stream().anyMatch(u -> u.getRol() == rol); }
}
