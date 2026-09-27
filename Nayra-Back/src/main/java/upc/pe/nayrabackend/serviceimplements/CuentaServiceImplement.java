package upc.pe.nayrabackend.serviceimplements;

import org.springframework.stereotype.Service;
import upc.pe.nayrabackend.entities.Cuentas;
import upc.pe.nayrabackend.repositories.ICuentasRepository;
import upc.pe.nayrabackend.serviceinterfaces.ICuentaService;

import java.util.Optional;

/** Entorno bancario simulado: no hay integración con bancos reales (D-021, D-022). */
@Service
public class CuentaServiceImplement implements ICuentaService {

    private final ICuentasRepository cuentas;

    public CuentaServiceImplement(ICuentasRepository cuentas) {
        this.cuentas = cuentas;
    }

    @Override
    public Optional<Cuentas> localizarPorTitular(String dni) {
        return cuentas.porTitularDni(dni);
    }

    @Override
    public void vincular(Cuentas cuenta, String usuarioId) {
        cuenta.vincular(usuarioId);
        cuentas.guardar(cuenta);
    }
}
