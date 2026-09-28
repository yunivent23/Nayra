package upc.pe.nayrabackend.persistencia;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import upc.pe.nayrabackend.entities.Credenciales;
import upc.pe.nayrabackend.entities.Cuentas;
import upc.pe.nayrabackend.entities.Dispositivos;
import upc.pe.nayrabackend.entities.Identificadores;
import upc.pe.nayrabackend.entities.Notificaciones;
import upc.pe.nayrabackend.entities.Operaciones;
import upc.pe.nayrabackend.entities.RegistroIdentidadSimulado;
import upc.pe.nayrabackend.entities.Rol;
import upc.pe.nayrabackend.entities.TipoDocumentoIdentidad;
import upc.pe.nayrabackend.entities.Usuario;
import upc.pe.nayrabackend.repositories.ICredencialesRepository;
import upc.pe.nayrabackend.repositories.ICuentasRepository;
import upc.pe.nayrabackend.repositories.IDispositivosRepository;
import upc.pe.nayrabackend.repositories.INotificacionesRepository;
import upc.pe.nayrabackend.repositories.IOperacionesRepository;
import upc.pe.nayrabackend.repositories.IRegistroIdentidadRepository;
import upc.pe.nayrabackend.repositories.ISesionesRepository;
import upc.pe.nayrabackend.repositories.IUsuariosRepository;
import upc.pe.nayrabackend.securities.TokenSesionJwt;
import upc.pe.nayrabackend.serviceinterfaces.ICredencialesService;
import upc.pe.nayrabackend.serviceinterfaces.IDispositivoService;
import upc.pe.nayrabackend.serviceinterfaces.IPinService;
import upc.pe.nayrabackend.serviceinterfaces.ISesionesService;
import upc.pe.nayrabackend.serviceinterfaces.IUsuarioService;
import upc.pe.nayrabackend.soporte.BaseDeDatosDePrueba;
import upc.pe.nayrabackend.soporte.Soporte;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tablas nuevas del modelo de datos v4 contra PostgreSQL real: credenciales (§2.2), sesiones (§2.7), operaciones
 * (§2.8) y notificaciones (§2.11). Cuando la aplicación no permite construir el dato inválido, se usa SQL directo.
 */
@SpringBootTest
@BaseDeDatosDePrueba
@ActiveProfiles("pruebasbd")
class ModeloDatosV4PostgresTest {

    static final String ENTIDAD = "4f1c2b7e-8d3a-4c6f-9b21-5e7a0c3d1f01";
    private static final AtomicInteger SECUENCIA = new AtomicInteger(50000000);

    @Autowired JdbcTemplate jdbc;
    @Autowired IUsuariosRepository usuarios;
    @Autowired IDispositivosRepository dispositivos;
    @Autowired ICuentasRepository cuentas;
    @Autowired IRegistroIdentidadRepository identidades;
    @Autowired ICredencialesRepository credencialesRepo;
    @Autowired ISesionesRepository sesionesRepo;
    @Autowired IOperacionesRepository operaciones;
    @Autowired INotificacionesRepository notificaciones;
    @Autowired ICredencialesService credenciales;
    @Autowired ISesionesService sesiones;
    @Autowired IUsuarioService usuarioService;
    @Autowired IDispositivoService dispositivoService;
    @Autowired IPinService pines;
    @Autowired TokenSesionJwt jwt;

    private Instant ahora;

    @BeforeEach
    void preparar() {
        ahora = Instant.now().truncatedTo(ChronoUnit.MICROS);
    }

    private static String documento() {
        return String.valueOf(SECUENCIA.incrementAndGet());
    }

    private Usuario usuario() {
        Usuario u = new Usuario(Identificadores.nuevo(), TipoDocumentoIdentidad.DNI, documento(), "Juan", "Pérez Gómez",
                Soporte.celularNuevo(), Rol.USER, ahora);
        usuarios.guardar(u);
        return u;
    }

    private Usuario usuarioConCredencial() {
        Usuario u = usuario();
        credenciales.crear(u.getId(), pines.hashear(Soporte.PIN));
        return u;
    }

    private Cuentas cuenta(Usuario propietario) {
        RegistroIdentidadSimulado titular = new RegistroIdentidadSimulado(Identificadores.nuevo(), TipoDocumentoIdentidad.DNI,
                documento(), "Titular", "Simulado");
        identidades.guardar(titular);
        Cuentas c = new Cuentas(Identificadores.nuevo(), titular.id(), ENTIDAD, "SIM-V4-" + documento(), new BigDecimal("100.00"),
                "PEN", ahora);
        if (propietario != null) {
            c.vincular(propietario.getId(), ahora);
        }
        cuentas.guardar(c);
        return c;
    }

