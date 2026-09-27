package upc.pe.nayrabackend.serviceinterfaces;

import java.time.Instant;
import java.util.Optional;

/** Desafío de voz (D-054): palabra + 3 dígitos + palabra, un solo uso, ligado a la cuenta y al contexto. */
public interface IDesafioService {

    enum Contexto { INICIO_SESION, CAMBIO_DISPOSITIVO, ENROLAMIENTO }

    record Desafio(String id, String texto, String cuentaId, Contexto contexto, Instant expira) {
    }

    Desafio emitir(String cuentaId, Contexto contexto);

    /** Consume el desafío (enviar un audio lo consume, D-054). Vacío si no existe, venció o no corresponde. */
    Optional<Desafio> consumir(String desafioId, String cuentaId, Contexto contexto);
}
