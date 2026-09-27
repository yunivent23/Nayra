package upc.pe.nayrabackend.entities;

import java.time.Instant;

/**
 * Evento de auditoría (AUDITORÍA, 03 §16.9; D-041).
 * Nunca contiene PIN, audio, embeddings, puntajes biométricos ni secretos (03 §16.9, D-013, D-061).
 * La IP de la propuesta original no se registra todavía; el catálogo definitivo de eventos y campos
 * sigue pendiente (D-019).
 *
 * @param actorId           usuario que ejecuta la acción (null si la ejecuta el sistema o aún no hay cuenta)
 * @param usuarioAfectadoId usuario sobre el que recae la acción, cuando corresponde
 * @param accion            código de la acción (p. ej. AUTENTICACION_VOZ, ADMIN_BLOQUEO)
 * @param motivo            código del motivo del fallo o del detalle, nunca texto libre
 */
public record Auditoria(String id, Instant fecha, String actorId, String usuarioAfectadoId, String accion,
                        Resultado resultado, String motivo, String dispositivoId) {

    public enum Resultado { EXITO, FALLO }
}
