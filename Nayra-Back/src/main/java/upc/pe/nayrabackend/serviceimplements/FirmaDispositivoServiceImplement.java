package upc.pe.nayrabackend.serviceimplements;

import org.springframework.stereotype.Service;
import upc.pe.nayrabackend.serviceinterfaces.IFirmaDispositivoService;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.interfaces.ECPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/** D-041: verificación de firmas ECDSA P-256 del par de claves de Android Keystore. */
@Service
public class FirmaDispositivoServiceImplement implements IFirmaDispositivoService {

    // Orden del grupo de secp256r1 (P-256), identifica la curva de forma inequívoca
    private static final java.math.BigInteger ORDEN_P256 = new java.math.BigInteger(
            "FFFFFFFF00000000FFFFFFFFFFFFFFFFBCE6FAADA7179E84F3B9CAC2FC632551", 16);

    @Override
    public String validarClavePublica(String clavePublicaBase64) {
        PublicKey clave = decodificar(clavePublicaBase64);
        if (!(clave instanceof ECPublicKey ec) || !ORDEN_P256.equals(ec.getParams().getOrder())) {
            throw new IllegalArgumentException("La clave pública debe ser EC P-256");
        }
        return Base64.getEncoder().encodeToString(clave.getEncoded());
    }

    @Override
    public String mensajeCanonico(String proposito, String nonce, String dispositivoId) {
        return "NAYRA|v1|" + proposito + "|" + nonce + "|" + (dispositivoId == null ? "-" : dispositivoId);
    }

    @Override
    public boolean verificar(String clavePublicaBase64, String mensaje, String firmaBase64Url) {
        try {
            Signature verificador = Signature.getInstance(ALGORITMO);
            verificador.initVerify(decodificar(clavePublicaBase64));
            verificador.update(mensaje.getBytes(StandardCharsets.UTF_8));
            return verificador.verify(Base64.getUrlDecoder().decode(firmaBase64Url));
        } catch (GeneralSecurityException | IllegalArgumentException e) {
            return false;
        }
    }

    private PublicKey decodificar(String clavePublicaBase64) {
        try {
            byte[] der = Base64.getDecoder().decode(clavePublicaBase64);
            return KeyFactory.getInstance("EC").generatePublic(new X509EncodedKeySpec(der));
        } catch (GeneralSecurityException | IllegalArgumentException | NullPointerException e) {
            throw new IllegalArgumentException("Clave pública inválida");
        }
    }
}
