package upc.pe.nayrabackend.serviceinterfaces;

import upc.pe.nayrabackend.entities.Auditoria;
import upc.pe.nayrabackend.entities.Auditoria.Resultado;

import java.util.List;

/**
 * Auditoría (03 §16.9, D-041). Acciones y motivos son códigos: nunca PIN, audio, embeddings,
 * puntajes biométricos ni secretos. Catálogo definitivo de eventos pendiente (D-019).
 */
public interface IAuditoriaService {

    /** Filtro de consulta; los campos nulos no filtran. */
    record Filtro(String usuarioId, String accionPrefijo, Resultado resultado) {
        public static final Filtro TODOS = new Filtro(null, null, null);
    }

    void registrar(String accion, Resultado resultado, String actorId, String usuarioAfectadoId, String motivo,
                   String dispositivoId);

    default void exito(String accion, String actorId, String usuarioAfectadoId) {
        registrar(accion, Resultado.EXITO, actorId, usuarioAfectadoId, null, null);
    }

    default void fallo(String accion, String actorId, String usuarioAfectadoId, String motivo) {
        registrar(accion, Resultado.FALLO, actorId, usuarioAfectadoId, motivo, null);
    }

    List<Auditoria> consultar(Filtro filtro);
}
