package upc.pe.nayrabackend.serviceinterfaces;

/**
 * Credencial del PIN (servicio de autenticación; modelo de datos v4 §2.2, D-061). El hash nunca sale de este servicio.
 */
public interface ICredencialesService {

    /** Máximo de PIN incorrectos consecutivos (v4 §2.2, D-044). */
    int INTENTOS_MAXIMOS = 3;

    /** Resultado de comprobar un PIN. {@code agotados}: se alcanzó el máximo y la cuenta debe bloquearse. */
    record ResultadoPin(boolean correcto, int intentosRestantes, boolean agotados) {
    }

    /** Crea la credencial al terminar el registro (D-052, paso 9) con un hash ya calculado. */
    void crear(String usuarioId, String pinHash);

    /**
     * Comprueba el PIN. Solo un PIN incorrecto incrementa el contador; un PIN correcto lo devuelve a 0 (v4 §2.2).
     */
    ResultadoPin verificar(String usuarioId, String pin);

    /** Intentos que quedan antes del bloqueo. */
    int intentosRestantes(String usuarioId);

    /**
     * PROVISIONAL (P-11, PENDIENTE NO BLOQUEANTE): el desbloqueo administrativo reinicia el contador, igual que antes
     * de v4. Si P-11 decide lo contrario, basta con dejar de llamar a este método.
     */
    void reiniciarIntentos(String usuarioId);
}
