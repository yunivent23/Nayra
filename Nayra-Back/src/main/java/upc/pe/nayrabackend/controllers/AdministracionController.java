package upc.pe.nayrabackend.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import upc.pe.nayrabackend.dtos.AdministracionDTOs.DispositivoResumen;
import upc.pe.nayrabackend.dtos.AdministracionDTOs.EventoAuditoriaDTO;
import upc.pe.nayrabackend.dtos.AdministracionDTOs.UsuarioDetalle;
import upc.pe.nayrabackend.dtos.AdministracionDTOs.UsuarioResumen;
import upc.pe.nayrabackend.dtos.RegistroDTOs.RegistroIniciado;
import upc.pe.nayrabackend.dtos.RegistroDTOs.SolicitudInicioRegistro;
import upc.pe.nayrabackend.serviceinterfaces.IAdministracionService;
import upc.pe.nayrabackend.serviceinterfaces.IRegistroService;

import java.util.List;

/**
 * API del administrador. Funciones y límites según D-041; el panel web sigue pendiente (D-045).
 * Acceso solo para ADMIN mediante WebSecurityConfig: mecanismo de autorización PROVISIONAL (D-009 pendiente).
 * Rutas PROVISIONALES hasta docs/04_API.md (D-014).
 */
@RestController
@RequestMapping("/api/v1/admin")
public class AdministracionController {

    private final IAdministracionService admin;
    private final IRegistroService registro;

    public AdministracionController(IAdministracionService admin, IRegistroService registro) {
        this.admin = admin;
        this.registro = registro;
    }

    @GetMapping("/usuarios")
    public List<UsuarioResumen> usuarios(@RequestParam(name = "dni", required = false) String dni,
                                         @RequestParam(name = "texto", required = false) String texto) {
        return admin.buscarUsuarios(actor(), dni, texto);
    }

    @GetMapping("/usuarios/{id}")
    public UsuarioDetalle usuario(@PathVariable("id") String id) {
        return admin.detalleUsuario(actor(), id);
    }

    @PostMapping("/usuarios/{id}/bloqueo")
    public ResponseEntity<Void> bloquear(@PathVariable("id") String id) {
        admin.bloquearUsuario(actor(), id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/usuarios/{id}/desbloqueo")
    public ResponseEntity<Void> desbloquear(@PathVariable("id") String id) {
        admin.desbloquearUsuario(actor(), id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/usuarios/{id}/dispositivo")
    public ResponseEntity<DispositivoResumen> dispositivo(@PathVariable("id") String id) {
        return admin.dispositivoActivo(actor(), id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.noContent().build());
    }

    @PostMapping("/usuarios/{id}/dispositivo/revocacion")
    public ResponseEntity<Void> revocarDispositivo(@PathVariable("id") String id) {
        admin.revocarDispositivo(actor(), id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Registro asistido, pasos 3–5 (D-052): el representante proporciona el DNI.
     * PROVISIONAL: solo un ADMIN actúa como representante; D-052 admite también otra persona autorizada (pendiente).
     */
    @PostMapping("/registros")
    public RegistroIniciado iniciarRegistro(@RequestBody SolicitudInicioRegistro solicitud) {
        return registro.iniciar(actor(), solicitud == null ? null : solicitud.dni());
    }

    /** Registro asistido, paso 6 (D-052): el representante valida la identidad. */
    @PostMapping("/registros/{codigo}/validacion-identidad")
    public ResponseEntity<Void> validarIdentidad(@PathVariable("codigo") String codigo) {
        registro.validarIdentidad(actor(), codigo);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/auditoria")
    public List<EventoAuditoriaDTO> auditoria(@RequestParam(name = "usuarioId", required = false) String usuarioId) {
        return admin.auditoria(actor(), usuarioId);
    }

    @GetMapping("/auditoria/intentos-fallidos")
    public List<EventoAuditoriaDTO> intentosFallidos(@RequestParam(name = "usuarioId", required = false) String usuarioId) {
        return admin.intentosFallidos(actor(), usuarioId);
    }

    @GetMapping("/auditoria/acciones-administrativas")
    public List<EventoAuditoriaDTO> accionesAdministrativas() {
        return admin.accionesAdministrativas(actor());
    }

    private static String actor() {
        return SesionActual.usuario().usuarioId();
    }
}
