package upc.pe.nayrabackend.serviceimplements;

import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.stereotype.Service;
import upc.pe.nayrabackend.config.NayraSeguridadProperties;
import upc.pe.nayrabackend.serviceinterfaces.IPinService;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Base64;
import java.util.regex.Pattern;

/**
 * D-043: Argon2id (m = 19 MiB, t = 2, p = 1) sobre HMAC-SHA256(pepper, PIN).
 * El pepper vive fuera de la base de datos y del repositorio. El PIN nunca se registra.
 */
@Service
public class PinServiceImplement implements IPinService {

    private static final Pattern SEIS_DIGITOS = Pattern.compile("\\d{6}");
    private static final int SALT_BYTES = 16;
    private static final int HASH_BYTES = 32;
    private static final int PARALELISMO = 1;
    private static final int MEMORIA_KIB = 19 * 1024;
    private static final int ITERACIONES = 2;

    private final Argon2PasswordEncoder encoder =
            new Argon2PasswordEncoder(SALT_BYTES, HASH_BYTES, PARALELISMO, MEMORIA_KIB, ITERACIONES);
    private final SecretKeySpec pepper;

    public PinServiceImplement(NayraSeguridadProperties propiedades) {
        this.pepper = new SecretKeySpec(propiedades.pin().pepper().getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }

    @Override
    public boolean formatoValido(String pin) {
        return pin != null && SEIS_DIGITOS.matcher(pin).matches();
    }

    @Override
    public String hashear(String pin) {
        if (!formatoValido(pin)) {
            throw new IllegalArgumentException("PIN con formato inválido");
        }
        return encoder.encode(conPepper(pin));
    }

    @Override
    public boolean verificar(String pin, String pinHash) {
        if (!formatoValido(pin) || pinHash == null) {
            return false;
        }
        return encoder.matches(conPepper(pin), pinHash);
    }

    private String conPepper(String pin) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(pepper);
            return Base64.getEncoder().encodeToString(mac.doFinal(pin.getBytes(StandardCharsets.US_ASCII)));
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("HMAC-SHA256 no disponible", e);
        }
    }
}
