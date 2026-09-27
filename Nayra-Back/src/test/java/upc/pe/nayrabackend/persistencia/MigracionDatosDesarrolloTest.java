package upc.pe.nayrabackend.persistencia;

import org.flywaydb.core.api.FlywayException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import upc.pe.nayrabackend.soporte.BaseAuxiliar;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Migración de una base con datos de desarrollo creada con V001–V004 al modelo v4 (V005–V011): los hashes del PIN y
 * los contadores pasan a credenciales y se verifican antes de borrar las columnas de usuarios (v4 §2.1 y §2.2).
 */
@EnabledIfEnvironmentVariable(named = "NAYRA_TEST_DB_URL", matches = ".+",
        disabledReason = "Requiere PostgreSQL: definir NAYRA_TEST_DB_URL (base exclusiva de pruebas)")
class MigracionDatosDesarrolloTest {

    static final String BASE = "nayra_test_migracion";
    static final Map<String, String> MARCADORES = Map.of("rol_ejecucion", "nayra_app", "rol_negocio", "nayra_negocio",
            "rol_autenticacion", "nayra_autenticacion");

    static final String U_ACTIVO = "11111111-1111-4111-8111-111111111111";
    static final String U_BLOQUEADO = "22222222-2222-4222-8222-222222222222";
    static final String ENTIDAD = "33333333-3333-4333-8333-333333333333";

    @AfterAll
    static void limpiar() throws Exception {
        try (Connection c = java.sql.DriverManager.getConnection(System.getenv("NAYRA_TEST_DB_URL"), BaseAuxiliar.usuario(),
                BaseAuxiliar.contrasena()); Statement s = c.createStatement()) {
            s.execute("DROP DATABASE IF EXISTS " + BASE + " WITH (FORCE)");
        }
    }

    /** Base en V004 con dos usuarios, dos cuentas y un dispositivo de la plataforma indicada. */
    private static void baseEnV004(String plataforma) throws Exception {
        BaseAuxiliar.recrear(BASE);
        BaseAuxiliar.flywayNayra(BASE, MARCADORES, "4").migrate();
        try (Connection c = BaseAuxiliar.conectar(BASE); Statement s = c.createStatement()) {
            s.execute("INSERT INTO nayra.usuarios (id, dni, nombres, apellidos, numero_celular, pin_hash, rol, estado, fecha_creacion, "
                    + "fecha_actualizacion, intentos_fallidos) VALUES "
                    + "('" + U_ACTIVO + "', '10000002', 'Persona', 'Dos', '987654321', '$2a$04$hashActivo', 'USER', 'ACTIVA', now(), now(), 1),"
                    + "('" + U_BLOQUEADO + "', '10000003', 'Persona', 'Tres', '987654321', '$2a$04$hashBloqueado', 'ADMIN', 'BLOQUEADA', now(), now(), 3)");
            s.execute("INSERT INTO nayra.entidades_bancarias VALUES ('" + ENTIDAD + "', 'Entidad')");
            s.execute("INSERT INTO nayra.registro_identidad_simulado VALUES "
                    + "('44444444-4444-4444-8444-444444444441', '10000002', 'Persona', 'Dos'),"
                    + "('44444444-4444-4444-8444-444444444442', '10000003', 'Persona', 'Tres')");
            s.execute("INSERT INTO nayra.cuentas (id, titular_id, propietario_id, entidad_bancaria_id, codigo_cuenta, saldo, moneda, "
                    + "fecha_creacion, fecha_actualizacion) VALUES "
                    + "(gen_random_uuid(), '44444444-4444-4444-8444-444444444441', '" + U_ACTIVO + "', '" + ENTIDAD + "', 'SIM-1', 10, 'PEN', now(), now()),"
                    + "(gen_random_uuid(), '44444444-4444-4444-8444-444444444442', NULL, '" + ENTIDAD + "', 'SIM-2', 20, 'PEN', now(), now())");
            s.execute("INSERT INTO nayra.dispositivos (id, usuario_id, clave_publica, plataforma, fecha_vinculacion) VALUES "
                    + "(gen_random_uuid(), '" + U_ACTIVO + "', '\\x00', '" + plataforma + "', now())");
        }
    }

    @Test
    void losDatosDeDesarrolloPasanACredencialesSinPerderse() throws Exception {
        baseEnV004("ANDROID");
        BaseAuxiliar.flywayNayra(BASE, MARCADORES, null).migrate();
        try (Connection c = BaseAuxiliar.conectar(BASE); Statement s = c.createStatement()) {
            ResultSet r = s.executeQuery("SELECT u.id::text, u.tipo_documento_identidad, u.numero_documento, u.estado, c.pin_hash, "
                    + "c.intentos_fallidos FROM nayra.usuarios u JOIN nayra.credenciales c ON c.usuario_id = u.id ORDER BY u.numero_documento");
            assertTrue(r.next());
            assertEquals(U_ACTIVO, r.getString(1));
            assertEquals("DNI", r.getString(2));
            assertEquals("10000002", r.getString(3));
            assertEquals("ACTIVO", r.getString(4));
            assertEquals("$2a$04$hashActivo", r.getString(5));
            assertEquals(1, r.getInt(6));
            assertTrue(r.next());
            assertEquals(U_BLOQUEADO, r.getString(1));
            assertEquals("BLOQUEADO", r.getString(4));
            assertEquals("$2a$04$hashBloqueado", r.getString(5));
            assertEquals(3, r.getInt(6));
            assertFalse(r.next());

            r = s.executeQuery("SELECT count(*) FROM information_schema.columns WHERE table_schema = 'nayra' AND table_name = 'usuarios' "
                    + "AND column_name IN ('pin_hash', 'intentos_fallidos', 'dni')");
            r.next();
            assertEquals(0, r.getInt(1), "Las columnas se eliminan después de copiar y verificar");

            r = s.executeQuery("SELECT tipo_documento_identidad, numero_documento FROM nayra.registro_identidad_simulado ORDER BY 2");
            r.next();
            assertEquals("DNI", r.getString(1));
            assertEquals("10000002", r.getString(2));

            // codigo_qr PROVISIONAL: se completa, es único y no contiene el documento.
            r = s.executeQuery("SELECT codigo_qr FROM nayra.cuentas");
            Set<String> qr = new HashSet<>();
            while (r.next()) {
                assertNotNull(r.getString(1));
                assertFalse(r.getString(1).contains("1000000"));
                qr.add(r.getString(1));
            }
            assertEquals(2, qr.size());
        }
    }

    @Test
    void unDispositivoIosDetieneLaMigracionSinTocarlo() throws Exception {
        baseEnV004("IOS");
        assertThrows(FlywayException.class, () -> BaseAuxiliar.flywayNayra(BASE, MARCADORES, null).migrate());
        try (Connection c = BaseAuxiliar.conectar(BASE); Statement s = c.createStatement()) {
            ResultSet r = s.executeQuery("SELECT max(version::int) FROM nayra.flyway_schema_history WHERE success AND type = 'SQL'");
            r.next();
            assertEquals(7, r.getInt(1), "V008 (solo ANDROID) no se aplica mientras exista un dispositivo IOS");
            r = s.executeQuery("SELECT plataforma FROM nayra.dispositivos");
            r.next();
            assertEquals("IOS", r.getString(1));
        }
    }
}
