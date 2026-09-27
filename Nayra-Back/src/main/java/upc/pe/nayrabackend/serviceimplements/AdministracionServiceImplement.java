package upc.pe.nayrabackend.serviceimplements;

import org.springframework.stereotype.Service;
import upc.pe.nayrabackend.dtos.AdministracionDTOs.DispositivoResumen;
import upc.pe.nayrabackend.dtos.AdministracionDTOs.EventoAuditoriaDTO;
import upc.pe.nayrabackend.dtos.AdministracionDTOs.UsuarioDetalle;
import upc.pe.nayrabackend.dtos.AdministracionDTOs.UsuarioResumen;
import upc.pe.nayrabackend.entities.Auditoria;
import upc.pe.nayrabackend.entities.Dispositivos;
import upc.pe.nayrabackend.entities.Usuario;
import upc.pe.nayrabackend.excepciones.NayraException;
import upc.pe.nayrabackend.repositories.IDispositivosRepository;
import upc.pe.nayrabackend.repositories.IUsuariosRepository;
import upc.pe.nayrabackend.serviceinterfaces.IAdministracionService;
import upc.pe.nayrabackend.serviceinterfaces.IAuditoriaService;
import upc.pe.nayrabackend.serviceinterfaces.IAuditoriaService.Filtro;
import upc.pe.nayrabackend.serviceinterfaces.IDispositivoService;
import upc.pe.nayrabackend.serviceinterfaces.ISesionesService;
import upc.pe.nayrabackend.serviceinterfaces.IUsuarioService;

