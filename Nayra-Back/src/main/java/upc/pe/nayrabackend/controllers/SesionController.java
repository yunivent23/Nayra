package upc.pe.nayrabackend.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import upc.pe.nayrabackend.dtos.SesionDTO;
import upc.pe.nayrabackend.exceptions.CodigoError;
import upc.pe.nayrabackend.exceptions.NayraException;
import upc.pe.nayrabackend.securities.UsuarioAutenticado;
import upc.pe.nayrabackend.serviceinterfaces.ISesionesService;

import java.util.List;
import java.util.UUID;

/** Gestión de sesiones activas propias (04_API.md §2.5). HU-14. */
@RestController
@RequestMapping("/api/v1/sesiones")
public class SesionController {

    private final ISesionesService sesiones;

    public SesionController(ISesionesService sesiones) {
        this.sesiones = sesiones;
    }

    @GetMapping
    public List<SesionDTO> listar(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        return sesiones.listarAbiertas(usuario.usuarioId()).stream()
                .map(s -> new SesionDTO(s.getIdPublico(),
                        s.getDispositivo() == null ? null : s.getDispositivo().getNombre(),
                        s.getFechaInicio(), s.getUltimoAcceso(), s.getId().equals(usuario.sesionId())))
                .toList();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cerrar(@AuthenticationPrincipal UsuarioAutenticado usuario, @PathVariable UUID id) {
        if (!sesiones.cerrarPropia(id, usuario.usuarioId())) {
            throw new NayraException(CodigoError.NO_ENCONTRADO);
        }
        return ResponseEntity.noContent().build();
    }
}
