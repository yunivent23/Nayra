package upc.pe.nayrabackend.serviceimplements;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import upc.pe.nayrabackend.config.NayraSeguridadProperties;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DesafioServiceImplementTest {

    private final DesafioServiceImplement servicio = new DesafioServiceImplement(null, new SecureRandom(),
            Clock.systemUTC(), new NayraSeguridadProperties(
            new NayraSeguridadProperties.Pin("p"),
            new NayraSeguridadProperties.Intentos(3),
            new NayraSeguridadProperties.Desafio(Duration.ofSeconds(90)),
            new NayraSeguridadProperties.Sesion(Duration.ofMinutes(5), Duration.ofHours(1), Duration.ofSeconds(30))));

    @RepeatedTest(200)
    void formatoPalabraTresDigitosPalabra() {
        List<String> e = servicio.generarElementos();
        assertEquals(5, e.size());
        assertTrue(servicio.palabras().contains(e.get(0)));
        assertTrue(DesafioServiceImplement.DIGITOS.containsAll(e.subList(1, 4)));
        assertTrue(servicio.palabras().contains(e.get(4)));
    }

    @Test
    void listaDePalabrasSinDigitosNiDuplicados() {
        List<String> p = servicio.palabras();
        assertEquals(p.size(), p.stream().distinct().count());
        assertTrue(p.stream().noneMatch(DesafioServiceImplement.DIGITOS::contains));
        assertTrue(p.stream().allMatch(w -> w.matches("[a-zñ]+")), "sin tildes ni mayúsculas");
    }
}
