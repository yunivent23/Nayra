package upc.pe.nayrabackend.persistencia;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import upc.pe.nayrabackend.soporte.BaseAuxiliar;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * P-4 (PENDIENTE NO BLOQUEANTE): comprobación experimental de un PostgreSQL compartido con el esquema nayra, el
 * esquema biometria con historial propio y un usuario de base de datos restringido por servicio (v4 §4.2).
 *
 * Cada operación se ejecuta con SET ROLE al usuario del servicio, que solo tiene los permisos concedidos por las
 * migraciones V011 (nayra) y V003 (biometria). Los roles se crean aquí porque crearlos corresponde al despliegue
 * (D-016, D-017); son NOLOGIN y quedan en el servidor de pruebas.
 */
@EnabledIfEnvironmentVariable(named = "NAYRA_TEST_DB_URL", matches = ".+",
        disabledReason = "Requiere PostgreSQL: definir NAYRA_TEST_DB_URL (base exclusiva de pruebas)")
class PermisosPorServicioTest {

    static final String BASE = "nayra_test_permisos";
    static final String NEGOCIO = "nayra_negocio";
    static final String AUTENTICACION = "nayra_autenticacion";
    static final String BIOMETRIA = "nayra_biometria";
    static final String ENTIDAD = UUID.randomUUID().toString();
    static final String TITULAR = UUID.randomUUID().toString();

    @BeforeAll
    static void preparar() throws Exception {
        BaseAuxiliar.recrear(BASE);
        try (Connection c = BaseAuxiliar.conectar(BASE); Statement s = c.createStatement()) {
            for (String rol : new String[]{NEGOCIO, AUTENTICACION, BIOMETRIA, "nayra_app"}) {
                s.execute("DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = '" + rol + "') THEN "
                        + "CREATE ROLE " + rol + " NOLOGIN; END IF; END $$");
            }
        }
        BaseAuxiliar.flywayNayra(BASE, Map.of("rol_ejecucion", "nayra_app", "rol_negocio", NEGOCIO,
                "rol_autenticacion", AUTENTICACION), null).migrate();
        BaseAuxiliar.flywayBiometria(BASE, Map.of("rol_biometria", BIOMETRIA)).migrate();
        // Datos de referencia creados por el usuario de migración.
        try (Connection c = BaseAuxiliar.conectar(BASE); Statement s = c.createStatement()) {
            s.execute("INSERT INTO nayra.entidades_bancarias VALUES ('" + ENTIDAD + "', 'Entidad simulada')");
            s.execute("INSERT INTO nayra.registro_identidad_simulado (id, tipo_documento_identidad, numero_documento, nombres, apellidos) "
                    + "VALUES ('" + TITULAR + "', 'DNI', '30000001', 'Titular', 'Simulado')");
        }
    }

    @AfterAll
    static void limpiar() throws Exception {
        try (Connection c = java.sql.DriverManager.getConnection(System.getenv("NAYRA_TEST_DB_URL"), BaseAuxiliar.usuario(),
                BaseAuxiliar.contrasena()); Statement s = c.createStatement()) {
            s.execute("DROP DATABASE IF EXISTS " + BASE + " WITH (FORCE)");
        }
    }

    /** Ejecuta el SQL como el rol del servicio y devuelve el SQLSTATE si falla (null si funciona). */
    private static String como(String rol, String... sentencias) throws SQLException {
        try (Connection c = BaseAuxiliar.conectar(BASE); Statement s = c.createStatement()) {
            s.execute("SET ROLE " + rol);
            try {
                for (String sql : sentencias) {
                    s.execute(sql);
                }
                return null;
            } catch (SQLException e) {
                return e.getSQLState();
            }
        }
    }

    private static final String SIN_PERMISO = "42501";
    private static final String FK_VIOLADA = "23503";

    private static String usuarioDeNegocio() throws SQLException {
        String id = UUID.randomUUID().toString();
        assertNull(como(NEGOCIO, "INSERT INTO nayra.usuarios (id, tipo_documento_identidad, numero_documento, nombres, apellidos, "
                + "numero_celular, rol, estado, fecha_creacion, fecha_actualizacion) VALUES ('" + id + "', 'DNI', '"
                + (40000000 + Math.abs(id.hashCode() % 9999999)) + "', 'N', 'A', '" + upc.pe.nayrabackend.soporte.Soporte.celularNuevo() + "', 'USER', 'ACTIVO', now(), now())"));
        return id;
    }

