package upc.pe.nayrabackend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import upc.pe.nayrabackend.soporte.BaseDeDatosDePrueba;

/**
 * El contexto completo arranca contra PostgreSQL: Flyway aplica las migraciones del esquema nayra y Hibernate
 * valida que las entidades coinciden con las tablas (ddl-auto=validate, D-051).
 */
@SpringBootTest
@BaseDeDatosDePrueba
@ActiveProfiles("pruebasbd")
class NayraBackendApplicationTests {

    @Test
    void contextLoads() {
    }

}
