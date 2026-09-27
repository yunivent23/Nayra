package upc.pe.nayrabackend.repositories.memoria;

import upc.pe.nayrabackend.entities.Credenciales;
import upc.pe.nayrabackend.repositories.ICredencialesRepository;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Adaptador en memoria para pruebas sin base de datos. No es un bean: la aplicación usa PostgreSQL (v4 §2.2).
 * El incremento es atómico porque los métodos de {@link Credenciales} están sincronizados.
 */
public class CredencialesEnMemoria implements ICredencialesRepository {

    private final Map<String, Credenciales> porUsuario = new ConcurrentHashMap<>();

    @Override
    public void guardar(Credenciales credencial) { porUsuario.put(credencial.getUsuarioId(), credencial); }

    @Override
    public Optional<Credenciales> deUsuario(String usuarioId) {
        return Optional.ofNullable(usuarioId == null ? null : porUsuario.get(usuarioId));
    }

    @Override
    public OptionalInt registrarFallo(String usuarioId, int maximo, Instant ahora) {
        return deUsuario(usuarioId).map(c -> OptionalInt.of(c.registrarFallo(maximo, ahora))).orElseGet(OptionalInt::empty);
    }

    @Override
    public void reiniciarIntentos(String usuarioId, Instant ahora) {
        deUsuario(usuarioId).ifPresent(c -> c.reiniciarIntentos(ahora));
    }
}
