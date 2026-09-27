package upc.pe.nayrabackend.serviceinterfaces;

import upc.pe.nayrabackend.dtos.AutenticacionVozDTOs.ResultadoPaso;

/**
 * Coordinación del inicio de sesión (D-037 modificada por D-061):
 * dispositivo vinculado (D-048) → PIN (D-061) → desafío (D-054) → pipeline de voz (Python) → decisión (D-056).
 */
public interface IAutenticacionVozService {

    String PROPOSITO_INICIO_SESION = "INICIO_SESION";

    String emitirNonce(String dispositivoId);

    /** Verifica la firma del dispositivo sobre el nonce y abre una transacción de autenticación. */
    String abrirTransaccion(String dispositivoId, String nonce, String firma);

    ResultadoPaso verificarPin(String transaccionId, String pin);

    /** El PIN dictado se transcribe y luego se valida igual que el tecleado. */
    ResultadoPaso verificarPinDictado(String transaccionId, byte[] audioWav);

    ResultadoPaso verificarVoz(String transaccionId, byte[] audioWav);
}
