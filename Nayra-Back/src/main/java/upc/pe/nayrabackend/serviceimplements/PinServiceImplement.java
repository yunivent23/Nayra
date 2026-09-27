package upc.pe.nayrabackend.serviceimplements;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import upc.pe.nayrabackend.serviceinterfaces.IPinService;

import java.util.regex.Pattern;

/**
 * PIN de 6 dígitos (D-061). Solo se guarda su hash; nunca texto plano, audio ni logs.
 *
 * PROVISIONAL — el algoritmo de hash y sus parámetros siguen pendientes (D-047), así como
 * el secreto del servidor (pepper) que D-061 exige. Mientras tanto se reutiliza el
 * PasswordEncoder (BCrypt) ya configurado en el proyecto. Reemplazar al cerrar D-047.
 */
@Service
public class PinServiceImplement implements IPinService {

    private static final Pattern SEIS_DIGITOS = Pattern.compile("\\d{6}");
    private final PasswordEncoder encoder;

    public PinServiceImplement(PasswordEncoder encoder) {
        this.encoder = encoder;
    }

    @Override
    public boolean formatoValido(String pin) {
        return pin != null && SEIS_DIGITOS.matcher(pin).matches();
    }

    @Override
    public String hashear(String pin) {
        if (!formatoValido(pin)) {
            throw new IllegalArgumentException("El PIN debe tener 6 dígitos.");
        }
        return encoder.encode(pin);
    }

    @Override
    public boolean coincide(String pin, String hash) {
        return formatoValido(pin) && encoder.matches(pin, hash);
    }
}
