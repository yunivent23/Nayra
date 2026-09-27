package upc.pe.nayrabackend.persistencia;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import upc.pe.nayrabackend.dtos.AutenticacionVozDTOs.ResultadoPaso;
import upc.pe.nayrabackend.entities.Dispositivos;
import upc.pe.nayrabackend.entities.Identificadores;
import upc.pe.nayrabackend.entities.Rol;
import upc.pe.nayrabackend.entities.Sesiones;
import upc.pe.nayrabackend.entities.TipoDocumentoIdentidad;
import upc.pe.nayrabackend.entities.Usuario;
import upc.pe.nayrabackend.repositories.ICredencialesRepository;
import upc.pe.nayrabackend.repositories.ISesionesRepository;
import upc.pe.nayrabackend.repositories.IUsuariosRepository;
import upc.pe.nayrabackend.securities.TokenSesionJwt;
import upc.pe.nayrabackend.serviceinterfaces.IAutenticacionVozService;
import upc.pe.nayrabackend.serviceinterfaces.ICredencialesService;
import upc.pe.nayrabackend.serviceinterfaces.IDispositivoService;
import upc.pe.nayrabackend.serviceinterfaces.IPinService;
import upc.pe.nayrabackend.serviceinterfaces.ISesionesService;
import upc.pe.nayrabackend.serviceinterfaces.IUsuarioService;
import upc.pe.nayrabackend.soporte.BaseDeDatosDePrueba;
import upc.pe.nayrabackend.soporte.Soporte;

import java.security.KeyPair;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Concurrencia contra PostgreSQL real (auditoría de consistencia v4, E-01 y E-02).
 *
 * E-01: el contador de PIN se incrementa con un UPDATE atómico; varios PIN incorrectos simultáneos cuentan todos.
 * E-02: una sesión solo cambia con UPDATE condicionales; una revocación no puede ser deshecha por una petición
 * concurrente ni por una copia leída antes.
 *
 * Las pruebas lanzan las operaciones a la vez con una barrera y repiten varias rondas. No pueden forzar un
 * intercalado exacto, pero con el patrón anterior (leer, sumar en Java y guardar la entidad) perderían incrementos.
 */
@SpringBootTest
@BaseDeDatosDePrueba
@ActiveProfiles({"prototipo", "pruebasbd"})
class ConcurrenciaPostgresTest {

    @TestConfiguration
    static class VozDePrueba {
        @Bean
        @Primary
        Soporte.VozFalsa vozFalsa() {
            return new Soporte.VozFalsa();
        }
    }

    private static final AtomicInteger SECUENCIA = new AtomicInteger(60000000);
    private static final int RONDAS = 10;

    @Autowired JdbcTemplate jdbc;
    @Autowired IUsuariosRepository usuarios;
    @Autowired ICredencialesRepository credencialesRepo;
    @Autowired ISesionesRepository sesionesRepo;
    @Autowired ICredencialesService credenciales;
    @Autowired ISesionesService sesiones;
    @Autowired IUsuarioService usuarioService;
    @Autowired IDispositivoService dispositivoService;
    @Autowired IAutenticacionVozService autenticacion;
    @Autowired IPinService pines;
    @Autowired TokenSesionJwt jwt;

    private Instant ahora;

    @BeforeEach
    void preparar() {
        ahora = Instant.now().truncatedTo(ChronoUnit.MICROS);
    }

    private Usuario usuarioConCredencial() {
        Usuario u = new Usuario(Identificadores.nuevo(), TipoDocumentoIdentidad.DNI, String.valueOf(SECUENCIA.incrementAndGet()),
                "Ana", "Torres Ruiz", "987654321", Rol.USER, ahora);
        usuarios.guardar(u);
        credenciales.crear(u.getId(), pines.hashear(Soporte.PIN));
        return u;
    }

    private int intentosEnBase(String usuarioId) {
        return jdbc.queryForObject("SELECT intentos_fallidos FROM nayra.credenciales WHERE usuario_id = ?::uuid",
                Integer.class, usuarioId);
    }

