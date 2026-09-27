package upc.pe.nayrabackend.soporte;

import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.boot.flyway.autoconfigure.FlywayMigrationStrategy;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Pruebas con Spring y PostgreSQL real (D-051). Se ejecutan solo si existe la variable NAYRA_TEST_DB_URL; la base
 * se vacía y se migra de nuevo con Flyway al crear cada contexto, así que debe ser una base exclusiva de pruebas.
 * La clase de prueba activa el perfil "pruebasbd" (con @ActiveProfiles).
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@EnabledIfEnvironmentVariable(named = "NAYRA_TEST_DB_URL", matches = ".+",
        disabledReason = "Requiere PostgreSQL: definir NAYRA_TEST_DB_URL (base exclusiva de pruebas)")
@Import(BaseDeDatosDePrueba.Limpieza.class)
public @interface BaseDeDatosDePrueba {

    @TestConfiguration
    class Limpieza {
        @Bean
        FlywayMigrationStrategy limpiarYMigrar() {
            return flyway -> {
                flyway.clean();
                flyway.migrate();
            };
        }
    }
}
