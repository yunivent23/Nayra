package upc.pe.nayrabackend.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.SecureRandom;
import java.time.Clock;

@Configuration
@EnableConfigurationProperties(NayraProperties.class)
public class ConfiguracionGeneral {

    @Bean
    public Clock reloj() {
        return Clock.systemUTC();
    }

    /** Fuente de aleatoriedad para desafíos, nonces, códigos de registro y tokens de sesión (D-054, D-048). */
    @Bean
    public SecureRandom secureRandom() {
        return new SecureRandom();
    }
}
