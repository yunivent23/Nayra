package upc.pe.nayrabackend.serviceinterfaces;

import java.util.List;

/**
 * Cliente del servicio Python/FastAPI (D-010).
 * CONTRATO PROVISIONAL — pendiente de docs/04_API.md (D-014).
 */
public interface IServicioVozCliente {

    /** Veredicto técnico de una etapa, tal como lo devuelve Python (D-056). */
    record Etapa(String etapa, boolean aprobada) {
    }

    /** Resultado técnico del pipeline: Python aplica los umbrales; Spring Boot decide (D-056). */
    record ResultadoTecnico(boolean aprobado, String motivo, List<Etapa> etapas, Integer muestrasValidas) {
    }

    record ResultadoEnrolamiento(boolean correcto, String motivo, int muestrasValidas) {
    }

    /** Error técnico del servicio de voz: no cuenta como intento fallido del usuario (D-010, D-044). */
    class ServicioVozNoDisponibleException extends RuntimeException {
        public ServicioVozNoDisponibleException(String mensaje, Throwable causa) {
            super(mensaje, causa);
        }
    }

    ResultadoTecnico verificar(String usuarioId, String desafio, byte[] audioWav);

    ResultadoTecnico agregarMuestraEnrolamiento(String usuarioId, String desafio, byte[] audioWav);

    ResultadoEnrolamiento finalizarEnrolamiento(String usuarioId);

    /** PROVISIONAL (D-046): transcripción del PIN dictado; vacío si no se reconoció con claridad. */
    String transcribirPin(byte[] audioWav);
}
