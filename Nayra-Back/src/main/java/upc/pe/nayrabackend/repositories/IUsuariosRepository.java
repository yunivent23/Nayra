package upc.pe.nayrabackend.repositories;

import upc.pe.nayrabackend.entities.Rol;
import upc.pe.nayrabackend.entities.Usuario;

import java.util.List;
import java.util.Optional;

/** Puerto de persistencia de USUARIOS. Adaptador actual: en memoria (PROVISIONAL, D-051). */
public interface IUsuariosRepository {
    void guardar(Usuario usuario);
    Optional<Usuario> porId(String id);
    Optional<Usuario> porDni(String dni);
    List<Usuario> todos();
    boolean existeConRol(Rol rol);
}
