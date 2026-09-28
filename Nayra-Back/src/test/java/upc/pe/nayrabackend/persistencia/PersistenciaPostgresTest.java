package upc.pe.nayrabackend.persistencia;

import upc.pe.nayrabackend.entities.TipoDocumentoIdentidad;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import upc.pe.nayrabackend.config.EntornoSimuladoInicial;
import upc.pe.nayrabackend.entities.Auditoria;
import upc.pe.nayrabackend.entities.Cuentas;
import upc.pe.nayrabackend.entities.Dispositivos;
import upc.pe.nayrabackend.entities.Identificadores;
import upc.pe.nayrabackend.entities.RegistroIdentidadSimulado;
import upc.pe.nayrabackend.entities.Rol;
import upc.pe.nayrabackend.entities.Usuario;
import upc.pe.nayrabackend.repositories.IAuditoriaRepository;
import upc.pe.nayrabackend.repositories.ICuentasRepository;
import upc.pe.nayrabackend.repositories.IDispositivosRepository;
import upc.pe.nayrabackend.repositories.IEntidadesBancariasRepository;
import upc.pe.nayrabackend.repositories.IRegistroIdentidadRepository;
import upc.pe.nayrabackend.repositories.IUsuariosRepository;
import upc.pe.nayrabackend.serviceinterfaces.IAuditoriaService;
import upc.pe.nayrabackend.serviceinterfaces.ICuentaService;
import upc.pe.nayrabackend.serviceinterfaces.IDispositivoService;
import upc.pe.nayrabackend.soporte.BaseDeDatosDePrueba;
import upc.pe.nayrabackend.soporte.Soporte;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Restricciones del esquema nayra (V002–V011, D-051, D-009 y modelo de datos v4) comprobadas contra PostgreSQL real, a través de los
 * adaptadores JPA y, cuando la aplicación no permite construir el dato inválido, con SQL directo.
 */
@SpringBootTest
@BaseDeDatosDePrueba
@ActiveProfiles("pruebasbd")
class PersistenciaPostgresTest {

    /** Datos ficticios de entorno-simulado/datos-ficticios.json. */
    static final String ENTIDAD = "4f1c2b7e-8d3a-4c6f-9b21-5e7a0c3d1f01";
    static final String TITULAR_10000001 = "0b6f2a4c-1d3e-4f5a-8b7c-9d0e1f2a3b01";
    static final String TITULAR_10000002 = "0b6f2a4c-1d3e-4f5a-8b7c-9d0e1f2a3b02";

    private static final AtomicInteger SECUENCIA = new AtomicInteger(20000000);

    @Autowired JdbcTemplate jdbc;
    @Autowired IUsuariosRepository usuarios;
    @Autowired IDispositivosRepository dispositivos;
    @Autowired ICuentasRepository cuentas;
    @Autowired IEntidadesBancariasRepository entidades;
    @Autowired IRegistroIdentidadRepository identidades;
    @Autowired IAuditoriaRepository auditoriaRepo;
    @Autowired IAuditoriaService auditoria;
    @Autowired ICuentaService cuentaService;
    @Autowired IDispositivoService dispositivoService;
    @Autowired Clock reloj;

    private Instant ahora;

    @BeforeEach
    void preparar() {
        // PostgreSQL guarda microsegundos.
        ahora = Instant.now().truncatedTo(ChronoUnit.MICROS);
    }

    private static String dniNuevo() {
        return String.valueOf(SECUENCIA.incrementAndGet());
    }

    private Usuario usuarioNuevo() {
        Usuario u = new Usuario(Identificadores.nuevo(), TipoDocumentoIdentidad.DNI, dniNuevo(), "Nombre", "Apellido", Soporte.celularNuevo(),
                Rol.USER, ahora);
        usuarios.guardar(u);
        return u;
    }

    private RegistroIdentidadSimulado titularNuevo() {
        RegistroIdentidadSimulado r = new RegistroIdentidadSimulado(Identificadores.nuevo(), TipoDocumentoIdentidad.DNI, dniNuevo(), "Titular", "Simulado");
        identidades.guardar(r);
        return r;
    }

