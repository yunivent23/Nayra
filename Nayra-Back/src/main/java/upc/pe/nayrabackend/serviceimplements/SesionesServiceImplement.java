package upc.pe.nayrabackend.serviceimplements;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import upc.pe.nayrabackend.config.NayraSeguridadProperties;
import upc.pe.nayrabackend.entities.Dispositivos;
import upc.pe.nayrabackend.entities.Sesiones;
import upc.pe.nayrabackend.entities.Users;
import upc.pe.nayrabackend.repositories.ISesionesRepository;
import upc.pe.nayrabackend.serviceinterfaces.ISesionesService;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** D-042: token opaco de 256 bits, hash SHA-256 en SESIONES, 5 min de inactividad en el servidor. */
@Service
public class SesionesServiceImplement implements ISesionesService {

    private static final int BYTES_TOKEN = 32;

    private final ISesionesRepository repository;
    private final SecureRandom random;
    private final Clock reloj;
    private final NayraSeguridadProperties.Sesion config;

    public SesionesServiceImplement(ISesionesRepository repository, SecureRandom random, Clock reloj,
                                    NayraSeguridadProperties propiedades) {
        this.repository = repository;
        this.random = random;
        this.reloj = reloj;
        this.config = propiedades.sesion();
    }

    @Override
    @Transactional
    public String crear(Users usuario, Dispositivos dispositivo) {
        byte[] bytes = new byte[BYTES_TOKEN];
        random.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        OffsetDateTime ahora = OffsetDateTime.now(reloj);

        Sesiones s = new Sesiones();
        s.setIdPublico(UUID.randomUUID());
        s.setUsuario(usuario);
        s.setDispositivo(dispositivo);
        s.setTokenHash(hash(token));
        s.setFechaInicio(ahora);
        s.setUltimoAcceso(ahora);
        repository.save(s);
        return token;
    }

    @Override
    @Transactional
    public Optional<Sesiones> validar(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        Optional<Sesiones> encontrada = repository.buscarAbiertaPorTokenHash(hash(token));
        if (encontrada.isEmpty()) {
            return Optional.empty();
        }
        Sesiones s = encontrada.get();
        OffsetDateTime ahora = OffsetDateTime.now(reloj);
        if (Duration.between(s.getUltimoAcceso(), ahora).compareTo(config.inactividad()) >= 0) {
            cerrar(s, INACTIVIDAD, ahora);
            return Optional.empty();
        }
        if (Duration.between(s.getFechaInicio(), ahora).compareTo(config.duracionMaxima()) >= 0) {
            cerrar(s, DURACION_MAXIMA, ahora);
            return Optional.empty();
        }
        // Se escribe como máximo una vez por intervalo; la sesión puede cerrarse antes, nunca después, de los 5 min
        if (Duration.between(s.getUltimoAcceso(), ahora).compareTo(config.intervaloActualizacion()) >= 0) {
            repository.registrarAcceso(s.getId(), ahora);
            s.setUltimoAcceso(ahora);
        }
        return Optional.of(s);
    }

    @Override
    @Transactional
    public void cerrar(Long sesionId, String motivo) {
        repository.findById(sesionId)
                .filter(s -> s.getFechaCierre() == null)
                .ifPresent(s -> cerrar(s, motivo, OffsetDateTime.now(reloj)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Sesiones> listarAbiertas(Long usuarioId) {
        return repository.listarAbiertas(usuarioId);
    }

    @Override
    @Transactional
    public boolean cerrarPropia(UUID idPublico, Long usuarioId) {
        return repository.findByIdPublicoAndUsuarioIdAndFechaCierreIsNull(idPublico, usuarioId)
                .map(s -> {
                    cerrar(s, LOGOUT, OffsetDateTime.now(reloj));
                    return true;
                })
                .orElse(false);
    }

    private void cerrar(Sesiones s, String motivo, OffsetDateTime ahora) {
        s.setFechaCierre(ahora);
        s.setMotivoCierre(motivo);
        repository.save(s);
    }

    static String hash(String token) {
        try {
            byte[] d = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.US_ASCII));
            return HexFormat.of().formatHex(d);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
