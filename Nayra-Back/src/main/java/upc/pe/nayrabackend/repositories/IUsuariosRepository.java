package upc.pe.nayrabackend.repositories;

import upc.pe.nayrabackend.entities.Rol;
import upc.pe.nayrabackend.entities.TipoDocumentoIdentidad;
import upc.pe.nayrabackend.entities.Usuario;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/** Puerto de persistencia de USUARIOS. Adaptador: PostgreSQL (D-051). */
public interface IUsuariosRepository {
    void guardar(Usuario usuario);
    Optional<Usuario> porId(String id);
    Optional<Usuario> porDocumento(TipoDocumentoIdentidad tipo, String numero);
    /** Usuario con ese celular en formato canónico (único, V012). */
    Optional<Usuario> porCelular(String celular);
    /** Usuarios cuyo celular (formato canónico) está en la colección, en una sola consulta. */
    List<Usuario> porCelulares(Collection<String> celulares);
    List<Usuario> todos();
    boolean existeConRol(Rol rol);
}
