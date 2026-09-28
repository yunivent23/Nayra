package upc.pe.nayrabackend.controllers;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import upc.pe.nayrabackend.dtos.DestinatarioDTOs.DestinatarioEncontrado;
import upc.pe.nayrabackend.dtos.DestinatarioDTOs.SolicitudBusquedaDestinatario;
import upc.pe.nayrabackend.serviceinterfaces.IOperacionesService;

/**
 * Destinatario de una transferencia (G-1, HU-69). Requiere sesión. Rutas PROVISIONALES hasta docs/04_API.md (D-014).
 * Es POST para que el celular no quede en la URL ni en los registros de acceso.
 */
@RestController
@RequestMapping("/api/v1/destinatarios")
public class DestinatarioController {

    private final IOperacionesService operaciones;

    public DestinatarioController(IOperacionesService operaciones) {
        this.operaciones = operaciones;
    }

    @PostMapping("/busqueda")
    public DestinatarioEncontrado buscar(@RequestBody SolicitudBusquedaDestinatario solicitud) {
        return operaciones.buscarDestinatario(SesionActual.usuario().usuarioId(), solicitud.celular());
    }
}
