package upc.pe.nayrabackend.controllers;

import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import upc.pe.nayrabackend.dtos.RegistroDTOs.RegistroIniciado;
import upc.pe.nayrabackend.dtos.RegistroDTOs.SolicitudInicioRegistro;
import upc.pe.nayrabackend.serviceinterfaces.IRegistroService;

/**
 * MECANISMO TÉCNICO PROVISIONAL DEL PROTOTIPO, solo perfil "prototipo" (autorizado el 2026-09-27): crea el
 * registro del primer administrador mientras no exista ninguno. Está separado del registro asistido de D-052 y
 * NO lo cumple: el primer administrador no pasa por la validación de identidad de un representante. Después el administrador completa el registro en su celular e inicia sesión con el
 * mismo flujo que el usuario (dispositivo + PIN + voz), lo que también es PROVISIONAL: D-050 sigue pendiente. Retirar cuando se decida cómo se crean los administradores.
 */
@RestController
@Profile("prototipo")
@RequestMapping("/prototipo/arranque")
public class ArranquePrototipoController {

    private final IRegistroService registro;

    public ArranquePrototipoController(IRegistroService registro) {
        this.registro = registro;
    }

    @PostMapping("/administrador")
    public RegistroIniciado administradorInicial(@RequestBody SolicitudInicioRegistro solicitud) {
        return registro.iniciarAdministradorInicial(solicitud == null ? null : solicitud.dni());
    }
}