    private Cuentas cuentaNueva(String titularId, String codigo, String saldo) {
        return new Cuentas(Identificadores.nuevo(), titularId, ENTIDAD, codigo, new BigDecimal(saldo), "PEN", ahora);
    }

    // ---- Esquema y datos ficticios ----

    @Test
    void migracionesAplicadasEnElEsquemaNayra() {
        assertEquals(12, jdbc.queryForObject("SELECT count(*) FROM nayra.flyway_schema_history WHERE success AND type = 'SQL'", Integer.class));
        List<String> tablas = jdbc.queryForList(
                "SELECT table_name FROM information_schema.tables WHERE table_schema = 'nayra' AND table_name <> 'flyway_schema_history'"
                        + " ORDER BY table_name", String.class);
        assertEquals(List.of("auditoria", "credenciales", "cuentas", "dispositivos", "entidades_bancarias", "notificaciones",
                "operaciones", "registro_identidad_simulado", "sesiones", "usuarios"), tablas);
        assertEquals(0, jdbc.queryForObject("SELECT count(*) FROM information_schema.tables WHERE table_schema = 'public'",
                Integer.class));
        // D-009, opción A: no existe tabla de roles. solicitudes_atencion está fuera del alcance (v4 §2.9).
        assertEquals(0, jdbc.queryForObject("SELECT count(*) FROM information_schema.tables WHERE table_name IN "
                + "('roles', 'solicitudes_atencion')", Integer.class));
        // pin_hash e intentos_fallidos salieron de usuarios (v4 §2.2).
        assertEquals(0, jdbc.queryForObject("SELECT count(*) FROM information_schema.columns WHERE table_schema = 'nayra' "
                + "AND table_name = 'usuarios' AND column_name IN ('pin_hash', 'intentos_fallidos', 'dni')", Integer.class));
    }

    @Test
    void ningunaTablaGuardaAudio() {
        // El único binario del esquema nayra es la clave pública del dispositivo; el audio nunca se guarda (D-013).
        assertEquals(List.of("dispositivos.clave_publica"), jdbc.queryForList("SELECT table_name || '.' || column_name FROM "
                + "information_schema.columns WHERE table_schema = 'nayra' AND data_type = 'bytea' ORDER BY 1", String.class));
        assertEquals(0, jdbc.queryForObject("SELECT count(*) FROM information_schema.columns WHERE table_schema = 'nayra' "
                + "AND column_name ILIKE '%audio%'", Integer.class));
    }

    @Test
    void datosFicticiosCargadosSinDuplicarAlReiniciar() throws Exception {
        assertTrue(entidades.porId(ENTIDAD).isPresent());
        assertEquals(TITULAR_10000002, identidades.porDocumento(TipoDocumentoIdentidad.DNI, "10000002").orElseThrow().id());
        Cuentas c = cuentaService.localizarPorTitular(TipoDocumentoIdentidad.DNI, "10000002").orElseThrow();
        assertEquals(TITULAR_10000002, c.getTitularId());
        assertEquals(ENTIDAD, c.getEntidadBancariaId());
        assertEquals(0, new BigDecimal("500.00").compareTo(c.getSaldo()));
        assertEquals(Cuentas.Estado.ACTIVA, c.getEstado());
        assertTrue(cuentaService.localizarPorTitular(TipoDocumentoIdentidad.DNI, "10000001").isEmpty());

        int registros = jdbc.queryForObject("SELECT count(*) FROM nayra.registro_identidad_simulado", Integer.class);
        int filasCuentas = jdbc.queryForObject("SELECT count(*) FROM nayra.cuentas", Integer.class);
        new EntornoSimuladoInicial(entidades, identidades, cuentas, reloj);
        assertEquals(registros, jdbc.queryForObject("SELECT count(*) FROM nayra.registro_identidad_simulado", Integer.class));
        assertEquals(filasCuentas, jdbc.queryForObject("SELECT count(*) FROM nayra.cuentas", Integer.class));
        assertEquals(1, jdbc.queryForObject("SELECT count(*) FROM nayra.entidades_bancarias", Integer.class));
    }

    // ---- usuarios ----

