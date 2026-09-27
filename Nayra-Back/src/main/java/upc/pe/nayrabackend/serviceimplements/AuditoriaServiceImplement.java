package upc.pe.nayrabackend.serviceimplements;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import upc.pe.nayrabackend.entities.Auditoria;
import upc.pe.nayrabackend.entities.Auditoria.Resultado;
import upc.pe.nayrabackend.repositories.IAuditoriaRepository;
import upc.pe.nayrabackend.serviceinterfaces.IAuditoriaService;

import java.time.Clock;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Registro de auditoría. Solo acepta códigos en mayúsculas para la acción y el motivo: cualquier otro
 * texto (p. ej. un PIN o un mensaje libre recibido de otro servicio) se reemplaza por
 * MOTIVO_NO_RECONOCIDO, de modo que un dato sensible nunca llegue a la auditoría ni a los logs.
 * Persistencia en memoria: PROVISIONAL (D-051).
 */
@Service
public class AuditoriaServiceImplement implements IAuditoriaService {

    static final String MOTIVO_NO_RECONOCIDO = "MOTIVO_NO_RECONOCIDO";
    private static final Pattern CODIGO = Pattern.compile("[A-Z][A-Z_]{0,63}");
    private static final Logger log = LoggerFactory.getLogger(AuditoriaServiceImplement.class);

    private final IAuditoriaRepository repositorio;
    private final Clock reloj;

    public AuditoriaServiceImplement(IAuditoriaRepository repositorio, Clock reloj) {
        this.repositorio = repositorio;
        this.reloj = reloj;
    }

    @Override
    public void registrar(String accion, Resultado resultado, String actorId, String usuarioAfectadoId, String motivo,
                          String dispositivoId) {
        if (accion == null || !CODIGO.matcher(accion).matches()) {
            throw new IllegalArgumentException("Acción de auditoría no válida.");
        }
        String motivoSeguro = motivo == null ? null : (CODIGO.matcher(motivo).matches() ? motivo : MOTIVO_NO_RECONOCIDO);
        Auditoria evento = new Auditoria(UUID.randomUUID().toString(), Instant.now(reloj), actorId, usuarioAfectadoId,
                accion, resultado, motivoSeguro, dispositivoId);
        repositorio.guardar(evento);
        log.info("auditoria accion={} resultado={} actor={} afectado={} motivo={}", accion, resultado, actorId,
                usuarioAfectadoId, motivoSeguro);
    }

    @Override
    public List<Auditoria> consultar(Filtro f) {
        return repositorio.todos().stream()
                .filter(e -> f.usuarioId() == null || f.usuarioId().equals(e.usuarioAfectadoId()) || f.usuarioId().equals(e.actorId()))
                .filter(e -> f.accionPrefijo() == null || e.accion().startsWith(f.accionPrefijo()))
                .filter(e -> f.resultado() == null || f.resultado() == e.resultado())
                .sorted(Comparator.comparing(Auditoria::fecha).reversed())
                .toList();
    }
}
