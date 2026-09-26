package upc.pe.nayrabackend.integracion;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

/** Reloj controlable para probar la inactividad de 5 minutos sin esperar. */
public class RelojMutable extends Clock {

    // PostgreSQL guarda microsegundos: se trunca para que los límites exactos de las pruebas sean deterministas
    private Instant ahora = Instant.now().truncatedTo(java.time.temporal.ChronoUnit.MICROS);

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
