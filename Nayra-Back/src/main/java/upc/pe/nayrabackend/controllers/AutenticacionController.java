package upc.pe.nayrabackend.controllers;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import upc.pe.nayrabackend.config.NayraSeguridadProperties;
import upc.pe.nayrabackend.dtos.DesafioDTO;
import upc.pe.nayrabackend.dtos.SesionCreadaDTO;
import upc.pe.nayrabackend.dtos.SolicitudDesafioDTO;
import upc.pe.nayrabackend.entities.DesafioAutenticacion;
import upc.pe.nayrabackend.securities.UsuarioAutenticado;
import upc.pe.nayrabackend.serviceinterfaces.IAutenticacionService;
import upc.pe.nayrabackend.serviceinterfaces.ISesionesService;

import java.io.IOException;
import java.util.Arrays;
import java.util.UUID;

/** Inicio y cierre de sesión (04_API.md §2.1, §2.2, §2.5). HU-12, HU-13, HU-40 a HU-45. */
@RestController
@RequestMapping("/api/v1/auth")
public class AutenticacionController {

    private final IAutenticacionService autenticacion;
    private final ISesionesService sesiones;
    private final NayraSeguridadProperties propiedades;

    public AutenticacionController(IAutenticacionService autenticacion, ISesionesService sesiones,
                                   NayraSeguridadProperties propiedades) {
        this.autenticacion = autenticacion;
        this.sesiones = sesiones;
        this.propiedades = propiedades;
    }

    @PostMapping("/desafios")
    public ResponseEntity<DesafioDTO> desafio(@Valid @RequestBody SolicitudDesafioDTO solicitud) {
        DesafioAutenticacion d = autenticacion.emitirDesafioLogin(solicitud.dispositivoId());
        var elementos = Arrays.asList(d.getElementos().split(" "));
        return ResponseEntity.status(HttpStatus.CREATED).body(new DesafioDTO(
                d.getIdPublico(), elementos, String.join(", ", elementos), d.getNonce(), d.getFechaExpiracion()));
    }

    @PostMapping(value = "/sesiones", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SesionCreadaDTO> iniciar(@RequestParam("desafioId") UUID desafioId,
                                                   @RequestParam("firma") String firma,
                                                   @RequestParam("pin") String pin,
                                                   @RequestPart("audio") MultipartFile audio) throws IOException {
        String token = autenticacion.autenticar(desafioId, firma, pin, audio.getBytes());
        return ResponseEntity.status(HttpStatus.CREATED).body(
                new SesionCreadaDTO(token, propiedades.sesion().inactividad().toSeconds()));
    }

    @DeleteMapping("/sesiones/actual")
    public ResponseEntity<Void> cerrar(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        sesiones.cerrar(usuario.sesionId(), ISesionesService.LOGOUT);
        return ResponseEntity.noContent().build();
    }
}