    private Operaciones operacion(Cuentas origen, Cuentas destino, String monto) {
        Operaciones o = new Operaciones(Identificadores.nuevo(), origen.getId(), destino.getId(), new BigDecimal(monto), null,
                Operaciones.nuevoCodigoReferencia(new SecureRandom()), Operaciones.Estado.EXITOSO, ahora);
        operaciones.guardar(o);
        return o;
    }

    private String insertarOperacionSql(String origen, String destino, String monto, String moneda, String codigo, String estado,
                                        String canal, String tipo) {
        String id = Identificadores.nuevo();
        jdbc.update("INSERT INTO nayra.operaciones (id, cuenta_origen_id, cuenta_destino_id, tipo, monto, moneda, codigo_referencia, "
                        + "estado, canal, fecha, fecha_actualizacion) VALUES (?::uuid, ?::uuid, ?::uuid, ?, ?::numeric, ?, ?, ?, ?, now(), now())",
                id, origen, destino, tipo, monto, moneda, codigo, estado, canal);
        return id;
    }

    // ---- credenciales ----

    @Test
    void unaCredencialPorUsuarioConFkYSinPinEnClaro() {
        Usuario u = usuarioConCredencial();
        Credenciales c = credencialesRepo.deUsuario(u.getId()).orElseThrow();
        assertNotEquals(Soporte.PIN, c.getPinHash());
        assertFalse(c.getPinHash().contains(Soporte.PIN));
        assertEquals(0, c.getIntentosFallidos());
        assertThrows(DataIntegrityViolationException.class,
                () -> credencialesRepo.guardar(new Credenciales(Identificadores.nuevo(), u.getId(), "h", ahora)));
        assertThrows(DataIntegrityViolationException.class,
                () -> credencialesRepo.guardar(new Credenciales(Identificadores.nuevo(), Identificadores.nuevo(), "h", ahora)));
        assertThrows(DataIntegrityViolationException.class, () -> jdbc.update(
                "UPDATE nayra.credenciales SET intentos_fallidos = 4 WHERE usuario_id = ?::uuid", u.getId()));
        assertThrows(DataIntegrityViolationException.class, () -> jdbc.update(
                "UPDATE nayra.credenciales SET intentos_fallidos = -1 WHERE usuario_id = ?::uuid", u.getId()));
        assertThrows(DataIntegrityViolationException.class, () -> jdbc.update(
                "UPDATE nayra.credenciales SET pin_hash = NULL WHERE usuario_id = ?::uuid", u.getId()));
    }

    @Test
    void pinIncorrectoCuentaYElTerceroAgotaPinCorrectoReinicia() {
        Usuario u = usuarioConCredencial();
        ICredencialesService.ResultadoPin r1 = credenciales.verificar(u.getId(), "000000");
        assertFalse(r1.correcto());
        assertEquals(2, r1.intentosRestantes());
        assertEquals(1, jdbc.queryForObject("SELECT intentos_fallidos FROM nayra.credenciales WHERE usuario_id = ?::uuid",
                Integer.class, u.getId()));
        assertTrue(credenciales.verificar(u.getId(), Soporte.PIN).correcto());
        assertEquals(0, jdbc.queryForObject("SELECT intentos_fallidos FROM nayra.credenciales WHERE usuario_id = ?::uuid",
                Integer.class, u.getId()));
        credenciales.verificar(u.getId(), "000000");
        credenciales.verificar(u.getId(), "111111");
        ICredencialesService.ResultadoPin r3 = credenciales.verificar(u.getId(), "222222");
        assertTrue(r3.agotados());
        assertEquals(0, r3.intentosRestantes());
        assertEquals(3, jdbc.queryForObject("SELECT intentos_fallidos FROM nayra.credenciales WHERE usuario_id = ?::uuid",
                Integer.class, u.getId()));
    }

    // ---- sesiones ----

    private Dispositivos dispositivo(Usuario u) throws Exception {
        return dispositivoService.vincular(u.getId(), Soporte.claveP256().getPublic());
    }