    @Test
    void usuarioSeGuardaYSeLeeIgual() {
        Usuario u = usuarioNuevo();
        u.bloquear(ahora);
        usuarios.guardar(u);
        Usuario leido = usuarios.porId(u.getId()).orElseThrow();
        assertEquals(TipoDocumentoIdentidad.DNI, leido.getTipoDocumentoIdentidad());
        assertEquals(u.getNumeroDocumento(), leido.getNumeroDocumento());
        assertEquals(Rol.USER, leido.getRol());
        assertEquals(Usuario.Estado.BLOQUEADO, leido.getEstado());
        assertEquals("BLOQUEADO", jdbc.queryForObject("SELECT estado FROM nayra.usuarios WHERE id = ?::uuid", String.class, u.getId()));
        assertEquals(ahora, leido.getFechaCreacion());
        assertEquals("USER", jdbc.queryForObject("SELECT rol FROM nayra.usuarios WHERE id = ?::uuid", String.class, u.getId()));
        assertTrue(usuarios.porId("no-es-uuid").isEmpty());
    }

    @Test
    void celularUnicoYEnFormatoCanonico() {
        Usuario u = usuarioNuevo();
        assertEquals(u.getId(), usuarios.porCelular(u.getCelular()).orElseThrow().getId());
        assertTrue(usuarios.porCelular("+51" + u.getCelular()).isEmpty(), "La búsqueda usa solo el formato canónico");
        Usuario mismoCelular = new Usuario(Identificadores.nuevo(), TipoDocumentoIdentidad.DNI, dniNuevo(), "Otro", "Otro",
                u.getCelular(), Rol.USER, ahora);
        assertThrows(DataIntegrityViolationException.class, () -> usuarios.guardar(mismoCelular));
        // V012: CHECK del formato canónico (Perú, 9 dígitos que empiezan por 9, sin +51).
        for (String fueraDeFormato : List.of("+51" + Soporte.celularNuevo(), "812345678", "98765432")) {
            Usuario v = new Usuario(Identificadores.nuevo(), TipoDocumentoIdentidad.DNI, dniNuevo(), "Otro", "Otro",
                    fueraDeFormato, Rol.USER, ahora);
            assertThrows(DataIntegrityViolationException.class, () -> usuarios.guardar(v), fueraDeFormato);
        }
    }

    @Test
    void documentoUnicoPorTipoYNumero() {
        Usuario u = usuarioNuevo();
        Usuario otro = new Usuario(Identificadores.nuevo(), TipoDocumentoIdentidad.DNI, u.getNumeroDocumento(), "Otro", "Otro",
                Soporte.celularNuevo(), Rol.USER, ahora);
        assertThrows(DataIntegrityViolationException.class, () -> usuarios.guardar(otro));
        // El mismo número con otro tipo es otro documento (UNIQUE compuesto, v4 §2.1).
        Usuario ce = new Usuario(Identificadores.nuevo(), TipoDocumentoIdentidad.CE, u.getNumeroDocumento(), "Otro", "Otro",
                Soporte.celularNuevo(), Rol.USER, ahora);
        usuarios.guardar(ce);
        assertEquals(ce.getId(), usuarios.porDocumento(TipoDocumentoIdentidad.CE, u.getNumeroDocumento()).orElseThrow().getId());
        RegistroIdentidadSimulado r = titularNuevo();
        assertThrows(DataIntegrityViolationException.class, () -> identidades.guardar(new RegistroIdentidadSimulado(
                Identificadores.nuevo(), TipoDocumentoIdentidad.DNI, r.numeroDocumento(), "X", "Y")));
        identidades.guardar(new RegistroIdentidadSimulado(Identificadores.nuevo(), TipoDocumentoIdentidad.CE, r.numeroDocumento(), "X", "Y"));
    }

