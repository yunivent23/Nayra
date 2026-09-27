package upc.pe.nayrabackend.repositories.memoria;

import upc.pe.nayrabackend.entities.Sesiones;
import upc.pe.nayrabackend.repositories.ISesionesRepository;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Adaptador en memoria para pruebas sin base de datos. No es un bean: la aplicación usa PostgreSQL (v4 §2.7).
 * Las comprobaciones y los cambios se hacen bajo el monitor de cada {@link Sesiones}, con la misma semántica que los
 * UPDATE condicionales del adaptador PostgreSQL (E-02).
 */
public class SesionesEnMemoria implements ISesionesRepository {

    private final Map<String, Sesiones> sesiones = new ConcurrentHashMap<>();

    @Override
    public void crear(Sesiones sesion) {
        if (sesiones.putIfAbsent(sesion.getId(), sesion) != null) {
            throw new IllegalStateException("La sesión ya existe; solo se modifica con registrarAcceso o revocar.");
        }
    }

    @Override
    public Optional<Sesiones> porId(String id) { return Optional.ofNullable(id == null ? null : sesiones.get(id)); }

    @Override
    public List<Sesiones> vigentesDeUsuario(String usuarioId) {
        return sesiones.values().stream().filter(s -> s.getUsuarioId().equals(usuarioId) && s.vigente()).toList();
    }

    @Override
    public boolean registrarAcceso(String id, Instant ahora, Instant limiteInactividad) {
        Sesiones s = id == null ? null : sesiones.get(id);
        if (s == null) {
            return false;
        }
        synchronized (s) {
            if (!s.vigente() || s.getFechaUltimoAcceso().isBefore(limiteInactividad)) {
                return false;
            }
            s.registrarAcceso(ahora);
            return true;
        }
    }

    @Override
    public boolean revocar(String id, Instant ahora) {
        Sesiones s = id == null ? null : sesiones.get(id);
        return s != null && s.revocar(ahora);
    }

    @Override
    public int revocarDeUsuario(String usuarioId, Instant ahora) {
        int revocadas = 0;
        for (Sesiones s : vigentesDeUsuario(usuarioId)) {
            if (s.revocar(ahora)) {
                revocadas++;
            }
        }
        return revocadas;
    }
}
