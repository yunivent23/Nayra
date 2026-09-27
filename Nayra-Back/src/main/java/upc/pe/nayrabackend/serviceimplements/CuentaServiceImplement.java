package upc.pe.nayrabackend.serviceimplements;

import org.springframework.stereotype.Service;
import upc.pe.nayrabackend.entities.Cuentas;
import upc.pe.nayrabackend.entities.TipoDocumentoIdentidad;
import upc.pe.nayrabackend.repositories.ICuentasRepository;
import upc.pe.nayrabackend.repositories.IRegistroIdentidadRepository;
import upc.pe.nayrabackend.serviceinterfaces.ICuentaService;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;

/** Entorno bancario simulado: no hay integración con bancos reales (D-021, D-022). */
@Service
public class CuentaServiceImplement implements ICuentaService {

    private final ICuentasRepository cuentas;
    private final IRegistroIdentidadRepository identidades;
    private final Clock reloj;

    public CuentaServiceImplement(ICuentasRepository cuentas, IRegistroIdentidadRepository identidades, Clock reloj) {
        this.cuentas = cuentas;
        this.identidades = identidades;
        this.reloj = reloj;
    }

    /** Documento → registro de identidad simulado → cuenta cuyo titular es ese registro (03 §16.4). */
    @Override
    public Optional<Cuentas> localizarPorTitular(TipoDocumentoIdentidad tipo, String numeroDocumento) {
        return identidades.porDocumento(tipo, numeroDocumento).flatMap(titular -> cuentas.porTitular(titular.id()));
    }

    @Override
    public void vincular(Cuentas cuenta, String usuarioId) {
        cuenta.vincular(usuarioId, Instant.now(reloj));
        cuentas.guardar(cuenta);
    }
}
