package upc.pe.nayrabackend.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import upc.pe.nayrabackend.dtos.UsuarioDTOs.DatosPropios;
import upc.pe.nayrabackend.serviceinterfaces.IUsuarioService;

/** Cuenta de acceso del usuario autenticado. Rutas PROVISIONALES hasta docs/04_API.md (D-014). */
@RestController
@RequestMapping("/api/v1/usuarios")
public class UsuarioController {

    private final IUsuarioService usuarios;

    public UsuarioController(IUsuarioService usuarios) {
        this.usuarios = usuarios;
    }

    /** HU-09 (consultar datos propios) y HU-15 (estado de la cuenta). */
    @GetMapping("/me")
    public DatosPropios datosPropios() {
        return usuarios.datosPropios(SesionActual.usuario().usuarioId());
    }
}
