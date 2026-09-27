package upc.pe.nayrabackend.soporte;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import upc.pe.nayrabackend.config.EntornoSimuladoInicial;
import upc.pe.nayrabackend.config.NayraProperties;
import upc.pe.nayrabackend.config.VozProperties;
import upc.pe.nayrabackend.dtos.AutenticacionVozDTOs.DesafioDTO;
import upc.pe.nayrabackend.dtos.AutenticacionVozDTOs.ResultadoPaso;
import upc.pe.nayrabackend.dtos.RegistroDTOs.RegistroFinalizado;
import upc.pe.nayrabackend.dtos.RegistroDTOs.SolicitudDatosRegistro;
import upc.pe.nayrabackend.repositories.memoria.AuditoriaEnMemoria;
import upc.pe.nayrabackend.repositories.memoria.CredencialesEnMemoria;
import upc.pe.nayrabackend.repositories.memoria.DispositivosEnMemoria;
import upc.pe.nayrabackend.repositories.memoria.EntornoSimuladoEnMemoria;
import upc.pe.nayrabackend.repositories.memoria.SesionesEnMemoria;
import upc.pe.nayrabackend.repositories.memoria.UsuariosEnMemoria;
import upc.pe.nayrabackend.securities.TokenSesionJwt;
import upc.pe.nayrabackend.serviceimplements.*;
import upc.pe.nayrabackend.serviceinterfaces.IServicioVozCliente;

import java.nio.file.Path;
import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Deque;
import java.util.List;

/**
 * Ensambla el backend general y AG-13 sin Spring ni base de datos, con los adaptadores en memoria de prueba,
 * los datos ficticios del entorno simulado y un servicio de voz falso.
 */
public final class Soporte {

    /** Reloj que avanza a mano para probar vencimientos. */
    public static final class RelojManual extends Clock {
        Instant ahora = Instant.parse("2026-09-27T10:00:00Z");
        public void avanzar(Duration d) { ahora = ahora.plus(d); }
        @Override public ZoneId getZone() { return ZoneOffset.UTC; }
        @Override public Clock withZone(ZoneId zone) { return this; }
        @Override public Instant instant() { return ahora; }
    }

    /** Servicio de voz falso: devuelve los resultados encolados y registra los desafíos recibidos. */
    public static final class VozFalsa implements IServicioVozCliente {
        public final Deque<Object> respuestas = new ArrayDeque<>();
        public final List<String> desafiosRecibidos = new ArrayList<>();
        public String pinDictado;
        public boolean caido;

        public void encolar(boolean aprobado, String motivo) {
            respuestas.add(new ResultadoTecnico(aprobado, motivo, List.of(), null));
        }

        private void comprobar() {
            if (caido) throw new ServicioVozNoDisponibleException("caído", null);
        }

        @Override public ResultadoTecnico verificar(String usuarioId, String desafio, byte[] audioWav) {
            comprobar();
            desafiosRecibidos.add(desafio);
            return (ResultadoTecnico) respuestas.pop();
        }
        @Override public ResultadoTecnico agregarMuestraEnrolamiento(String usuarioId, String desafio, byte[] audioWav) {
            comprobar();
            desafiosRecibidos.add(desafio);
            return (ResultadoTecnico) respuestas.pop();
        }
        @Override public ResultadoEnrolamiento finalizarEnrolamiento(String usuarioId) {
            comprobar();
            return (ResultadoEnrolamiento) respuestas.pop();
        }
        @Override public String transcribirPin(byte[] audioWav) {
            comprobar();
            return pinDictado;
        }
    }

    /** Persona registrada en las pruebas, con la clave privada de su "celular". */
    public record Persona(String usuarioId, String dispositivoId, KeyPair par, String pin) {
    }

    public static final String PIN = "482913";
    public static final String CELULAR = "987654321";
    /** DNI ficticios de entorno-simulado/datos-ficticios.json. 10000001 no tiene cuenta financiera. */
    public static final String DNI_ADMIN = "10000001";
    public static final String DNI_USUARIO = "10000002";
    public static final String DNI_USUARIO_2 = "10000003";
    public static final byte[] AUDIO = new byte[]{1, 2, 3};

    public final RelojManual reloj = new RelojManual();
    public final VozFalsa voz = new VozFalsa();
    public final VozProperties vozProps = new VozProperties("http://localhost:0", "", Duration.ofSeconds(1), Duration.ofSeconds(1),
            Path.of("..", "shared", "desafio", "vocabulario_v2.json").toString(),
            Duration.ofSeconds(120), Duration.ofMinutes(5), 3);
    public final NayraProperties props = new NayraProperties(new NayraProperties.Sesion(Duration.ofMinutes(5), null),
            new NayraProperties.Dispositivo(Duration.ofSeconds(60)), new NayraProperties.Registro(Duration.ofSeconds(900)));
    private final SecureRandom random = new SecureRandom();

    public final UsuariosEnMemoria usuariosRepo = new UsuariosEnMemoria();
    public final DispositivosEnMemoria dispositivosRepo = new DispositivosEnMemoria();
    public final SesionesEnMemoria sesionesRepo = new SesionesEnMemoria();
    public final CredencialesEnMemoria credencialesRepo = new CredencialesEnMemoria();
    public final AuditoriaEnMemoria auditoriaRepo = new AuditoriaEnMemoria();
    public final EntornoSimuladoEnMemoria entorno = new EntornoSimuladoEnMemoria();

