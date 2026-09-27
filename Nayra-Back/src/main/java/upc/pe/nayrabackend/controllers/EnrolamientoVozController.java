package upc.pe.nayrabackend.controllers;

import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import upc.pe.nayrabackend.dtos.AutenticacionVozDTOs.DesafioDTO;
import upc.pe.nayrabackend.dtos.AutenticacionVozDTOs.ResultadoEnrolamiento;
import upc.pe.nayrabackend.dtos.AutenticacionVozDTOs.ResultadoMuestra;
import upc.pe.nayrabackend.serviceinterfaces.IEnrolamientoVozService;

import java.io.IOException;

/**
 * Enrolamiento de voz dentro del registro asistido (D-052 paso 11; AG-13). Lo protege el código de registro
 * (mecanismo PROVISIONAL del prototipo, no decisión de D-052).
 * RUTAS PROVISIONALES (excepción /prototipo del primer entregable, D-014). Solo perfil "prototipo".
 */
@RestController
@Profile("prototipo")
@RequestMapping("/prototipo/registro/{codigo}/enrolamiento")
public class EnrolamientoVozController {

    private final IEnrolamientoVozService servicio;

    public EnrolamientoVozController(IEnrolamientoVozService servicio) {
        this.servicio = servicio;
    }

    @PostMapping("/desafios")
    public DesafioDTO desafio(@PathVariable("codigo") String codigo) {
        return servicio.emitirDesafio(codigo);
    }

    @PostMapping(value = "/muestras", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResultadoMuestra muestra(@PathVariable("codigo") String codigo, @RequestParam("desafioId") String desafioId,
                                    @RequestPart("audio") MultipartFile audio) throws IOException {
        return servicio.enviarMuestra(codigo, desafioId, audio.getBytes());
    }

    @PostMapping("/finalizacion")
    public ResultadoEnrolamiento finalizar(@PathVariable("codigo") String codigo) {
        return servicio.finalizar(codigo);
    }
}