import java.text.Normalizer;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class AdministracionServiceImplement implements IAdministracionService {

    /** Prefijo de las acciones de administrador en la auditoría (HU-94). Catálogo definitivo pendiente (D-019). */
    static final String PREFIJO_ADMIN = "ADMIN_";
    /** Eventos de autenticación fallidos (HU-80). */
    private static final String PREFIJO_AUTENTICACION = "AUTENTICACION_";

    private final IUsuariosRepository usuarios;
    private final IDispositivosRepository dispositivosRepo;
    private final IUsuarioService usuarioService;
    private final IDispositivoService dispositivoService;
    private final ISesionesService sesiones;
    private final IAuditoriaService auditoria;

    public AdministracionServiceImplement(IUsuariosRepository usuarios, IDispositivosRepository dispositivosRepo,
                                          IUsuarioService usuarioService, IDispositivoService dispositivoService,
                                          ISesionesService sesiones, IAuditoriaService auditoria) {
        this.usuarios = usuarios;
        this.dispositivosRepo = dispositivosRepo;
        this.usuarioService = usuarioService;
        this.dispositivoService = dispositivoService;
        this.sesiones = sesiones;
        this.auditoria = auditoria;
    }

    @Override
    public List<UsuarioResumen> buscarUsuarios(String adminId, String dni, String texto) {
        String buscado = normalizar(texto);
        List<UsuarioResumen> resultado = usuarios.todos().stream()
                .filter(u -> dni == null || dni.isBlank() || u.getDni().equals(dni.trim()))
                .filter(u -> buscado.isEmpty() || normalizar(u.getNombres() + " " + u.getApellidos()).contains(buscado))
                .sorted(Comparator.comparing(Usuario::getApellidos).thenComparing(Usuario::getNombres))
                .map(u -> new UsuarioResumen(u.getId(), u.getDni(), u.getNombres(), u.getApellidos(),
                        u.getRol().name(), u.getEstado().name()))
                .toList();
        boolean esBusqueda = (dni != null && !dni.isBlank()) || !buscado.isEmpty();
        // Solo se registra el tipo de consulta: el DNI o el texto buscado son datos personales.
        auditoria.exito(esBusqueda ? "ADMIN_BUSQUEDA_USUARIOS" : "ADMIN_CONSULTA_USUARIOS", adminId, null);
        return resultado;
    }

    @Override
    public UsuarioDetalle detalleUsuario(String adminId, String usuarioId) {
        Usuario u = usuarioService.obtener(usuarioId);
        auditoria.exito("ADMIN_CONSULTA_USUARIO", adminId, usuarioId);
        return new UsuarioDetalle(u.getId(), u.getDni(), u.getNombres(), u.getApellidos(), u.getCelular(),
                u.getRol().name(), u.getEstado().name(), u.getFechaCreacion(),
                dispositivosRepo.activoDeUsuario(usuarioId).map(AdministracionServiceImplement::resumen).orElse(null));
    }

    @Override
    public void bloquearUsuario(String adminId, String usuarioId) {
        noSobreSiMismo(adminId, usuarioId, "ADMIN_BLOQUEO_CUENTA");
        Usuario u = usuarioService.obtener(usuarioId);
        if (u.getEstado() == Usuario.Estado.BLOQUEADA) {
            throw NayraException.conflicto("CUENTA_YA_BLOQUEADA");
        }
        usuarioService.bloquear(usuarioId, adminId, null);
    }

    @Override
    public void desbloquearUsuario(String adminId, String usuarioId) {
        Usuario u = usuarioService.obtener(usuarioId);
        if (u.getEstado() != Usuario.Estado.BLOQUEADA) {
            throw NayraException.conflicto("CUENTA_NO_BLOQUEADA");
        }
        usuarioService.desbloquear(usuarioId, adminId);
    }

    @Override
    public Optional<DispositivoResumen> dispositivoActivo(String adminId, String usuarioId) {
        usuarioService.obtener(usuarioId);
        auditoria.exito("ADMIN_CONSULTA_DISPOSITIVO", adminId, usuarioId);
        return dispositivosRepo.activoDeUsuario(usuarioId).map(AdministracionServiceImplement::resumen);
    }

    @Override
    public void revocarDispositivo(String adminId, String usuarioId) {
        noSobreSiMismo(adminId, usuarioId, "ADMIN_REVOCACION_DISPOSITIVO");
        usuarioService.obtener(usuarioId);
        Dispositivos activo = dispositivosRepo.activoDeUsuario(usuarioId)
                .orElseThrow(() -> NayraException.conflicto("SIN_DISPOSITIVO_ACTIVO"));
        dispositivoService.revocarActivo(usuarioId);
        // Un dispositivo revocado no puede mantener sesiones (D-040).
        sesiones.revocarDeUsuario(usuarioId, "DISPOSITIVO_REVOCADO");
        auditoria.registrar("ADMIN_REVOCACION_DISPOSITIVO", Auditoria.Resultado.EXITO, adminId, usuarioId, null,
                activo.getId());
    }

    @Override
    public List<EventoAuditoriaDTO> auditoria(String adminId, String usuarioId) {
        List<EventoAuditoriaDTO> r = eventos(new Filtro(vacioANull(usuarioId), null, null));
        auditoria.exito("ADMIN_CONSULTA_AUDITORIA", adminId, vacioANull(usuarioId));
        return r;
    }

    @Override
    public List<EventoAuditoriaDTO> intentosFallidos(String adminId, String usuarioId) {
        List<EventoAuditoriaDTO> r = eventos(new Filtro(vacioANull(usuarioId), PREFIJO_AUTENTICACION, Auditoria.Resultado.FALLO));
        auditoria.exito("ADMIN_CONSULTA_INTENTOS_FALLIDOS", adminId, vacioANull(usuarioId));
        return r;
    }

    @Override
    public List<EventoAuditoriaDTO> accionesAdministrativas(String adminId) {
        List<EventoAuditoriaDTO> r = eventos(new Filtro(null, PREFIJO_ADMIN, null));
        auditoria.exito("ADMIN_CONSULTA_ACCIONES_ADMINISTRATIVAS", adminId, null);
        return r;
    }

    /**
     * REGLA TÉCNICA PROVISIONAL DEL PROTOTIPO (autorizada el 2026-09-27): un administrador no puede bloquearse
     * ni revocar su propio dispositivo, para no dejar el prototipo sin administrador. Es una regla añadida;
     * no proviene de D-041.
     */
    private void noSobreSiMismo(String adminId, String usuarioId, String accion) {
        if (adminId != null && adminId.equals(usuarioId)) {
            auditoria.fallo(accion, adminId, usuarioId, "ACCION_SOBRE_SI_MISMO");
            throw NayraException.conflicto("ACCION_SOBRE_SI_MISMO");
        }
    }

    private List<EventoAuditoriaDTO> eventos(Filtro filtro) {
        return auditoria.consultar(filtro).stream()
                .map(a -> new EventoAuditoriaDTO(a.fecha(), a.accion(), a.resultado().name(), a.actorId(),
                        a.usuarioAfectadoId(), a.motivo(), a.dispositivoId()))
                .toList();
    }

    private static DispositivoResumen resumen(Dispositivos d) {
        return new DispositivoResumen(d.getId(), d.getPlataforma(), d.getEstado().name(), d.getFechaVinculacion(),
                d.getFechaRevocacion());
    }

    private static String vacioANull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }

    private static String normalizar(String s) {
        if (s == null) {
            return "";
        }
        return Normalizer.normalize(s.trim(), Normalizer.Form.NFD).replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT);
    }
}
