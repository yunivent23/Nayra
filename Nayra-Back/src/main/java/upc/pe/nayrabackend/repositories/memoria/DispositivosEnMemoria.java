package upc.pe.nayrabackend.repositories.memoria;

import upc.pe.nayrabackend.entities.Dispositivos;
import upc.pe.nayrabackend.repositories.IDispositivosRepository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/** Adaptador en memoria para pruebas sin base de datos. No es un bean: la aplicación usa PostgreSQL (D-051). */
public class DispositivosEnMemoria implements IDispositivosRepository {

    private final Map<String, Dispositivos> dispositivos = new ConcurrentHashMap<>();

    @Override
    public void guardar(Dispositivos dispositivo) { dispositivos.put(dispositivo.getId(), dispositivo); }

    @Override
    public Optional<Dispositivos> porId(String id) { return Optional.ofNullable(id == null ? null : dispositivos.get(id)); }

    @Override
    public Optional<Dispositivos> activoDeUsuario(String usuarioId) {
        return dispositivos.values().stream().filter(d -> d.getUsuarioId().equals(usuarioId) && d.activo()).findFirst();
    }
}