    @Test
    void estadoRolYTipoDeDocumentoValidos() {
        Usuario u = usuarioNuevo();
        for (String estado : List.of("ACTIVO", "BLOQUEADO", "INACTIVO")) {
            assertEquals(1, jdbc.update("UPDATE nayra.usuarios SET estado = ? WHERE id = ?::uuid", estado, u.getId()));
        }
        for (String anterior : List.of("ACTIVA", "BLOQUEADA", "ELIMINADO")) {
            assertThrows(DataIntegrityViolationException.class,
                    () -> jdbc.update("UPDATE nayra.usuarios SET estado = ? WHERE id = ?::uuid", anterior, u.getId()));
        }
        assertThrows(DataIntegrityViolationException.class,
                () -> jdbc.update("UPDATE nayra.usuarios SET rol = 'SOPORTE' WHERE id = ?::uuid", u.getId()));
        assertThrows(DataIntegrityViolationException.class,
                () -> jdbc.update("UPDATE nayra.usuarios SET tipo_documento_identidad = 'PASAPORTE' WHERE id = ?::uuid", u.getId()));
        assertThrows(DataIntegrityViolationException.class, () -> jdbc.update(
                "UPDATE nayra.registro_identidad_simulado SET tipo_documento_identidad = 'RUC' WHERE id = ?::uuid", TITULAR_10000002));
    }

    // ---- cuentas ----

    @Test
    void titularUnico() {
        assertThrows(DataIntegrityViolationException.class,
                () -> cuentas.guardar(cuentaNueva(TITULAR_10000002, "SIM-X-" + dniNuevo(), "10.00")));
    }

    @Test
    void propietarioUnicoYVariasCuentasSinPropietario() {
        // Varias cuentas sin vincular (propietario NULL) conviven: las del entorno simulado ya lo demuestran.
        assertTrue(jdbc.queryForObject("SELECT count(*) FROM nayra.cuentas WHERE propietario_id IS NULL", Integer.class) >= 2);
        Usuario u = usuarioNuevo();
        Cuentas primera = cuentaNueva(titularNuevo().id(), "SIM-A-" + dniNuevo(), "1.00");
        cuentas.guardar(primera);
        cuentaService.vincular(primera, u.getId());
        assertEquals(primera.getId(), cuentas.porPropietario(u.getId()).orElseThrow().getId());
        Cuentas segunda = cuentaNueva(titularNuevo().id(), "SIM-B-" + dniNuevo(), "1.00");
        segunda.vincular(u.getId(), ahora);
        assertThrows(DataIntegrityViolationException.class, () -> cuentas.guardar(segunda));
    }

    @Test
    void codigoQrFijoUnicoYSinDatosPersonales() {
        // Las cuentas del entorno simulado ya tienen un codigo_qr PROVISIONAL (P-3): UUID aleatorio, no el documento.
        List<String> qr = jdbc.queryForList("SELECT codigo_qr FROM nayra.cuentas", String.class);
        assertFalse(qr.isEmpty());
        assertTrue(qr.stream().allMatch(c -> Identificadores.leer(c).isPresent()));
        assertTrue(qr.stream().noneMatch(c -> c.contains("10000002")));
        Cuentas c = cuentaService.localizarPorTitular(TipoDocumentoIdentidad.DNI, "10000002").orElseThrow();
        assertEquals(c.getCodigoQr(), cuentaService.localizarPorTitular(TipoDocumentoIdentidad.DNI, "10000002").orElseThrow().getCodigoQr(),
                "El código se guarda: no se genera en cada consulta");
        Cuentas otra = cuentaNueva(titularNuevo().id(), "SIM-Q-" + dniNuevo(), "1.00");
        cuentas.guardar(otra);
        assertThrows(DataIntegrityViolationException.class, () -> jdbc.update(
                "UPDATE nayra.cuentas SET codigo_qr = ? WHERE id = ?::uuid", c.getCodigoQr(), otra.getId()));
        assertThrows(DataIntegrityViolationException.class, () -> jdbc.update(
                "UPDATE nayra.cuentas SET codigo_qr = NULL WHERE id = ?::uuid", otra.getId()));
    }

    @Test
    void monedaSoloPen() {
        assertThrows(DataIntegrityViolationException.class, () -> cuentas.guardar(new Cuentas(Identificadores.nuevo(),
                titularNuevo().id(), ENTIDAD, "SIM-U-" + dniNuevo(), BigDecimal.ONE, "USD", ahora)));
    }

    @Test
    void codigoDeCuentaUnico() {
        assertThrows(DataIntegrityViolationException.class,
                () -> cuentas.guardar(cuentaNueva(titularNuevo().id(), "SIM-0000000002", "1.00")));
    }