    @Test
    void sesionSeCreaSeValidaYRegistraElAcceso() throws Exception {
        Usuario u = usuarioConCredencial();
        Dispositivos d = dispositivo(u);
        String token = sesiones.crear(u.getId(), d.getId());
        String jti = jwt.jti(token).orElseThrow();
        assertEquals(1, jdbc.queryForObject("SELECT count(*) FROM nayra.sesiones WHERE id = ?::uuid AND usuario_id = ?::uuid "
                + "AND dispositivo_id = ?::uuid AND fecha_revocacion IS NULL", Integer.class, jti, u.getId(), d.getId()));
        jdbc.update("UPDATE nayra.sesiones SET fecha_creacion = fecha_creacion - interval '4 minutes', "
                + "fecha_ultimo_acceso = fecha_ultimo_acceso - interval '4 minutes' WHERE id = ?::uuid", jti);
        Instant antes = sesionesRepo.porId(jti).orElseThrow().getFechaUltimoAcceso();
        var activa = sesiones.validar(token).orElseThrow();
        assertEquals(u.getId(), activa.usuarioId());
        assertTrue(sesionesRepo.porId(jti).orElseThrow().getFechaUltimoAcceso().isAfter(antes), "La actividad se guarda");
        // Solo las columnas de v4: ni token, ni hash, ni expiración, ni motivo.
        assertEquals(List.of("dispositivo_id", "fecha_creacion", "fecha_revocacion", "fecha_ultimo_acceso", "id", "usuario_id"),
                jdbc.queryForList("SELECT column_name FROM information_schema.columns WHERE table_schema = 'nayra' "
                        + "AND table_name = 'sesiones' ORDER BY column_name", String.class));
    }

    @Test
    void laInactividadDeMasDeCincoMinutosRevocaLaSesion() throws Exception {
        Usuario u = usuarioConCredencial();
        String token = sesiones.crear(u.getId(), dispositivo(u).getId());
        String jti = jwt.jti(token).orElseThrow();
        jdbc.update("UPDATE nayra.sesiones SET fecha_creacion = fecha_creacion - interval '6 minutes', "
                + "fecha_ultimo_acceso = fecha_ultimo_acceso - interval '6 minutes' WHERE id = ?::uuid", jti);
        assertTrue(sesiones.validar(token).isEmpty());
        assertNotNull(jdbc.queryForObject("SELECT fecha_revocacion FROM nayra.sesiones WHERE id = ?::uuid", Object.class, jti));
        jdbc.update("UPDATE nayra.sesiones SET fecha_ultimo_acceso = now() WHERE id = ?::uuid", jti);
        assertTrue(sesiones.validar(token).isEmpty(), "Una sesión revocada no se reabre");
    }

    @Test
    void cerrarBloquearYRevocarElDispositivoRevocanLaSesion() throws Exception {
        Usuario u = usuarioConCredencial();
        Dispositivos d = dispositivo(u);
        String t1 = sesiones.crear(u.getId(), d.getId());
        sesiones.cerrar(jwt.jti(t1).orElseThrow());
        assertTrue(sesiones.validar(t1).isEmpty());

        String t2 = sesiones.crear(u.getId(), d.getId());
        usuarioService.bloquear(u.getId(), null, "PRUEBA");
        assertTrue(sesiones.validar(t2).isEmpty());
        assertEquals(0, jdbc.queryForObject("SELECT count(*) FROM nayra.sesiones WHERE usuario_id = ?::uuid AND fecha_revocacion IS NULL",
                Integer.class, u.getId()));
        usuarioService.desbloquear(u.getId(), null);

        String t3 = sesiones.crear(u.getId(), d.getId());
        dispositivoService.revocarActivo(u.getId());
        assertTrue(sesiones.validar(t3).isEmpty());
        assertNotNull(jdbc.queryForObject("SELECT fecha_revocacion FROM nayra.sesiones WHERE id = ?::uuid", Object.class,
                jwt.jti(t3).orElseThrow()));
    }

    @Test
    void fkYCheckDeSesiones() throws Exception {
        Usuario u = usuario();
        Dispositivos d = dispositivo(u);
        assertThrows(DataIntegrityViolationException.class, () -> jdbc.update("INSERT INTO nayra.sesiones (id, usuario_id, "
                + "dispositivo_id, fecha_creacion, fecha_ultimo_acceso) VALUES (gen_random_uuid(), ?::uuid, gen_random_uuid(), now(), now())",
                u.getId()));
        assertThrows(DataIntegrityViolationException.class, () -> jdbc.update("INSERT INTO nayra.sesiones (id, usuario_id, "
                + "dispositivo_id, fecha_creacion, fecha_ultimo_acceso) VALUES (gen_random_uuid(), gen_random_uuid(), ?::uuid, now(), now())",
                d.getId()));
        assertThrows(DataIntegrityViolationException.class, () -> jdbc.update("INSERT INTO nayra.sesiones (id, usuario_id, "
                + "dispositivo_id, fecha_creacion, fecha_ultimo_acceso) VALUES (gen_random_uuid(), ?::uuid, ?::uuid, now(), "
                + "now() - interval '1 second')", u.getId(), d.getId()));
    }

