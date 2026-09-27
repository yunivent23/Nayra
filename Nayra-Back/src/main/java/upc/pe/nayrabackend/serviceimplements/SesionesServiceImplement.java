package upc.pe.nayrabackend.serviceimplements;

import org.springframework.stereotype.Service;
import upc.pe.nayrabackend.config.NayraProperties;
import upc.pe.nayrabackend.entities.Auditoria;
import upc.pe.nayrabackend.entities.Dispositivos;
import upc.pe.nayrabackend.entities.Sesiones;
import upc.pe.nayrabackend.entities.Usuario;
import upc.pe.nayrabackend.repositories.IDispositivosRepository;
import upc.pe.nayrabackend.repositories.ISesionesRepository;
import upc.pe.nayrabackend.repositories.IUsuariosRepository;
import upc.pe.nayrabackend.serviceinterfaces.IAuditoriaService;
import upc.pe.nayrabackend.serviceinterfaces.ISesionesService;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementación PROVISIONAL de sesiones (D-018 pendiente en lo técnico). Ver {@link ISesionesService}.
 * La regla de 5 minutos de inactividad (aprobada) se aplica en cada validación en el servidor.
 */
@Service
public class SesionesServiceImplement implements ISesionesService {

    private final ISesionesRepository sesiones;
    private final IUsuariosRepository usuarios;
    private final IDispositivosRepository dispositivos;
    private final IAuditoriaService auditoria;
    private final SecureRandom random;
    private final Clock reloj;
    private final Duration inactividadMaxima;

    public SesionesServiceImplement(ISesionesRepository sesiones, IUsuariosRepository usuarios,
                                    IDispositivosRepository dispositivos, IAuditoriaService auditoria,
                                    SecureRandom random, Clock reloj, NayraProperties props) {
        this.sesiones = sesiones;
        this.usuarios = usuarios;
        this.dispositivos = dispositivos;
        this.auditoria = auditoria;
        this.random = random;
        this.reloj = reloj;
        this.inactividadMaxima = props.sesion().inactividadMaxima();
    }

    @Override
    public String crear(String usuarioId, String dispositivoId) {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        Sesiones sesion = new Sesiones(UUID.randomUUID().toString(), hash(token), usuarioId, dispositivoId, Instant.now(reloj));
        sesiones.guardar(sesion);
        auditoria.registrar("SESION_CREADA", Auditoria.Resultado.EXITO, usuarioId, usuarioId, null, dispositivoId);
        return token;
    }

    @Override
    public Optional<SesionActiva> validar(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        Sesiones sesion = sesiones.porTokenHash(hash(token)).orElse(null);
        if (sesion == null || !sesion.vigente()) {
            return Optional.empty();
        }
        Instant ahora = Instant.now(reloj);
        if (Duration.between(sesion.getUltimoAcceso(), ahora).compareTo(inactividadMaxima) > 0) {
            // Cierre automático por inactividad, sin aviso previo (D-018).
            sesion.revocar(ahora, "INACTIVIDAD");
            auditoria.exito("SESION_CERRADA_POR_INACTIVIDAD", null, sesion.getUsuarioId());
            return Optional.empty();
        }
        Usuario usuario = usuarios.porId(sesion.getUsuarioId()).orElse(null);
        boolean dispositivoActivo = dispositivos.porId(sesion.getDispositivoId()).map(Dispositivos::activo).orElse(false);
        if (usuario == null || usuario.getEstado() != Usuario.Estado.ACTIVA || !dispositivoActivo) {
            sesion.revocar(ahora, "CUENTA_O_DISPOSITIVO_NO_ACTIVO");
            return Optional.empty();
        }
        sesion.registrarAcceso(ahora);
        return Optional.of(new SesionActiva(sesion.getId(), usuario.getId(), sesion.getDispositivoId(), usuario.getRol()));
    }

    @Override
    public void cerrar(String sesionId) {
        sesiones.porId(sesionId).ifPresent(s -> {
            s.revocar(Instant.now(reloj), "CIERRE_POR_USUARIO");
            auditoria.exito("SESION_CERRADA", s.getUsuarioId(), s.getUsuarioId());
        });
    }

    @Override
    public void revocarDeUsuario(String usuarioId, String motivo) {
        Instant ahora = Instant.now(reloj);
        sesiones.deUsuario(usuarioId).stream().filter(Sesiones::vigente).forEach(s -> s.revocar(ahora, motivo));
    }

    static String hash(String token) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
