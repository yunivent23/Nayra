package upc.pe.nayrabackend.controllers;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import upc.pe.nayrabackend.dtos.DestinatarioDTOs.DestinatarioEncontrado;
import upc.pe.nayrabackend.dtos.DestinatarioDTOs.DestinatariosDisponibles;
import upc.pe.nayrabackend.dtos.DestinatarioDTOs.SolicitudBusquedaDestinatario;
import upc.pe.nayrabackend.dtos.DestinatarioDTOs.SolicitudBusquedaMultiple;
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

    /**
     * Cuáles de los celulares de la agenda del teléfono están registrados en Nayra y pueden recibir (D-042, HU-69).
     * Máximo {@code IOperacionesService.MAX_CELULARES_POR_CONSULTA} por solicitud. Solo devuelve coincidencias.
     */
    @PostMapping("/busqueda-multiple")
    public DestinatariosDisponibles buscarVarios(@RequestBody SolicitudBusquedaMultiple solicitud) {
        return operaciones.buscarDestinatarios(SesionActual.usuario().usuarioId(), solicitud.celulares());
    }
}
