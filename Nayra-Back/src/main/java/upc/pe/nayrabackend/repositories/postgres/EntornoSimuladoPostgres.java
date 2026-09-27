package upc.pe.nayrabackend.repositories.postgres;

import org.springframework.stereotype.Repository;
import upc.pe.nayrabackend.entities.Cuentas;
import upc.pe.nayrabackend.entities.EntidadBancaria;
import upc.pe.nayrabackend.entities.Identificadores;
import upc.pe.nayrabackend.entities.RegistroIdentidadSimulado;
import upc.pe.nayrabackend.entities.TipoDocumentoIdentidad;
import upc.pe.nayrabackend.repositories.ICuentasRepository;
import upc.pe.nayrabackend.repositories.IEntidadesBancariasRepository;
import upc.pe.nayrabackend.repositories.IRegistroIdentidadRepository;

import java.util.Optional;

/**
 * Adaptador PostgreSQL del entorno bancario simulado (D-021, D-035, D-051): nayra.cuentas,
 * nayra.entidades_bancarias y nayra.registro_identidad_simulado.
 */
@Repository
public class EntornoSimuladoPostgres implements ICuentasRepository, IEntidadesBancariasRepository, IRegistroIdentidadRepository {

    private final CuentaJpaRepository cuentas;
    private final EntidadBancariaJpaRepository entidades;
    private final RegistroIdentidadJpaRepository identidades;

    EntornoSimuladoPostgres(CuentaJpaRepository cuentas, EntidadBancariaJpaRepository entidades,
                            RegistroIdentidadJpaRepository identidades) {
        this.cuentas = cuentas;
        this.entidades = entidades;
        this.identidades = identidades;
    }

    @Override
    public void guardar(Cuentas cuenta) { cuentas.saveAndFlush(cuenta); }

    @Override
    public Optional<Cuentas> porTitular(String titularId) { return Identificadores.leer(titularId).flatMap(cuentas::findByTitularId); }

    @Override
    public Optional<Cuentas> porPropietario(String usuarioId) {
        return Identificadores.leer(usuarioId).flatMap(cuentas::findByPropietarioId);
    }

    @Override
    public void guardar(EntidadBancaria entidad) { entidades.saveAndFlush(entidad); }

    @Override
    public Optional<EntidadBancaria> porId(String id) { return Identificadores.leer(id).flatMap(entidades::findById); }

    @Override
    public void guardar(RegistroIdentidadSimulado registro) { identidades.saveAndFlush(registro); }

    @Override
    public Optional<RegistroIdentidadSimulado> porDocumento(TipoDocumentoIdentidad tipo, String numero) {
        return tipo == null || numero == null ? Optional.empty()
                : identidades.findByTipoDocumentoIdentidadAndNumeroDocumento(tipo, numero);
    }
}
