package upc.pe.nayrabackend.repositories.memoria;

import upc.pe.nayrabackend.entities.Cuentas;
import upc.pe.nayrabackend.entities.EntidadBancaria;
import upc.pe.nayrabackend.entities.RegistroIdentidadSimulado;
import upc.pe.nayrabackend.entities.TipoDocumentoIdentidad;
import upc.pe.nayrabackend.repositories.ICuentasRepository;
import upc.pe.nayrabackend.repositories.IEntidadesBancariasRepository;
import upc.pe.nayrabackend.repositories.IRegistroIdentidadRepository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Entorno bancario simulado en memoria (D-021, D-035): cuentas financieras, entidades bancarias y registro de
 * identidad simulado. Adaptador para pruebas sin base de datos; no es un bean: la aplicación usa PostgreSQL (D-051).
 */
public class EntornoSimuladoEnMemoria implements ICuentasRepository, IEntidadesBancariasRepository, IRegistroIdentidadRepository {

    private final Map<String, Cuentas> cuentas = new ConcurrentHashMap<>();
    private final Map<String, EntidadBancaria> entidades = new ConcurrentHashMap<>();
    private final Map<String, RegistroIdentidadSimulado> identidades = new ConcurrentHashMap<>();

    @Override
    public void guardar(Cuentas cuenta) { cuentas.put(cuenta.getId(), cuenta); }

    @Override
    public Optional<Cuentas> porTitular(String titularId) {
        return cuentas.values().stream().filter(c -> c.getTitularId().equals(titularId)).findFirst();
    }

    @Override
    public Optional<Cuentas> porPropietario(String usuarioId) {
        return cuentas.values().stream().filter(c -> usuarioId.equals(c.getPropietarioUsuarioId())).findFirst();
    }

    @Override
    public void guardar(EntidadBancaria entidad) { entidades.put(entidad.id(), entidad); }

    @Override
    public Optional<EntidadBancaria> porId(String id) { return Optional.ofNullable(id == null ? null : entidades.get(id)); }

    @Override
    public void guardar(RegistroIdentidadSimulado registro) { identidades.put(registro.id(), registro); }

    @Override
    public Optional<RegistroIdentidadSimulado> porDocumento(TipoDocumentoIdentidad tipo, String numero) {
        return identidades.values().stream()
                .filter(r -> r.tipoDocumentoIdentidad() == tipo && r.numeroDocumento().equals(numero)).findFirst();
    }
}