    @Test
    void saldoNoNegativoYEstadoValido() {
        assertThrows(DataIntegrityViolationException.class,
                () -> cuentas.guardar(cuentaNueva(titularNuevo().id(), "SIM-N-" + dniNuevo(), "-0.01")));
        Cuentas c = cuentaNueva(titularNuevo().id(), "SIM-C-" + dniNuevo(), "0.00");
        cuentas.guardar(c);
        assertThrows(DataIntegrityViolationException.class,
                () -> jdbc.update("UPDATE nayra.cuentas SET estado = 'SUSPENDIDA' WHERE id = ?::uuid", c.getId()));
    }

    @Test
    void fkDeCuentas() {
        Cuentas sinEntidad = new Cuentas(Identificadores.nuevo(), titularNuevo().id(), Identificadores.nuevo(), "SIM-E-" + dniNuevo(),
                BigDecimal.ONE, "PEN", ahora);
        assertThrows(DataIntegrityViolationException.class, () -> cuentas.guardar(sinEntidad));
        Cuentas sinTitular = cuentaNueva(Identificadores.nuevo(), "SIM-T-" + dniNuevo(), "1.00");
        assertThrows(DataIntegrityViolationException.class, () -> cuentas.guardar(sinTitular));
        Cuentas c = cuentaNueva(titularNuevo().id(), "SIM-P-" + dniNuevo(), "1.00");
        c.vincular(Identificadores.nuevo(), ahora);
        assertThrows(DataIntegrityViolationException.class, () -> cuentas.guardar(c));
    }

    // ---- dispositivos ----

    @Test
    void unSoloDispositivoActivoPorUsuario() throws Exception {
        Usuario u = usuarioNuevo();
        Dispositivos primero = dispositivoService.vincular(u.getId(), Soporte.claveP256().getPublic());
        // Insertar un segundo ACTIVO sin revocar el primero viola el índice único parcial.
        Dispositivos duplicado = new Dispositivos(Identificadores.nuevo(), u.getId(), Soporte.claveP256().getPublic(), "ANDROID", ahora);
        assertThrows(DataIntegrityViolationException.class, () -> dispositivos.guardar(duplicado));
        // El servicio revoca el anterior antes de insertar el nuevo (D-039).
        Dispositivos segundo = dispositivoService.vincular(u.getId(), Soporte.claveP256().getPublic());
        assertEquals(segundo.getId(), dispositivos.activoDeUsuario(u.getId()).orElseThrow().getId());
        Dispositivos revocado = dispositivos.porId(primero.getId()).orElseThrow();
        assertEquals(Dispositivos.Estado.REVOCADO, revocado.getEstado());
        assertNotNull(revocado.getFechaRevocacion());
        assertEquals(1, jdbc.queryForObject("SELECT count(*) FROM nayra.dispositivos WHERE usuario_id = ?::uuid AND estado = 'ACTIVO'",
                Integer.class, u.getId()));
    }

    @Test
    void clavePublicaSeConservaYSeValidanLosValores() throws Exception {
        Usuario u = usuarioNuevo();
        var par = Soporte.claveP256();
        Dispositivos d = dispositivoService.vincular(u.getId(), par.getPublic());
        Dispositivos leido = dispositivos.porId(d.getId()).orElseThrow();
        assertArrayEquals(par.getPublic().getEncoded(), leido.getClavePublica().getEncoded());
        assertEquals(Dispositivos.ALGORITMO, leido.getAlgoritmoClave());
        assertThrows(DataIntegrityViolationException.class,
                () -> jdbc.update("UPDATE nayra.dispositivos SET estado = 'PERDIDO' WHERE id = ?::uuid", d.getId()));
        assertThrows(DataIntegrityViolationException.class,
                () -> jdbc.update("UPDATE nayra.dispositivos SET plataforma = 'WEB' WHERE id = ?::uuid", d.getId()));
        // Solo Android en el primer entregable (v4, restricción de plataforma).
        assertThrows(DataIntegrityViolationException.class,
                () -> jdbc.update("UPDATE nayra.dispositivos SET plataforma = 'IOS' WHERE id = ?::uuid", d.getId()));
        assertThrows(DataIntegrityViolationException.class,
                () -> jdbc.update("UPDATE nayra.dispositivos SET algoritmo_clave = 'RSA' WHERE id = ?::uuid", d.getId()));
        Dispositivos sinUsuario = new Dispositivos(Identificadores.nuevo(), Identificadores.nuevo(), par.getPublic(), "ANDROID", ahora);
        assertThrows(DataIntegrityViolationException.class, () -> dispositivos.guardar(sinUsuario));
    }

