package upc.pe.nayrabackend.integracion;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

/** Reloj controlable para probar la inactividad de 5 minutos sin esperar. */
public class RelojMutable extends Clock {

    private Instant ahora = Instant.now();

    public void avanzar(Duration d) {
        ahora = ahora.plus(d);
    }

    @Override
    public ZoneId getZone() {
        return ZoneOffset.UTC;
    }

    @Override
    public Clock withZone(ZoneId zone) {
        return this;
    }

    @Override
    public Instant instant() {
        return ahora;
    }
}
