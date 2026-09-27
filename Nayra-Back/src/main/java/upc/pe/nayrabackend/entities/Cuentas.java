package upc.pe.nayrabackend.entities;

import java.math.BigDecimal;

/**
 * Cuenta financiera simulada (CUENTAS, 03 §3.3 y §16.4; D-025 a D-029, D-035).
 * Una cuenta por titular del registro de identidad simulado; el propietario (usuario de Nayra) es
 * opcional hasta la vinculación durante el registro y único una vez vinculado.
 * Entorno bancario simulado: no representa cuentas reales (D-021, D-022).
 * PROVISIONAL (D-051): sin mapeo JPA. El administrador no modifica saldos (D-041).
 */
public class Cuentas {

    private final String id;
    private final String titularDni;
    private final String entidadBancariaId;
    private final String identificador;
    private final BigDecimal saldo;
    private final String moneda;
    private String propietarioUsuarioId;

    public Cuentas(String id, String titularDni, String entidadBancariaId, String identificador,
                   BigDecimal saldo, String moneda) {
        this.id = id;
        this.titularDni = titularDni;
        this.entidadBancariaId = entidadBancariaId;
        this.identificador = identificador;
        this.saldo = saldo;
        this.moneda = moneda;
    }

    public String getId() { return id; }
    public String getTitularDni() { return titularDni; }
    public String getEntidadBancariaId() { return entidadBancariaId; }
    public String getIdentificador() { return identificador; }
    public BigDecimal getSaldo() { return saldo; }
    public String getMoneda() { return moneda; }
    public synchronized String getPropietarioUsuarioId() { return propietarioUsuarioId; }

    /** Vinculación única con la cuenta de acceso (D-028). */
    public synchronized void vincular(String usuarioId) {
        if (propietarioUsuarioId != null && !propietarioUsuarioId.equals(usuarioId)) {
            throw new IllegalStateException("CUENTA_FINANCIERA_YA_VINCULADA");
        }
        propietarioUsuarioId = usuarioId;
    }
}
