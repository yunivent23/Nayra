package upc.pe.nayrabackend.dtos;

/** DTOs del flujo de voz del prototipo (AG-13). Contrato PROVISIONAL — pendiente de docs/04_API.md (D-014). */
public final class AutenticacionVozDTOs {

    private AutenticacionVozDTOs() {
    }

    public record SolicitudNonce(String dispositivoId) {
    }

    public record RespuestaNonce(String nonce, String proposito) {
    }

    public record SolicitudTransaccion(String dispositivoId, String nonce, String firma) {
    }

    public record RespuestaTransaccion(String transaccionId) {
    }

    public record SolicitudPin(String pin) {
    }

    public record DesafioDTO(String desafioId, String texto) {
    }

    /**
     * Resultado del paso de autenticación, decidido por Spring Boot (D-056).
     * estado: CONTINUAR (pasar al desafío), AUTENTICADO, REINTENTAR, BLOQUEADA, SERVICIO_NO_DISPONIBLE, RECHAZADO.
     * No incluye puntajes biométricos.
     * sesion: token de sesión, solo cuando estado = AUTENTICADO (mecanismo PROVISIONAL, D-018).
     */
    public record ResultadoPaso(String estado, String motivo, Integer intentosRestantes, DesafioDTO desafio, String sesion) {
        public ResultadoPaso(String estado, String motivo, Integer intentosRestantes, DesafioDTO desafio) {
            this(estado, motivo, intentosRestantes, desafio, null);
        }
    }

    public record ResultadoMuestra(boolean aceptada, String motivo, Integer muestrasValidas, int muestrasRequeridas) {
    }

    public record ResultadoEnrolamiento(boolean correcto, String motivo, int muestrasValidas) {
    }
}