    public final AuditoriaServiceImplement auditoria = new AuditoriaServiceImplement(auditoriaRepo, reloj);
    public final PinServiceImplement pines = new PinServiceImplement(new BCryptPasswordEncoder(4));
    public final DispositivoServiceImplement dispositivos = new DispositivoServiceImplement(dispositivosRepo, props, random, reloj);
    public final TokenSesionJwt jwt = new TokenSesionJwt(props, random);
    public final SesionesServiceImplement sesiones = new SesionesServiceImplement(sesionesRepo, usuariosRepo, dispositivosRepo,
            auditoria, jwt, reloj, props);
    public final CredencialesServiceImplement credenciales = new CredencialesServiceImplement(credencialesRepo, pines, reloj);
    public final UsuarioServiceImplement usuarios = new UsuarioServiceImplement(usuariosRepo, dispositivosRepo, sesiones,
            credenciales, auditoria, reloj);
    public final CuentaServiceImplement cuentas = new CuentaServiceImplement(entorno, entorno, reloj);
    public final RegistroServiceImplement registro = new RegistroServiceImplement(usuariosRepo, entorno, cuentas, pines,
            credenciales, dispositivos, auditoria, props, random, reloj);
    public final AdministracionServiceImplement admin = new AdministracionServiceImplement(usuariosRepo, dispositivosRepo,
            usuarios, dispositivos, sesiones, auditoria);
    public final DesafioServiceImplement desafios;
    public final AutenticacionVozServiceImplement autenticacion;
    public final EnrolamientoVozServiceImplement enrolamiento;

    public Soporte() throws Exception {
        new EntornoSimuladoInicial(entorno, entorno, entorno, reloj);
        desafios = new DesafioServiceImplement(vozProps, random, reloj);
        autenticacion = new AutenticacionVozServiceImplement(dispositivos, credenciales, desafios, voz, auditoria,
                new PoliticaIntentosProvisional(), usuariosRepo, usuarios, sesiones, vozProps, reloj);
        enrolamiento = new EnrolamientoVozServiceImplement(desafios, voz, auditoria, registro, vozProps);
    }

    /** Pasos 7–13 del registro en el celular: confirma, datos, 3 muestras de voz y finalización. */
    public Persona completarRegistro(String codigo, String pin) throws Exception {
        KeyPair par = claveP256();
        registro.completarDatos(codigo, new SolicitudDatosRegistro(true, CELULAR, pin, publica(par)));
        for (int i = 1; i <= 3; i++) {
            DesafioDTO d = enrolamiento.emitirDesafio(codigo);
            voz.respuestas.add(new IServicioVozCliente.ResultadoTecnico(true, null, List.of(), i));
            if (!enrolamiento.enviarMuestra(codigo, d.desafioId(), AUDIO).aceptada()) {
                throw new IllegalStateException("muestra rechazada");
            }
        }
        voz.respuestas.add(new IServicioVozCliente.ResultadoEnrolamiento(true, null, 3));
        if (!enrolamiento.finalizar(codigo).correcto()) {
            throw new IllegalStateException("enrolamiento fallido");
        }
        RegistroFinalizado fin = registro.finalizar(codigo);
        voz.desafiosRecibidos.clear();
        return new Persona(fin.usuarioId(), fin.dispositivoId(), par, pin);
    }

    /** Primer administrador por la ruta de arranque del prototipo. */
    public Persona crearAdministrador() throws Exception {
        return completarRegistro(registro.iniciarAdministradorInicial("DNI", DNI_ADMIN).codigoRegistro(), PIN);
    }

    /** Registro asistido completo de un USER por un administrador. */
    public Persona registrarUsuario(Persona representante, String dni) throws Exception {
        String codigo = registro.iniciar(representante.usuarioId(), "DNI", dni).codigoRegistro();
        registro.validarIdentidad(representante.usuarioId(), codigo);
        return completarRegistro(codigo, PIN);
    }

    /** Abre una transacción firmando el nonce con la clave del dispositivo. */
    public String abrirTransaccion(Persona p) throws Exception {
        String nonce = autenticacion.emitirNonce(p.dispositivoId());
        return autenticacion.abrirTransaccion(p.dispositivoId(), nonce, firmar(p.par(), nonce, p.dispositivoId(), "INICIO_SESION"));
    }

    /** Inicio de sesión completo con voz aprobada; devuelve el token de sesión. */
    public String iniciarSesion(Persona p) throws Exception {
        String t = abrirTransaccion(p);
        autenticacion.verificarPin(t, p.pin());
        voz.encolar(true, null);
        ResultadoPaso r = autenticacion.verificarVoz(t, AUDIO);
        if (!"AUTENTICADO".equals(r.estado())) {
            throw new IllegalStateException(r.estado());
        }
        return r.sesion();
    }

    public static KeyPair claveP256() throws GeneralSecurityException {
        KeyPairGenerator g = KeyPairGenerator.getInstance("EC");
        g.initialize(new ECGenParameterSpec("secp256r1"));
        return g.generateKeyPair();
    }

    public static String publica(KeyPair par) {
        return Base64.getEncoder().encodeToString(par.getPublic().getEncoded());
    }

    public static String firmar(KeyPair par, String nonce, String dispositivoId, String proposito) throws GeneralSecurityException {
        Signature s = Signature.getInstance("SHA256withECDSA");
        s.initSign(par.getPrivate());
        s.update(DispositivoServiceImplement.mensajeFirmado(nonce, dispositivoId, proposito));
        return Base64.getEncoder().encodeToString(s.sign());
    }
}
