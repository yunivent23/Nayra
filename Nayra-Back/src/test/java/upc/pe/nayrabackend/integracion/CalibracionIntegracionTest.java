package upc.pe.nayrabackend.integracion;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import upc.pe.nayrabackend.config.NayraUmbralesProperties;
import upc.pe.nayrabackend.exceptions.CodigoError;
import upc.pe.nayrabackend.exceptions.NayraException;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

/** D-038: sin umbrales aprobados el sistema queda en modo calibración y no autentica por voz. */
@SpringBootTest
@ActiveProfiles("it")
@EnabledIfEnvironmentVariable(named = "NAYRA_IT_DB_URL", matches = ".+")
class CalibracionIntegracionTest {

    @Autowired NayraUmbralesProperties umbrales;
    @Autowired Flyway flyway;

    @Test
    void sinUmbralesConfiguradosNoHayDecisionPorVoz() {
        assertFalse(umbrales.completosParaVerificacion());
        assertFalse(umbrales.completosParaEnrolamiento());
        assertEquals(503, new NayraException(CodigoError.CALIBRACION).getEstado().value());
    }
}
