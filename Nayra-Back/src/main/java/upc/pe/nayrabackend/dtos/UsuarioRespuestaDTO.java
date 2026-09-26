package upc.pe.nayrabackend.dtos;

import upc.pe.nayrabackend.entities.Role;
import upc.pe.nayrabackend.entities.Users;

import java.time.LocalDate;
import java.util.List;

/** Datos de usuario expuestos por la API. Nunca incluye contraseña, hash del PIN ni datos biométricos. */
public record UsuarioRespuestaDTO(Long id, String username, String dni, String email, LocalDate fechaNacimiento,
                                  String telefono, String direccion, String fotoUsuario, Boolean enabled,
                                  Boolean bloqueado, List<String> roles) {

    public static UsuarioRespuestaDTO de(Users u) {
        return new UsuarioRespuestaDTO(u.getId(), u.getUsername(), u.getDni(), u.getEmail(), u.getFechaNacimiento(),
                u.getTelefono(), u.getDireccion(), u.getFotoUsuario(), u.getEnabled(), u.getBloqueado(),
                u.getRoles() == null ? List.of() : u.getRoles().stream().map(Role::getRol).toList());
    }
}