    /** Ejecuta las tareas a la vez (barrera) y devuelve sus resultados. */
    private static <T> List<T> aLaVez(List<Callable<T>> tareas) throws Exception {
        ExecutorService hilos = Executors.newFixedThreadPool(tareas.size());
        try {
            CyclicBarrier barrera = new CyclicBarrier(tareas.size());
            List<Future<T>> futuros = new ArrayList<>();
            for (Callable<T> t : tareas) {
                futuros.add(hilos.submit(() -> {
                    barrera.await(10, TimeUnit.SECONDS);
                    return t.call();
                }));
            }
            List<T> resultados = new ArrayList<>();
            for (Future<T> f : futuros) {
                resultados.add(f.get(60, TimeUnit.SECONDS));
            }
            return resultados;
        } finally {
            hilos.shutdownNow();
        }
    }

    // ---- E-01: contador de PIN ----

    @Test
    void unPinIncorrectoIncrementaExactamenteUnaVez() {
        Usuario u = usuarioConCredencial();
        ICredencialesService.ResultadoPin r = credenciales.verificar(u.getId(), "000000");
        assertFalse(r.correcto());
        assertEquals(1, intentosEnBase(u.getId()));
        assertEquals(2, r.intentosRestantes());
        credenciales.verificar(u.getId(), "111111");
        assertEquals(2, intentosEnBase(u.getId()));
    }

    @Test
    void dosPinIncorrectosSimultaneosCuentanComoDos() throws Exception {
        for (int ronda = 0; ronda < RONDAS; ronda++) {
            Usuario u = usuarioConCredencial();
            List<ICredencialesService.ResultadoPin> r = aLaVez(List.of(
                    () -> credenciales.verificar(u.getId(), "000000"),
                    () -> credenciales.verificar(u.getId(), "111111")));
            assertEquals(2, intentosEnBase(u.getId()), "ronda " + ronda + ": no se pierde ningún incremento");
            assertEquals(List.of(1, 2), r.stream().map(ICredencialesService.ResultadoPin::intentosRestantes).sorted().toList(),
                    "cada intento ve su propio valor del contador");
        }
    }

    @Test
    void muchosFallosSimultaneosNoPasanDeTresYCadaValorSeAsignaUnaVez() throws Exception {
        for (int ronda = 0; ronda < RONDAS; ronda++) {
            Usuario u = usuarioConCredencial();
            List<Callable<Integer>> tareas = new ArrayList<>();
            for (int i = 0; i < 8; i++) {
                tareas.add(() -> credencialesRepo.registrarFallo(u.getId(), ICredencialesService.INTENTOS_MAXIMOS, Instant.now())
                        .orElseThrow());
            }
            List<Integer> valores = aLaVez(tareas);
            assertEquals(3, intentosEnBase(u.getId()));
            assertEquals(1, Collections.frequency(valores, 1), "ronda " + ronda + ": " + valores);
            assertEquals(1, Collections.frequency(valores, 2), "ronda " + ronda + ": " + valores);
            assertEquals(6, Collections.frequency(valores, 3), "ronda " + ronda + ": " + valores);
        }
    }

    @Test
    void registrarFalloSinCredencialNoDevuelveValor() {
        assertTrue(credencialesRepo.registrarFallo(Identificadores.nuevo(), 3, ahora).isEmpty());
        assertTrue(credencialesRepo.registrarFallo("no-es-uuid", 3, ahora).isEmpty());
    }

    private record Autenticable(Usuario usuario, Dispositivos dispositivo, KeyPair par) {
    }

    private Autenticable autenticable() throws Exception {
        Usuario u = usuarioConCredencial();
        KeyPair par = Soporte.claveP256();
        return new Autenticable(u, dispositivoService.vincular(u.getId(), par.getPublic()), par);
    }

    private String transaccion(Autenticable a) throws Exception {
        String nonce = autenticacion.emitirNonce(a.dispositivo().getId());
        return autenticacion.abrirTransaccion(a.dispositivo().getId(), nonce,
                Soporte.firmar(a.par(), nonce, a.dispositivo().getId(), IAutenticacionVozService.PROPOSITO_INICIO_SESION));
    }

