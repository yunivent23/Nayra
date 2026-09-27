package upc.pe.nayrabackend.soporte;

import org.flywaydb.core.Flyway;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

/**
 * Bases auxiliares para las pruebas que ejecutan Flyway a mano (migración de datos y permisos por servicio). Se crean
 * junto a la base de NAYRA_TEST_DB_URL, en el mismo servidor, para no interferir con el contexto de Spring que usa
 * esa base. Requieren un usuario con CREATEDB y CREATEROLE (en local, postgres).
 */
public final class BaseAuxiliar {

    public static final String UBICACION_NAYRA = "classpath:db/migration/nayra";
    /** Historial propio del esquema biométrico (D-051): SQL del servicio Python, en formato Flyway. */
    public static final String UBICACION_BIOMETRIA = "filesystem:../Nayra-Voz/migraciones/biometria";

    private BaseAuxiliar() {
    }

    public static String usuario() {
        String u = System.getenv("NAYRA_TEST_DB_USER");
        return u == null || u.isBlank() ? "postgres" : u;
    }

    public static String contrasena() {
        String p = System.getenv("NAYRA_TEST_DB_PASSWORD");
        return p == null ? "" : p;
    }

    public static String url(String base) {
        String url = System.getenv("NAYRA_TEST_DB_URL");
        int barra = url.lastIndexOf('/');
        int consulta = url.indexOf('?', barra);
        return url.substring(0, barra + 1) + base + (consulta < 0 ? "" : url.substring(consulta));
    }

    public static Connection conectar(String base) throws SQLException {
        return DriverManager.getConnection(url(base), usuario(), contrasena());
    }

    /** Borra y crea de nuevo la base auxiliar. */
    public static void recrear(String base) throws SQLException {
        try (Connection c = DriverManager.getConnection(System.getenv("NAYRA_TEST_DB_URL"), usuario(), contrasena());
             Statement s = c.createStatement()) {
            s.execute("DROP DATABASE IF EXISTS " + base + " WITH (FORCE)");
            s.execute("CREATE DATABASE " + base);
        }
    }

    public static Flyway flywayNayra(String base, Map<String, String> marcadores, String objetivo) {
        var config = Flyway.configure().dataSource(url(base), usuario(), contrasena())
                .locations(UBICACION_NAYRA).schemas("nayra").defaultSchema("nayra").placeholders(marcadores);
        if (objetivo != null) {
            config.target(objetivo);
        }
        return config.load();
    }

    public static Flyway flywayBiometria(String base, Map<String, String> marcadores) {
        return Flyway.configure().dataSource(url(base), usuario(), contrasena())
                .locations(UBICACION_BIOMETRIA).schemas("biometria").defaultSchema("biometria").placeholders(marcadores).load();
    }
}
