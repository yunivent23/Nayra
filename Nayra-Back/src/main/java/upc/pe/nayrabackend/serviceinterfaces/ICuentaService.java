package upc.pe.nayrabackend.serviceinterfaces;

import upc.pe.nayrabackend.entities.Cuentas;
import upc.pe.nayrabackend.entities.TipoDocumentoIdentidad;

import java.util.Optional;

/** Cuenta financiera simulada (D-025 a D-029, D-035). */
public interface ICuentaService {

    /** Localiza la cuenta financiera cuyo titular del registro simulado tiene ese documento (D-028, D-035). */
    Optional<Cuentas> localizarPorTitular(TipoDocumentoIdentidad tipo, String numeroDocumento);

    /** Vincula la cuenta financiera con la cuenta de acceso (FK única, D-028). */
    void vincular(Cuentas cuenta, String usuarioId);
}
