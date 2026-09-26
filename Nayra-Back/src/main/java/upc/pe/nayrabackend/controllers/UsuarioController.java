package upc.pe.nayrabackend.controllers;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import upc.pe.nayrabackend.dtos.UsuarioDTO;
import upc.pe.nayrabackend.dtos.UsuarioRespuestaDTO;
import upc.pe.nayrabackend.entities.Role;
import upc.pe.nayrabackend.entities.Users;
import upc.pe.nayrabackend.serviceinterfaces.IUsuarioService;

import java.util.List;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    @Autowired
    private IUsuarioService service;

    // LISTAR TODOS LOS USUARIOS
    @GetMapping
    public List<UsuarioRespuestaDTO> listar() {
        return service.listarTodo().stream().map(UsuarioRespuestaDTO::de).toList();
    }

    // REGISTRAR USUARIO
    @PostMapping
    public ResponseEntity<String> insert(@RequestBody UsuarioDTO dto) {

        Users usuario = new Users();

        usuario.setId(null);
        usuario.setUsername(dto.getUsername());
        // P-A01 pendiente: no se registra contraseña; el PIN (D-043) se crea en el flujo de registro de AG-01
        usuario.setEnabled(dto.getEnabled());
        usuario.setDni(dto.getDni());
        usuario.setEmail(dto.getEmail());
        usuario.setFechaNacimiento(dto.getFechaNacimiento());
        usuario.setTelefono(dto.getTelefono());
        usuario.setDireccion(dto.getDireccion());
        usuario.setFotoUsuario(dto.getFotoUsuario());

        // CREAR EL ROL ASOCIADO AL USUARIO
        Role role = new Role();
        role.setRol(dto.getRole());
        role.setUser(usuario);

        usuario.setRoles(List.of(role));

        service.insert(usuario);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body("Usuario registrado correctamente.");
    }

    // BUSCAR USUARIO POR ID
    @GetMapping("/buscar/{id}")
    public ResponseEntity<?> findId(@PathVariable("id") Long id) {

        Users usuario = service.listId(id);

        if (usuario == null) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("No existe un usuario con el ID: " + id);
        }

        return ResponseEntity.ok(UsuarioRespuestaDTO.de(usuario));
    }

    // ELIMINAR USUARIO
    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable("id") Long id) {

        Users usuario = service.listId(id);

        if (usuario == null) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("No existe un usuario con el ID: " + id);
        }

        service.delete(id);

        return ResponseEntity.ok(
                "Usuario con ID " + id + " eliminado correctamente."
        );
    }

    // MODIFICAR USUARIO
    @PutMapping
    public ResponseEntity<String> edit(@RequestBody UsuarioDTO dto) {

        if (dto.getId() == null) {
            return ResponseEntity
                    .badRequest()
                    .body("El ID del usuario es obligatorio para la edición.");
        }

        Users existente = service.listId(dto.getId());

        if (existente == null) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("No se puede modificar. No existe el usuario con ID: "
                            + dto.getId());
        }

        // Solo datos personales (HU-23): nunca contraseña, PIN, roles ni estado de bloqueo
        existente.setUsername(dto.getUsername());
        existente.setDni(dto.getDni());
        existente.setEmail(dto.getEmail());
        existente.setFechaNacimiento(dto.getFechaNacimiento());
        existente.setTelefono(dto.getTelefono());
        existente.setDireccion(dto.getDireccion());
        existente.setFotoUsuario(dto.getFotoUsuario());
        service.edit(existente);

        return ResponseEntity.ok(
                "Usuario con ID " + existente.getId()
                        + " modificado correctamente."
        );
    }

    // BUSCAR USUARIOS POR USERNAME
    @GetMapping("/busquedas")
    public ResponseEntity<?> buscarPorNombre(
            @RequestParam String nombre) {

        List<Users> usuarios = service.buscarPorNombre(nombre);

        if (usuarios.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("No se encontraron usuarios con el nombre: "
                            + nombre);
        }

        return ResponseEntity.ok(usuarios.stream().map(UsuarioRespuestaDTO::de).toList());
    }

    // BUSCAR USUARIO POR USERNAME
    @GetMapping("/username/{username}")
    public ResponseEntity<?> buscarPorUsername(
            @PathVariable("username") String username) {

        Users usuario = service.buscarPorUsername(username);

        if (usuario == null) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("No existe un usuario con el username: "
                            + username);
        }

        return ResponseEntity.ok(UsuarioRespuestaDTO.de(usuario));
    }

    // BUSCAR USUARIO POR DNI
    @GetMapping("/dni/{dni}")
    public ResponseEntity<?> buscarPorDni(
            @PathVariable("dni") String dni) {

        Users usuario = service.buscarPorDni(dni);

        if (usuario == null) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("No existe un usuario con el DNI: " + dni);
        }

        return ResponseEntity.ok(UsuarioRespuestaDTO.de(usuario));
    }

    // BUSCAR USUARIO POR EMAIL
    @GetMapping("/email/{email}")
    public ResponseEntity<?> buscarPorEmail(
            @PathVariable("email") String email) {

        Users usuario = service.buscarPorEmail(email);

        if (usuario == null) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("No existe un usuario con el email: " + email);
        }

        return ResponseEntity.ok(UsuarioRespuestaDTO.de(usuario));
    }
}
