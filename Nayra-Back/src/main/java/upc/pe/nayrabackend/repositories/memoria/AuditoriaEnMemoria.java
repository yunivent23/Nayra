package upc.pe.nayrabackend.repositories.memoria;

import upc.pe.nayrabackend.entities.Auditoria;
import upc.pe.nayrabackend.entities.Auditoria.Resultado;
import upc.pe.nayrabackend.repositories.IAuditoriaRepository;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Adaptador en memoria para pruebas sin base de datos. No es un bean: la aplicación usa PostgreSQL (D-051).
 * Solo inserción; los eventos no se modifican ni se eliminan.
 */
public class AuditoriaEnMemoria implements IAuditoriaRepository {

    private final List<Auditoria> eventos = new CopyOnWriteArrayList<>();

    @Override
    public void guardar(Auditoria evento) { eventos.add(evento); }

    @Override
    public List<Auditoria> consultar(String usuarioId, String accionPrefijo, Resultado resultado) {
        return eventos.stream()
                .filter(e -> usuarioId == null || usuarioId.equals(e.usuarioAfectadoId()) || usuarioId.equals(e.actorId()))
                .filter(e -> accionPrefijo == null || e.accion().startsWith(accionPrefijo))
                .filter(e -> resultado == null || resultado == e.resultado())
                .sorted(Comparator.comparing(Auditoria::fecha).reversed())
                .toList();
    }

    /** Todos los eventos en orden de inserción (solo para pruebas). */
    public List<Auditoria> todos() { return List.copyOf(eventos); }
}
