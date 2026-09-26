package upc.pe.nayrabackend.serviceimplements;

import org.junit.jupiter.api.Test;
import upc.pe.nayrabackend.config.NayraSeguridadProperties;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class PinServiceImplementTest {

    private static PinServiceImplement servicio(String pepper) {
        return new PinServiceImplement(new NayraSeguridadProperties(
                new NayraSeguridadProperties.Pin(pepper),
                new NayraSeguridadProperties.Intentos(3),
                new NayraSeguridadProperties.Desafio(Duration.ofSeconds(90)),
                new NayraSeguridadProperties.Sesion(Duration.ofMinutes(5), Duration.ofHours(1), Duration.ofSeconds(30))));
    }

    @Test
    void soloAceptaSeisDigitos() {
        PinServiceImplement s = servicio("p");
        assertTrue(s.formatoValido("012345"));
        assertFalse(s.formatoValido("12345"));
        assertFalse(s.formatoValido("1234567"));
        assertFalse(s.formatoValido("12a456"));
        assertFalse(s.formatoValido(null));
    }

    @Test
    void usaArgon2idConParametrosAprobados() {
        String hash = servicio("p").hashear("482913");
        assertTrue(hash.startsWith("$argon2id$"), hash);
        assertTrue(hash.contains("m=19456,t=2,p=1"), hash);
        assertFalse(hash.contains("482913"));
    }

    @Test
    void verificaPinCorrectoEIncorrecto() {
        PinServiceImplement s = servicio("p");
        String hash = s.hashear("482913");
        assertTrue(s.verificar("482913", hash));
        assertFalse(s.verificar("482914", hash));
        assertFalse(s.verificar("482913", null));
    }

    @Test
    void elPepperEsNecesarioParaVerificar() {
        String hash = servicio("pepper-a").hashear("482913");
        assertFalse(servicio("pepper-b").verificar("482913", hash));
    }
}