    @Test
    void elTercerPinIncorrectoBloqueaYRevocaLasSesiones() throws Exception {
        Autenticable a = autenticable();
        String sesionPrevia = sesiones.crear(a.usuario().getId(), a.dispositivo().getId());
        String t = transaccion(a);
        assertEquals("REINTENTAR", autenticacion.verificarPin(t, "000000").estado());
        assertEquals("REINTENTAR", autenticacion.verificarPin(t, "111111").estado());
        ResultadoPaso tercero = autenticacion.verificarPin(t, "222222");
        assertEquals("BLOQUEADA", tercero.estado());
        assertEquals(3, intentosEnBase(a.usuario().getId()));
        assertEquals(Usuario.Estado.BLOQUEADO, usuarios.porId(a.usuario().getId()).orElseThrow().getEstado());
        assertTrue(sesiones.validar(sesionPrevia).isEmpty(), "El bloqueo revoca las sesiones (D-040)");
    }

    @Test
    void tresPinIncorrectosSimultaneosBloquean() throws Exception {
        for (int ronda = 0; ronda < RONDAS / 2; ronda++) {
            Autenticable a = autenticable();
            // Tres transacciones abiertas (cada una con su nonce firmado) que envían el PIN incorrecto a la vez.
            List<String> ts = List.of(transaccion(a), transaccion(a), transaccion(a));
            List<Callable<String>> tareas = new ArrayList<>();
            for (String t : ts) {
                tareas.add(() -> autenticacion.verificarPin(t, "000000").estado());
            }
            List<String> estados = aLaVez(tareas);
            assertEquals(3, intentosEnBase(a.usuario().getId()), "ronda " + ronda + ": " + estados);
            assertTrue(estados.contains("BLOQUEADA"), "ronda " + ronda + ": " + estados);
            assertEquals(Usuario.Estado.BLOQUEADO, usuarios.porId(a.usuario().getId()).orElseThrow().getEstado());
        }
    }

    // ---- E-02: sesiones ----

    @Test
    void unaCopiaLeidaAntesDeLaRevocacionNoLaDeshace() throws Exception {
        Autenticable a = autenticable();
        String token = sesiones.crear(a.usuario().getId(), a.dispositivo().getId());
        String jti = jwt.jti(token).orElseThrow();
        Sesiones copiaAntigua = sesionesRepo.porId(jti).orElseThrow();   // "petición A" lee la sesión vigente
        assertNull(copiaAntigua.getFechaRevocacion());

        sesiones.cerrar(jti);                                            // "petición B" la revoca

        // A intenta registrar su actividad: no cambia nada y la sesión sigue revocada.
        assertFalse(sesionesRepo.registrarAcceso(jti, Instant.now(), Instant.now().minusSeconds(300)));
        // La copia antigua ya no se puede guardar encima: el alta rechaza un id existente (Spring traduce la excepción).
        RuntimeException e = assertThrows(RuntimeException.class, () -> sesionesRepo.crear(copiaAntigua));
        assertTrue(e.getMessage().contains("La sesión ya existe"), e.getMessage());
        assertNotNull(jdbc.queryForObject("SELECT fecha_revocacion FROM nayra.sesiones WHERE id = ?::uuid", Object.class, jti));
        assertTrue(sesiones.validar(token).isEmpty());
    }

