package upc.pe.nayrabackend.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import upc.pe.nayrabackend.serviceinterfaces.ISesionesService;

/** Sesión del usuario (D-018). Rutas PROVISIONALES hasta docs/04_API.md (D-014). */
@RestController
@RequestMapping("/api/v1/sesiones")
public class SesionController {

    private final ISesionesService sesiones;

    public SesionController(ISesionesService sesiones) {
        this.sesiones = sesiones;
    }

    /** HU-13: cerrar sesión. */
    @DeleteMapping("/actual")
    public ResponseEntity<Void> cerrar() {
        sesiones.cerrar(SesionActual.usuario().sesionId());
        return ResponseEntity.noContent().build();
    }
}
