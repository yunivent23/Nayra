package upc.pe.nayrabackend.repositories.postgres;

import org.springframework.stereotype.Repository;
import upc.pe.nayrabackend.entities.Identificadores;
import upc.pe.nayrabackend.entities.Rol;
import upc.pe.nayrabackend.entities.TipoDocumentoIdentidad;
import upc.pe.nayrabackend.entities.Usuario;
import upc.pe.nayrabackend.repositories.IUsuariosRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/** Adaptador PostgreSQL de USUARIOS (nayra.usuarios, D-051). Cada guardado se escribe de inmediato. */
@Repository
public class UsuariosPostgres implements IUsuariosRepository {

    private final UsuarioJpaRepository jpa;

    UsuariosPostgres(UsuarioJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public void guardar(Usuario usuario) { jpa.saveAndFlush(usuario); }

    @Override
    public Optional<Usuario> porId(String id) { return Identificadores.leer(id).flatMap(jpa::findById); }

    @Override
    public Optional<Usuario> porDocumento(TipoDocumentoIdentidad tipo, String numero) {
        return tipo == null || numero == null ? Optional.empty() : jpa.findByTipoDocumentoIdentidadAndNumeroDocumento(tipo, numero);
    }

    @Override
    public Optional<Usuario> porCelular(String celular) {
        return celular == null ? Optional.empty() : jpa.findByCelular(celular);
    }

    @Override
    public List<Usuario> porCelulares(Collection<String> celulares) {
        return celulares.isEmpty() ? List.of() : jpa.findByCelularIn(celulares);
    }

    @Override
    public List<Usuario> todos() { return jpa.findAll(); }

    @Override
    public boolean existeConRol(Rol rol) { return jpa.existsByRol(rol); }
}