    @Test
    void revocarEsIdempotenteYSoloLaPrimeraLlamadaRevoca() throws Exception {
        Autenticable a = autenticable();
        String jti = jwt.jti(sesiones.crear(a.usuario().getId(), a.dispositivo().getId())).orElseThrow();
        List<Callable<Boolean>> tareas = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            tareas.add(() -> sesionesRepo.revocar(jti, Instant.now()));
        }
        List<Boolean> r = aLaVez(tareas);
        assertEquals(1, Collections.frequency(r, true), r.toString());
        Object primera = jdbc.queryForObject("SELECT fecha_revocacion FROM nayra.sesiones WHERE id = ?::uuid", Object.class, jti);
        assertFalse(sesionesRepo.revocar(jti, Instant.now().plusSeconds(60)));
        assertEquals(primera, jdbc.queryForObject("SELECT fecha_revocacion FROM nayra.sesiones WHERE id = ?::uuid", Object.class, jti),
                "La fecha de revocación no cambia después");
    }

    @Test
    void validacionesSimultaneasConUnCierreDejanLaSesionRevocada() throws Exception {
        for (int ronda = 0; ronda < RONDAS; ronda++) {
            Autenticable a = autenticable();
            String token = sesiones.crear(a.usuario().getId(), a.dispositivo().getId());
            String jti = jwt.jti(token).orElseThrow();
            List<Callable<Boolean>> tareas = new ArrayList<>();
            for (int i = 0; i < 6; i++) {
                tareas.add(() -> {
                    boolean alguna = false;
                    for (int j = 0; j < 5; j++) {
                        alguna |= sesiones.validar(token).isPresent();
                    }
                    return alguna;
                });
            }
            tareas.add(() -> {
                sesiones.cerrar(jti);
                return false;
            });
            aLaVez(tareas);
            assertNotNull(jdbc.queryForObject("SELECT fecha_revocacion FROM nayra.sesiones WHERE id = ?::uuid", Object.class, jti),
                    "ronda " + ronda + ": la revocación no se pierde");
            assertTrue(sesiones.validar(token).isEmpty(), "ronda " + ronda);
        }
    }

    @Test
    void validacionesSimultaneasConUnBloqueoDejanLasSesionesRevocadas() throws Exception {
        for (int ronda = 0; ronda < RONDAS / 2; ronda++) {
            Autenticable a = autenticable();
            String t1 = sesiones.crear(a.usuario().getId(), a.dispositivo().getId());
            String t2 = sesiones.crear(a.usuario().getId(), a.dispositivo().getId());
            List<Callable<Boolean>> tareas = new ArrayList<>();
            for (String t : List.of(t1, t2, t1, t2)) {
                tareas.add(() -> sesiones.validar(t).isPresent());
            }
            tareas.add(() -> {
                usuarioService.bloquear(a.usuario().getId(), null, "PRUEBA");
                return false;
            });
            aLaVez(tareas);
            assertEquals(0, jdbc.queryForObject("SELECT count(*) FROM nayra.sesiones WHERE usuario_id = ?::uuid "
                    + "AND fecha_revocacion IS NULL", Integer.class, a.usuario().getId()), "ronda " + ronda);
            assertTrue(sesiones.validar(t1).isEmpty());
            assertTrue(sesiones.validar(t2).isEmpty());
        }
    }

    @Test
    void registrarAccesoNoReabreUnaSesionVencida() throws Exception {
        Autenticable a = autenticable();
        String jti = jwt.jti(sesiones.crear(a.usuario().getId(), a.dispositivo().getId())).orElseThrow();
        jdbc.update("UPDATE nayra.sesiones SET fecha_creacion = fecha_creacion - interval '6 minutes', "
                + "fecha_ultimo_acceso = fecha_ultimo_acceso - interval '6 minutes' WHERE id = ?::uuid", jti);
        Object antes = jdbc.queryForObject("SELECT fecha_ultimo_acceso FROM nayra.sesiones WHERE id = ?::uuid", Object.class, jti);
        Instant t = Instant.now();
        assertFalse(sesionesRepo.registrarAcceso(jti, t, t.minusSeconds(300)), "Una petición que la leyó antes no la reabre");
        assertEquals(antes, jdbc.queryForObject("SELECT fecha_ultimo_acceso FROM nayra.sesiones WHERE id = ?::uuid", Object.class, jti));
    }

    @Test
    void laFechaDeUltimoAccesoNuncaRetrocede() throws Exception {
        Autenticable a = autenticable();
        String jti = jwt.jti(sesiones.crear(a.usuario().getId(), a.dispositivo().getId())).orElseThrow();
        Instant t = Instant.now().plusSeconds(30);
        assertTrue(sesionesRepo.registrarAcceso(jti, t, t.minusSeconds(300)));
        Object tras = jdbc.queryForObject("SELECT fecha_ultimo_acceso FROM nayra.sesiones WHERE id = ?::uuid", Object.class, jti);
        assertTrue(sesionesRepo.registrarAcceso(jti, t.minusSeconds(20), t.minusSeconds(320)), "Sigue vigente");
        assertEquals(tras, jdbc.queryForObject("SELECT fecha_ultimo_acceso FROM nayra.sesiones WHERE id = ?::uuid", Object.class, jti));
    }
}
