package upc.pe.nayrabackend.serviceinterfaces;

import upc.pe.nayrabackend.dtos.AutenticacionVozDTOs.DesafioDTO;
import upc.pe.nayrabackend.dtos.AutenticacionVozDTOs.ResultadoEnrolamiento;
import upc.pe.nayrabackend.dtos.AutenticacionVozDTOs.ResultadoMuestra;

/**
 * Enrolamiento de voz dentro del registro (D-052 paso 11; D-059): un desafío distinto por muestra.
 * Se identifica con el código del registro en curso (mecanismo PROVISIONAL del prototipo, no decisión de D-052).
 */
public interface IEnrolamientoVozService {

    DesafioDTO emitirDesafio(String codigoRegistro);

    ResultadoMuestra enviarMuestra(String codigoRegistro, String desafioId, byte[] audioWav);

    ResultadoEnrolamiento finalizar(String codigoRegistro);
}
