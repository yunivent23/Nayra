package upc.pe.nayrabackend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * El contexto sin perfil arranca sin PostgreSQL: todavía no hay tablas físicas (D-051) y la persistencia es
 * en memoria, así que se excluye la autoconfiguración JPA solo en esta prueba.
 */
@SpringBootTest(properties = {
        "spring.autoconfigure.exclude=org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration,"
                + "org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration,"
                + "org.springframework.boot.data.jpa.autoconfigure.DataJpaRepositoriesAutoConfiguration"})
class NayraBackendApplicationTests {

    @Test
    void contextLoads() {
    }

}
