package upc.pe.nayrabackend.controllers;

import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import upc.pe.nayrabackend.dtos.AutenticacionVozDTOs.RespuestaNonce;
import upc.pe.nayrabackend.dtos.AutenticacionVozDTOs.RespuestaTransaccion;
import upc.pe.nayrabackend.dtos.AutenticacionVozDTOs.ResultadoPaso;
import upc.pe.nayrabackend.dtos.AutenticacionVozDTOs.SolicitudNonce;
import upc.pe.nayrabackend.dtos.AutenticacionVozDTOs.SolicitudPin;
import upc.pe.nayrabackend.dtos.AutenticacionVozDTOs.SolicitudTransaccion;
import upc.pe.nayrabackend.serviceinterfaces.IAutenticacionVozService;

import java.io.IOException;

/**
 * Inicio de sesión por voz del prototipo (AG-13; D-037 modificada por D-061).
 * RUTAS PROVISIONALES — el contrato de la API sigue pendiente en docs/04_API.md (D-014).
 * Solo se activan con el perfil "prototipo".
 */
@RestController
@Profile("prototipo")
@RequestMapping("/prototipo/autenticacion")
public class AutenticacionVozController {

    private final IAutenticacionVozService servicio;

    public AutenticacionVozController(IAutenticacionVozService servicio) {
        this.servicio = servicio;
    }

    @PostMapping("/nonces")
    public RespuestaNonce nonce(@RequestBody SolicitudNonce solicitud) {
        return new RespuestaNonce(servicio.emitirNonce(solicitud.dispositivoId()), IAutenticacionVozService.PROPOSITO_INICIO_SESION);
    }

    @PostMapping("/transacciones")
    public RespuestaTransaccion abrir(@RequestBody SolicitudTransaccion solicitud) {
        return new RespuestaTransaccion(servicio.abrirTransaccion(solicitud.dispositivoId(), solicitud.nonce(), solicitud.firma()));
    }

    @PostMapping("/transacciones/{id}/pin")
    public ResultadoPaso pin(@PathVariable("id") String id, @RequestBody SolicitudPin solicitud) {
        return servicio.verificarPin(id, solicitud.pin());
    }

    @PostMapping(value = "/transacciones/{id}/pin-dictado", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResultadoPaso pinDictado(@PathVariable("id") String id, @RequestPart("audio") MultipartFile audio) throws IOException {
        return servicio.verificarPinDictado(id, audio.getBytes());
    }

    @PostMapping(value = "/transacciones/{id}/voz", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResultadoPaso voz(@PathVariable("id") String id, @RequestPart("audio") MultipartFile audio) throws IOException {
        return servicio.verificarVoz(id, audio.getBytes());
    }
}