    // ---- operaciones ----

    @Test
    void operacionValidaSeGuardaConPenMovilYCodigoDeSeisDigitos() {
        Cuentas origen = cuenta(usuario());
        Cuentas destino = cuenta(usuario());
        Operaciones o = operacion(origen, destino, "499.99");
        operacion(origen, destino, "0.01");
        Operaciones leida = operaciones.porId(o.getId()).orElseThrow();
        assertEquals("PEN", leida.getMoneda());
        assertEquals(Operaciones.Canal.MOVIL, leida.getCanal());
        assertEquals(Operaciones.Tipo.TRANSFERENCIA, leida.getTipo());
        assertTrue(leida.getCodigoReferencia().matches("\\d{6}"));
        assertEquals(o.getId(), operaciones.porCodigoReferencia(o.getCodigoReferencia()).orElseThrow().getId());
        assertEquals(destino.getId(), leida.getCuentaDestinoId());
    }

    /** E-03: lo que valida Java coincide con numeric(15,2) y se guarda sin redondeo. */
    @Test
    void elMontoValidadoSeGuardaExactoYElDeTresDecimalesNoLlegaALaBase() {
        Cuentas origen = cuenta(usuario());
        Cuentas destino = cuenta(usuario());
        Operaciones o = operacion(origen, destino, "150.5");
        assertEquals(0, new BigDecimal("150.50").compareTo(jdbc.queryForObject(
                "SELECT monto FROM nayra.operaciones WHERE id = ?::uuid", BigDecimal.class, o.getId())));
        assertEquals(new BigDecimal("150.50"), operaciones.porId(o.getId()).orElseThrow().getMonto());
        Integer antes = jdbc.queryForObject("SELECT count(*) FROM nayra.operaciones", Integer.class);
        assertThrows(IllegalArgumentException.class, () -> operacion(origen, destino, "0.005"));
        assertThrows(IllegalArgumentException.class, () -> operacion(origen, destino, "499.995"));
        assertEquals(antes, jdbc.queryForObject("SELECT count(*) FROM nayra.operaciones", Integer.class));
    }

    @Test
    void restriccionesDeOperaciones() {
        String a = cuenta(usuario()).getId();
        String b = cuenta(usuario()).getId();
        insertarOperacionSql(a, b, "1.00", "PEN", "100000", "EXITOSO", "MOVIL", "TRANSFERENCIA");
        // Monto: > 0 y < 500.
        for (String monto : List.of("0", "-1", "500", "500.01")) {
            assertThrows(DataIntegrityViolationException.class,
                    () -> insertarOperacionSql(a, b, monto, "PEN", "100001", "EXITOSO", "MOVIL", "TRANSFERENCIA"), monto);
        }
        assertThrows(DataIntegrityViolationException.class,
                () -> insertarOperacionSql(a, b, "1", "USD", "100002", "EXITOSO", "MOVIL", "TRANSFERENCIA"));
        assertThrows(DataIntegrityViolationException.class,
                () -> insertarOperacionSql(a, a, "1", "PEN", "100003", "EXITOSO", "MOVIL", "TRANSFERENCIA"), "Misma cuenta");
        assertThrows(DataIntegrityViolationException.class,
                () -> insertarOperacionSql(a, b, "1", "PEN", "100000", "EXITOSO", "MOVIL", "TRANSFERENCIA"), "Código repetido");
        for (String codigo : List.of("12345", "12345A", "ABCDEF")) {
            assertThrows(DataIntegrityViolationException.class,
                    () -> insertarOperacionSql(a, b, "1", "PEN", codigo, "EXITOSO", "MOVIL", "TRANSFERENCIA"), codigo);
        }
        assertThrows(DataIntegrityViolationException.class,
                () -> insertarOperacionSql(a, b, "1", "PEN", "100004", "PENDIENTE", "MOVIL", "TRANSFERENCIA"));
        assertThrows(DataIntegrityViolationException.class,
                () -> insertarOperacionSql(a, b, "1", "PEN", "100005", "EXITOSO", "WEB", "TRANSFERENCIA"));
        assertThrows(DataIntegrityViolationException.class,
                () -> insertarOperacionSql(a, b, "1", "PEN", "100006", "EXITOSO", "MOVIL", "RETIRO"));
        assertThrows(DataIntegrityViolationException.class,
                () -> insertarOperacionSql(a, Identificadores.nuevo(), "1", "PEN", "100007", "EXITOSO", "MOVIL", "TRANSFERENCIA"));
        for (String estado : List.of("FALLIDO", "CANCELADO")) {
            insertarOperacionSql(a, b, "1", "PEN", estado.equals("FALLIDO") ? "100008" : "100009", estado, "MOVIL", "TRANSFERENCIA");
        }
    }

