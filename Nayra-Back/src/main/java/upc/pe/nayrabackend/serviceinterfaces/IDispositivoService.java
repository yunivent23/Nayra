package upc.pe.nayrabackend.serviceinterfaces;

import upc.pe.nayrabackend.entities.Dispositivos;

import java.security.PublicKey;

/** Dispositivo vinculado con par de claves (D-039, D-048). */
public interface IDispositivoService {

    /** Valida la clave pública X.509/SPKI en Base64; solo se acepta EC P-256 (secp256r1). */
    PublicKey leerClavePublica(String clavePublicaBase64);

    /** Vincula un dispositivo ACTIVO y revoca el anterior del mismo usuario (D-039). */
    Dispositivos vincular(String usuarioId, PublicKey clavePublica);

    /** Revoca el dispositivo activo del usuario, si existe (D-040, HU-125). */
    void revocarActivo(String usuarioId);

    /** Si existe un dispositivo con ese identificador (activo o revocado). */
    boolean existe(String dispositivoId);

    /** Nonce aleatorio de un solo uso y vida corta para el dispositivo (D-048). */
    String emitirNonce(String dispositivoId);

    /**
     * Verifica la firma ECDSA P-256/SHA-256 sobre "nonce|dispositivoId|proposito" y consume el nonce.
     *
     * @return el identificador del usuario propietario del dispositivo
     * @throws SecurityException si el nonce no existe, venció o ya se usó, o la firma no es válida
     */
    String verificarFirma(String dispositivoId, String nonce, String firmaBase64, String proposito);
}
