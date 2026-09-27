package upc.pe.nayrabackend.repositories.memoria;

import org.springframework.stereotype.Repository;
import upc.pe.nayrabackend.entities.Cuentas;
import upc.pe.nayrabackend.entities.EntidadBancaria;
import upc.pe.nayrabackend.entities.RegistroIdentidadSimulado;
import upc.pe.nayrabackend.repositories.ICuentasRepository;
import upc.pe.nayrabackend.repositories.IEntidadesBancariasRepository;
import upc.pe.nayrabackend.repositories.IRegistroIdentidadRepository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Entorno bancario simulado en memoria (D-021, D-035): cuentas financieras, entidades bancarias y
 * registro de identidad simulado. PROVISIONAL (D-051).
 */
@Repository
public class EntornoSimuladoEnMemoria implements ICuentasRepository, IEntidadesBancariasRepository, IRegistroIdentidadRepository {

    private final Map<String, Cuentas> cuentas = new ConcurrentHashMap<>();
    private final Map<String, EntidadBancaria> entidades = new ConcurrentHashMap<>();
    private final Map<String, RegistroIdentidadSimulado> identidades = new ConcurrentHashMap<>();

    @Override
    public void guardar(Cuentas cuenta) { cuentas.put(cuenta.getId(), cuenta); }

    @Override
    public Optional<Cuentas> porTitularDni(String dni) {
        return cuentas.values().stream().filter(c -> c.getTitularDni().equals(dni)).findFirst();
    }

    @Override
    public Optional<Cuentas> porPropietario(String usuarioId) {
        return cuentas.values().stream().filter(c -> usuarioId.equals(c.getPropietarioUsuarioId())).findFirst();
    }

    @Override
    public void guardar(EntidadBancaria entidad) { entidades.put(entidad.id(), entidad); }

    @Override
    public Optional<EntidadBancaria> porId(String id) { return Optional.ofNullable(entidades.get(id)); }

    @Override
    public void guardar(RegistroIdentidadSimulado registro) { identidades.put(registro.dni(), registro); }

    @Override
    public Optional<RegistroIdentidadSimulado> porDni(String dni) { return Optional.ofNullable(dni == null ? null : identidades.get(dni)); }
}