    @Test
    void codigoDeReferenciaRepetidoSeRechazaAlGuardar() {
        Cuentas origen = cuenta(usuario());
        Cuentas destino = cuenta(usuario());
        Operaciones primera = operacion(origen, destino, "5.00");
        Operaciones repetida = new Operaciones(Identificadores.nuevo(), origen.getId(), destino.getId(), BigDecimal.ONE, "detalle",
                primera.getCodigoReferencia(), Operaciones.Estado.EXITOSO, ahora);
        assertThrows(DataIntegrityViolationException.class, () -> operaciones.guardar(repetida));
    }

    // ---- notificaciones ----

    @Test
    void notificacionDeOperacionConTextoGeneradoYSinColumnasDuplicadas() {
        Usuario emisor = usuario();
        Usuario receptor = usuario();
        Operaciones o = operacion(cuenta(emisor), cuenta(receptor), "150.00");
        String contenido = Notificaciones.contenidoTransferencia(emisor.getNombres(), emisor.getApellidos(), o.getMonto());
        assertEquals("Juan Pér... te realizó una transferencia de S/ 150.00.", contenido);
        Notificaciones n = new Notificaciones(Identificadores.nuevo(), receptor.getId(), o.getId(), "Transferencia recibida",
                contenido, ahora);
        notificaciones.guardar(n);
        Notificaciones leida = notificaciones.porId(n.getId()).orElseThrow();
        assertEquals(Notificaciones.Tipo.OPERACION, leida.getTipo());
        assertFalse(leida.isLeida());
        leida.marcarLeida(ahora);
        notificaciones.guardar(leida);
        assertEquals(List.of(n.getId()), notificaciones.deDestinatario(receptor.getId()).stream().map(Notificaciones::getId).toList());
        assertTrue(notificaciones.porId(n.getId()).orElseThrow().isLeida());
        assertEquals(List.of("contenido", "destinatario_id", "fecha_generacion", "fecha_lectura", "id", "leida", "operacion_id", "tipo",
                "titulo"), jdbc.queryForList("SELECT column_name FROM information_schema.columns WHERE table_schema = 'nayra' "
                + "AND table_name = 'notificaciones' ORDER BY column_name", String.class));
    }

    @Test
    void restriccionesDeNotificaciones() {
        Usuario receptor = usuario();
        Operaciones o = operacion(cuenta(usuario()), cuenta(receptor), "1.00");
        String insertar = "INSERT INTO nayra.notificaciones (id, destinatario_id, operacion_id, tipo, titulo, contenido, leida, "
                + "fecha_generacion, fecha_lectura) VALUES (gen_random_uuid(), ?::uuid, ?::uuid, ?, 'T', 'C', ?, now(), ?::timestamptz)";
        assertThrows(DataIntegrityViolationException.class, () -> jdbc.update(insertar, receptor.getId(), o.getId(), "SEGURIDAD", false, null));
        assertThrows(DataIntegrityViolationException.class, () -> jdbc.update(insertar, receptor.getId(), null, "OPERACION", false, null));
        assertThrows(DataIntegrityViolationException.class,
                () -> jdbc.update(insertar, receptor.getId(), Identificadores.nuevo(), "OPERACION", false, null));
        assertThrows(DataIntegrityViolationException.class,
                () -> jdbc.update(insertar, Identificadores.nuevo(), o.getId(), "OPERACION", false, null));
        assertThrows(DataIntegrityViolationException.class, () -> jdbc.update(insertar, receptor.getId(), o.getId(), "OPERACION", true, null));
        jdbc.update(insertar, receptor.getId(), o.getId(), "OPERACION", false, null);
        assertThrows(DataIntegrityViolationException.class, () -> jdbc.update(insertar, receptor.getId(), o.getId(), "OPERACION", false, null),
                "Una notificación por operación y destinatario");
    }
}
