package upc.pe.nayrabackend.securities;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.stereotype.Component;
import upc.pe.nayrabackend.config.NayraProperties;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Optional;

/**
 * JWT de sesión (modelo de datos v4 §4.1), emitido y validado por el servicio de autenticación.
 *
 * El JWT solo lleva el claim {@code jti} (= nayra.sesiones.id). La validez NO depende del JWT sino de la tabla
 * sesiones (revocación y 5 minutos de inactividad), así que:
 * - no lleva {@code exp}: cualquier valor fijo crearía una duración máxima, y reemitirlo sería una renovación (v4 §4.1);
 * - no hay refresh token ni renovación.
 *
 * PROVISIONAL (P-5, D-017): algoritmo HS256 (el emisor y el validador son el mismo servicio) y clave simétrica de al
 * menos 32 bytes leída del entorno (NAYRA_JWT_CLAVE, base64). Claims definitivos, algoritmo final y custodia de la
 * clave siguen pendientes. El token nunca se registra en logs ni se guarda.
 */
@Component
public class TokenSesionJwt {

    private static final Logger LOG = LoggerFactory.getLogger(TokenSesionJwt.class);
    private static final int BYTES_MINIMOS = 32;

    private final JwtEncoder encoder;
    private final JwtDecoder decoder;

    public TokenSesionJwt(NayraProperties props, SecureRandom random) {
        SecretKey clave = new SecretKeySpec(clave(props.sesion().jwtClave(), random), "HmacSHA256");
        this.encoder = new NimbusJwtEncoder(new ImmutableSecret<>(clave));
        this.decoder = NimbusJwtDecoder.withSecretKey(clave).macAlgorithm(MacAlgorithm.HS256).build();
    }

    private static byte[] clave(String base64, SecureRandom random) {
        if (base64 == null || base64.isBlank()) {
            LOG.warn("NAYRA_JWT_CLAVE no está definida: se usa una clave aleatoria en memoria (solo desarrollo; "
                    + "las sesiones no sobreviven al reinicio).");
            byte[] bytes = new byte[BYTES_MINIMOS];
            random.nextBytes(bytes);
            return bytes;
        }
        byte[] bytes = Base64.getDecoder().decode(base64.trim());
        if (bytes.length < BYTES_MINIMOS) {
            throw new IllegalStateException("NAYRA_JWT_CLAVE debe tener al menos " + BYTES_MINIMOS + " bytes (base64).");
        }
        return bytes;
    }

    /** JWT firmado con el jti de la sesión. */
    public String emitir(String sesionId) {
        JwtClaimsSet claims = JwtClaimsSet.builder().id(sesionId).build();
        return encoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();
    }

    /** jti de un JWT con firma válida; vacío si el token no es válido. No comprueba la sesión: eso lo hace la tabla. */
    public Optional<String> jti(String token) {
        try {
            return Optional.ofNullable(decoder.decode(token).getId());
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
