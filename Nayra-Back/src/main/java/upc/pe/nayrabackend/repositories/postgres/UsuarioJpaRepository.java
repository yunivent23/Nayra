package upc.pe.nayrabackend.repositories.postgres;

import org.springframework.data.jpa.repository.JpaRepository;
import upc.pe.nayrabackend.entities.Rol;
import upc.pe.nayrabackend.entities.TipoDocumentoIdentidad;
import upc.pe.nayrabackend.entities.Usuario;

import java.util.Optional;
import java.util.UUID;

interface UsuarioJpaRepository extends JpaRepository<Usuario, UUID> {
    Optional<Usuario> findByTipoDocumentoIdentidadAndNumeroDocumento(TipoDocumentoIdentidad tipo, String numero);
    Optional<Usuario> findByCelular(String celular);
    boolean existsByRol(Rol rol);
}
