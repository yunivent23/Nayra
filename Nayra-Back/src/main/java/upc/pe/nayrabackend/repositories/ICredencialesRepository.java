package upc.pe.nayrabackend.repositories;

import upc.pe.nayrabackend.entities.Credenciales;

import java.time.Instant;
import java.util.Optional;
import java.util.OptionalInt;

/** Puerto de persistencia de CREDENCIALES (servicio de autenticación, v4 §2.2). Adaptador: PostgreSQL. */
public interface ICredencialesRepository {

    /** Alta de la credencial (registro, D-052 paso 9). El contador no se modifica guardando la entidad. */
    void guardar(Credenciales credencial);

    Optional<Credenciales> deUsuario(String usuarioId);

    /**
     * PIN incorrecto: incrementa {@code intentos_fallidos} de forma atómica en la base, sin pasar de {@code maximo}
     * (E-01: nunca leer, sumar en Java y guardar la entidad). Devuelve el valor del contador después del intento;
     * vacío si el usuario no tiene credencial.
     */
    OptionalInt registrarFallo(String usuarioId, int maximo, Instant ahora);

    /** Devuelve el contador a 0 con una sola sentencia (PIN correcto o desbloqueo PROVISIONAL, P-11). */
    void reiniciarIntentos(String usuarioId, Instant ahora);
}