    @Test
    void historialesSeparadosParaNayraYBiometria() throws Exception {
        try (Connection c = BaseAuxiliar.conectar(BASE); Statement s = c.createStatement()) {
            var r = s.executeQuery("SELECT (SELECT count(*) FROM nayra.flyway_schema_history WHERE type = 'SQL'), "
                    + "(SELECT count(*) FROM biometria.flyway_schema_history WHERE type = 'SQL')");
            r.next();
            assertEquals(12, r.getInt(1));
            assertEquals(3, r.getInt(2));
        }
    }

    @Test
    void negocioTrabajaConSusTablas() throws Exception {
        String u = usuarioDeNegocio();
        String c1 = UUID.randomUUID().toString();
        String c2 = UUID.randomUUID().toString();
        String titular2 = UUID.randomUUID().toString();
        assertNull(como(NEGOCIO,
                "INSERT INTO nayra.registro_identidad_simulado (id, tipo_documento_identidad, numero_documento, nombres, apellidos) "
                        + "VALUES ('" + titular2 + "', 'CE', '30000009', 'T', 'S')",
                "INSERT INTO nayra.cuentas (id, titular_id, propietario_id, entidad_bancaria_id, codigo_cuenta, codigo_qr, saldo, moneda, "
                        + "estado, fecha_creacion, fecha_actualizacion) VALUES ('" + c1 + "', '" + TITULAR + "', '" + u + "', '" + ENTIDAD
                        + "', 'SIM-P4-1', gen_random_uuid()::text, 100, 'PEN', 'ACTIVA', now(), now())",
                "INSERT INTO nayra.cuentas (id, titular_id, entidad_bancaria_id, codigo_cuenta, codigo_qr, saldo, moneda, estado, "
                        + "fecha_creacion, fecha_actualizacion) VALUES ('" + c2 + "', '" + titular2 + "', '" + ENTIDAD
                        + "', 'SIM-P4-2', gen_random_uuid()::text, 0, 'PEN', 'ACTIVA', now(), now())",
                "INSERT INTO nayra.operaciones (id, cuenta_origen_id, cuenta_destino_id, tipo, monto, codigo_referencia, estado, fecha, "
                        + "fecha_actualizacion) VALUES (gen_random_uuid(), '" + c1 + "', '" + c2 + "', 'TRANSFERENCIA', 10, '000001', "
                        + "'EXITOSO', now(), now())",
                "INSERT INTO nayra.notificaciones (id, destinatario_id, operacion_id, tipo, titulo, contenido, fecha_generacion) "
                        + "SELECT gen_random_uuid(), '" + u + "', id, 'OPERACION', 'T', 'C', now() FROM nayra.operaciones WHERE codigo_referencia = '000001'",
                "UPDATE nayra.usuarios SET estado = 'BLOQUEADO' WHERE id = '" + u + "'",
                "SELECT count(*) FROM nayra.usuarios",
                "INSERT INTO nayra.auditoria (id, fecha, accion, resultado) VALUES (gen_random_uuid(), now(), 'P4', 'EXITOSO')"));
        // Negocio no lee credenciales (pin_hash no sale de Autenticación), ni sesiones, ni biometría; no modifica la auditoría.
        assertEquals(SIN_PERMISO, como(NEGOCIO, "SELECT pin_hash FROM nayra.credenciales"));
        assertEquals(SIN_PERMISO, como(NEGOCIO, "SELECT * FROM nayra.sesiones"));
        assertEquals(SIN_PERMISO, como(NEGOCIO, "SELECT * FROM biometria.perfiles_voz"));
        assertEquals(SIN_PERMISO, como(NEGOCIO, "UPDATE nayra.auditoria SET motivo = 'X'"));
    }

