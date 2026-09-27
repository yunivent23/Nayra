package upc.pe.nayrabackend.serviceimplements;

import org.springframework.stereotype.Service;
import upc.pe.nayrabackend.config.NayraProperties;
import upc.pe.nayrabackend.entities.Auditoria;
import upc.pe.nayrabackend.entities.Dispositivos;
import upc.pe.nayrabackend.entities.Identificadores;
import upc.pe.nayrabackend.entities.Sesiones;
import upc.pe.nayrabackend.entities.Usuario;
import upc.pe.nayrabackend.repositories.IDispositivosRepository;
import upc.pe.nayrabackend.repositories.ISesionesRepository;
import upc.pe.nayrabackend.repositories.IUsuariosRepository;
import upc.pe.nayrabackend.securities.TokenSesionJwt;
import upc.pe.nayrabackend.serviceinterfaces.IAuditoriaService;
import upc.pe.nayrabackend.serviceinterfaces.ISesionesService;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

/**
 * Sesiones del servicio de autenticación (v4 §2.7 y §4.1). Ver {@link ISesionesService}.
 *
 * Cada validación comprueba en nayra.sesiones que la sesión no esté revocada y que no hayan pasado más de 5 minutos
 * desde el último acceso (D-018); si pasaron, la revoca. También la revoca si la cuenta de acceso no está ACTIVA o el
 * dispositivo ya no está activo (D-040).
 *
 * LIMITACIÓN ACTUAL: Negocio y Autenticación son hoy una sola aplicación Spring Boot. El estado y el rol del usuario
 * se leen con el repositorio de usuarios del mismo proceso; separados, Autenticación se los pediría a Negocio
 * (solo id, estado y rol, v4 §4.2).
 */
@Service
public class SesionesServiceImplement implements ISesionesService {

    private final ISesionesRepository sesiones;
    private final IUsuariosRepository usuarios;
    private final IDispositivosRepository dispositivos;
    private final IAuditoriaService auditoria;
    private final TokenSesionJwt jwt;
    private final Clock reloj;
    private final Duration inactividadMaxima;

    public SesionesServiceImplement(ISesionesRepository sesiones, IUsuariosRepository usuarios,
                                    IDispositivosRepository dispositivos, IAuditoriaService auditoria,
                                    TokenSesionJwt jwt, Clock reloj, NayraProperties props) {
        this.sesiones = sesiones;
        this.usuarios = usuarios;
        this.dispositivos = dispositivos;
        this.auditoria = auditoria;
        this.jwt = jwt;
        this.reloj = reloj;
        this.inactividadMaxima = props.sesion().inactividadMaxima();
    }

    @Override
    public String crear(String usuarioId, String dispositivoId) {
        Sesiones sesion = new Sesiones(Identificadores.nuevo(), usuarioId, dispositivoId, Instant.now(reloj));
        sesiones.crear(sesion);
        auditoria.registrar("SESION_CREADA", Auditoria.Resultado.EXITOSO, usuarioId, usuarioId, null, dispositivoId);
        return jwt.emitir(sesion.getId());
    }

    /**
     * Concurrencia (E-02): la sesión leída solo sirve para decidir. Los cambios son sentencias condicionales del
     * repositorio: si otra petición la revocó entre la lectura y el cambio, {@code registrarAcceso} no modifica nada y la
     * sesión se trata como no válida. Nunca se guarda la copia leída, así que una revocación no puede deshacerse.
     */
    @Override
    public Optional<SesionActiva> validar(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        Sesiones sesion = jwt.jti(token).flatMap(sesiones::porId).orElse(null);
        if (sesion == null || !sesion.vigente()) {
            return Optional.empty();
        }
        Instant ahora = Instant.now(reloj);
        Instant limite = ahora.minus(inactividadMaxima);
        if (sesion.getFechaUltimoAcceso().isBefore(limite)) {
            // Cierre automático por inactividad, sin aviso previo (D-018): la inactividad detectada revoca la sesión.
            if (sesiones.revocar(sesion.getId(), ahora)) {
                auditoria.exito("SESION_CERRADA_POR_INACTIVIDAD", null, sesion.getUsuarioId());
            }
            return Optional.empty();
        }
        Usuario usuario = usuarios.porId(sesion.getUsuarioId()).orElse(null);
        boolean dispositivoActivo = dispositivos.porId(sesion.getDispositivoId()).map(Dispositivos::activo).orElse(false);
        if (usuario == null || usuario.getEstado() != Usuario.Estado.ACTIVO || !dispositivoActivo) {
            sesiones.revocar(sesion.getId(), ahora);
            return Optional.empty();
        }
        if (!sesiones.registrarAcceso(sesion.getId(), ahora, limite)) {
            // Revocada o vencida por otra petición después de leerla: no se reabre.
            return Optional.empty();
        }
        return Optional.of(new SesionActiva(sesion.getId(), usuario.getId(), sesion.getDispositivoId(), usuario.getRol()));
    }

    @Override
    public void cerrar(String sesionId) {
        sesiones.porId(sesionId).ifPresent(s -> {
            if (sesiones.revocar(s.getId(), Instant.now(reloj))) {
                auditoria.exito("SESION_CERRADA", s.getUsuarioId(), s.getUsuarioId());
            }
        });
    }

    @Override
    public void revocarDeUsuario(String usuarioId) {
        sesiones.revocarDeUsuario(usuarioId, Instant.now(reloj));
    }
}
