package upc.pe.nayrabackend.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import upc.pe.nayrabackend.dtos.RegistroDTOs.DatosParaConfirmar;
import upc.pe.nayrabackend.dtos.RegistroDTOs.EstadoRegistro;
import upc.pe.nayrabackend.dtos.RegistroDTOs.RegistroFinalizado;
import upc.pe.nayrabackend.dtos.RegistroDTOs.SolicitudDatosRegistro;
import upc.pe.nayrabackend.serviceinterfaces.IRegistroService;

/**
 * Pasos del registro asistido que la persona completa en su celular (D-052, pasos 7–10 y 13).
 * Sin sesión: los protege el código de registro de un solo uso (mecanismo PROVISIONAL del prototipo, no decisión de D-052).
 * Rutas PROVISIONALES hasta docs/04_API.md (D-014). El enrolamiento de voz (paso 11) va por /prototipo.
 */
@RestController
@RequestMapping("/api/v1/registros")
public class RegistroController {

    private final IRegistroService registro;

    public RegistroController(IRegistroService registro) {
        this.registro = registro;
    }

    @GetMapping("/{codigo}")
    public DatosParaConfirmar datos(@PathVariable("codigo") String codigo) {
        return registro.datosParaConfirmar(codigo);
    }

    @PostMapping("/{codigo}/datos")
    public EstadoRegistro completar(@PathVariable("codigo") String codigo, @RequestBody SolicitudDatosRegistro solicitud) {
        return registro.completarDatos(codigo, solicitud);
    }

    @PostMapping("/{codigo}/finalizacion")
    public RegistroFinalizado finalizar(@PathVariable("codigo") String codigo) {
        return registro.finalizar(codigo);
    }
}