    @Test
    void borrarUnUsuarioReferenciadoSeImpide() throws Exception {
        Usuario u = usuarioNuevo();
        dispositivoService.vincular(u.getId(), Soporte.claveP256().getPublic());
        assertThrows(DataIntegrityViolationException.class,
                () -> jdbc.update("DELETE FROM nayra.usuarios WHERE id = ?::uuid", u.getId()));
    }

    // ---- auditoria ----

    @Test
    void auditoriaConExitosoYFallido() throws Exception {
        Usuario u = usuarioNuevo();
        Dispositivos d = dispositivoService.vincular(u.getId(), Soporte.claveP256().getPublic());
        auditoria.registrar("PRUEBA_PERSISTENCIA", Auditoria.Resultado.EXITOSO, u.getId(), u.getId(), null, d.getId());
        auditoria.registrar("PRUEBA_PERSISTENCIA", Auditoria.Resultado.FALLIDO, null, u.getId(), "NO_COINCIDE", null);
        assertEquals(List.of("EXITOSO", "FALLIDO"), jdbc.queryForList(
                "SELECT resultado FROM nayra.auditoria WHERE accion = 'PRUEBA_PERSISTENCIA' ORDER BY resultado", String.class));
        List<Auditoria> fallidos = auditoriaRepo.consultar(u.getId(), "PRUEBA_", Auditoria.Resultado.FALLIDO);
        assertEquals(1, fallidos.size());
        assertEquals("NO_COINCIDE", fallidos.get(0).motivo());
        assertTrue(auditoriaRepo.consultar("no-es-uuid", null, null).isEmpty());
        assertThrows(DataIntegrityViolationException.class, () -> jdbc.update(
                "INSERT INTO nayra.auditoria (id, fecha, accion, resultado) VALUES (gen_random_uuid(), now(), 'X', 'EXITO')"));
    }

    @Test
    void fkDeAuditoria() {
        String inexistente = Identificadores.nuevo();
        assertThrows(DataIntegrityViolationException.class, () -> auditoriaRepo.guardar(new Auditoria(Identificadores.nuevo(), ahora,
                inexistente, null, "PRUEBA_FK", Auditoria.Resultado.FALLIDO, null, null)));
        assertThrows(DataIntegrityViolationException.class, () -> auditoriaRepo.guardar(new Auditoria(Identificadores.nuevo(), ahora,
                null, inexistente, "PRUEBA_FK", Auditoria.Resultado.FALLIDO, null, null)));
        assertThrows(DataIntegrityViolationException.class, () -> auditoriaRepo.guardar(new Auditoria(Identificadores.nuevo(), ahora,
                null, null, "PRUEBA_FK", Auditoria.Resultado.FALLIDO, null, inexistente)));
    }

    @Test
    void usuarioDeEjecucionNoPuedeModificarLaAuditoria() {
        Boolean existe = jdbc.queryForObject("SELECT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'nayra_app')", Boolean.class);
        org.junit.jupiter.api.Assumptions.assumeTrue(Boolean.TRUE.equals(existe), "El rol de ejecución nayra_app no existe en esta base");
        String privilegio = "SELECT has_table_privilege('nayra_app', 'nayra.auditoria', ?)";
        assertTrue(jdbc.queryForObject(privilegio, Boolean.class, "INSERT"));
        assertTrue(jdbc.queryForObject(privilegio, Boolean.class, "SELECT"));
        assertFalse(jdbc.queryForObject(privilegio, Boolean.class, "UPDATE"));
        assertFalse(jdbc.queryForObject(privilegio, Boolean.class, "DELETE"));
        assertTrue(jdbc.queryForObject("SELECT has_table_privilege('nayra_app', 'nayra.usuarios', 'UPDATE')", Boolean.class));
    }
}
