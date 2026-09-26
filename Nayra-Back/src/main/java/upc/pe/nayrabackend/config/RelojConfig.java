package upc.pe.nayrabackend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.SecureRandom;
import java.time.Clock;

@Configuration
public class RelojConfig {

    @Bean
    public Clock reloj() {
        return Clock.systemUTC();
    }

    @Bean
    public SecureRandom secureRandom() {
        return new SecureRandom();
    }
}
