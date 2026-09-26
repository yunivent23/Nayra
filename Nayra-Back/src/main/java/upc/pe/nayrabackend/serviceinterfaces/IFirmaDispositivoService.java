package upc.pe.nayrabackend.serviceinterfaces;

public interface IFirmaDispositivoService {

    String ALGORITMO = "SHA256withECDSA";

    /** Valida que la clave sea EC P-256 en formato X.509 (base64) y la devuelve normalizada. */
    String validarClavePublica(String clavePublicaBase64);

    /** Mensaje canónico de 04_API.md §2.4. */
    String mensajeCanonico(String proposito, String nonce, String dispositivoId);

    boolean verificar(String clavePublicaBase64, String mensaje, String firmaBase64Url);
}