    @Test
    void autenticacionTrabajaConSusTablasSinLeerUsuarios() throws Exception {
        String u = usuarioDeNegocio();
        String d = UUID.randomUUID().toString();
        String sesion = UUID.randomUUID().toString();
        // Las FK credenciales/dispositivos/sesiones → usuarios funcionan sin SELECT sobre usuarios: PostgreSQL las
        // comprueba con los privilegios del propietario de la tabla referenciada.
        assertNull(como(AUTENTICACION,
                "INSERT INTO nayra.credenciales (id, usuario_id, pin_hash, fecha_creacion, fecha_actualizacion) "
                        + "VALUES (gen_random_uuid(), '" + u + "', '$2a$04$hash', now(), now())",
                "INSERT INTO nayra.dispositivos (id, usuario_id, clave_publica, plataforma, fecha_vinculacion) "
                        + "VALUES ('" + d + "', '" + u + "', '\\x00', 'ANDROID', now())",
                "INSERT INTO nayra.sesiones (id, usuario_id, dispositivo_id, fecha_creacion, fecha_ultimo_acceso) "
                        + "VALUES ('" + sesion + "', '" + u + "', '" + d + "', now(), now())",
                "UPDATE nayra.credenciales SET intentos_fallidos = 1 WHERE usuario_id = '" + u + "'",
                "UPDATE nayra.sesiones SET fecha_revocacion = now() WHERE id = '" + sesion + "'",
                "UPDATE nayra.dispositivos SET estado = 'REVOCADO', fecha_revocacion = now() WHERE id = '" + d + "'",
                "INSERT INTO nayra.auditoria (id, fecha, accion, resultado) VALUES (gen_random_uuid(), now(), 'P4', 'EXITOSO')"));
        // La FK sigue protegiendo: un usuario inexistente se rechaza por la FK, no por falta de permiso.
        assertEquals(FK_VIOLADA, como(AUTENTICACION, "INSERT INTO nayra.credenciales (id, usuario_id, pin_hash, fecha_creacion, "
                + "fecha_actualizacion) VALUES (gen_random_uuid(), gen_random_uuid(), 'h', now(), now())"));
        assertEquals(FK_VIOLADA, como(AUTENTICACION, "INSERT INTO nayra.sesiones (id, usuario_id, dispositivo_id, fecha_creacion, "
                + "fecha_ultimo_acceso) VALUES (gen_random_uuid(), gen_random_uuid(), '" + d + "', now(), now())"));
        // Autenticación NO lee usuarios ni otras tablas de Negocio, ni la biometría.
        assertEquals(SIN_PERMISO, como(AUTENTICACION, "SELECT id, estado FROM nayra.usuarios"));
        assertEquals(SIN_PERMISO, como(AUTENTICACION, "UPDATE nayra.usuarios SET estado = 'BLOQUEADO'"));
        assertEquals(SIN_PERMISO, como(AUTENTICACION, "SELECT * FROM nayra.cuentas"));
        assertEquals(SIN_PERMISO, como(AUTENTICACION, "SELECT * FROM biometria.perfiles_voz"));
        // Ningún servicio borra: las FK son RESTRICT y v4 no define borrados en nayra.
        assertEquals(SIN_PERMISO, como(AUTENTICACION, "DELETE FROM nayra.sesiones"));
    }

    @Test
    void biometricoTrabajaConSuEsquemaYNoVeNayra() throws Exception {
        String perfil = UUID.randomUUID().toString();
        String usuario = UUID.randomUUID().toString();
        assertNull(como(BIOMETRIA,
                "INSERT INTO biometria.perfiles_voz (id, usuario_id, embedding_cifrado, iv, clave_version, modelo, version_modelo, "
                        + "numero_muestras, fecha_creacion, fecha_actualizacion) VALUES ('" + perfil + "', '" + usuario
                        + "', '\\x0102', '\\x000000000000000000000000', 1, 'm', 'v', 3, now(), now())",
                "SELECT embedding_cifrado FROM biometria.perfiles_voz WHERE usuario_id = '" + usuario + "'",
                "UPDATE biometria.perfiles_voz SET estado = 'REVOCADO', fecha_revocacion = now() WHERE id = '" + perfil + "'",
                "DELETE FROM biometria.perfiles_voz WHERE id = '" + perfil + "'"));
        assertEquals(SIN_PERMISO, como(BIOMETRIA, "SELECT * FROM nayra.usuarios"));
        assertEquals(SIN_PERMISO, como(BIOMETRIA, "SELECT * FROM nayra.credenciales"));
    }
}
