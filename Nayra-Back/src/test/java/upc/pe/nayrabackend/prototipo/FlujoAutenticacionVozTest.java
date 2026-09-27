package upc.pe.nayrabackend.prototipo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import upc.pe.nayrabackend.dtos.AutenticacionVozDTOs.ResultadoPaso;
import upc.pe.nayrabackend.entities.Usuario;
import upc.pe.nayrabackend.excepciones.NayraException;
import upc.pe.nayrabackend.soporte.Soporte;
import upc.pe.nayrabackend.soporte.Soporte.Persona;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static upc.pe.nayrabackend.soporte.Soporte.AUDIO;

/**
 * Inicio de sesión AG-13 sobre el backend general: dispositivo → PIN → desafío → voz → decisión (D-056) → sesión.
 * La persona se registra con el registro asistido (D-052), que incluye el enrolamiento.
 */
class FlujoAutenticacionVozTest {

    private Soporte s;
    private Persona admin;
    private Persona usuario;

    @BeforeEach
    void registrar() throws Exception {
        s = new Soporte();
        admin = s.crearAdministrador();
        usuario = s.registrarUsuario(admin, Soporte.DNI_USUARIO);
    }

    @Test
    void flujoCorrectoCreaUnaSesion() throws Exception {
        String t = s.abrirTransaccion(usuario);
        ResultadoPaso pin = s.autenticacion.verificarPin(t, Soporte.PIN);
        assertEquals("CONTINUAR", pin.estado());
        assertNotNull(pin.desafio());
        assertNull(pin.sesion());
        s.voz.encolar(true, null);
        ResultadoPaso voz = s.autenticacion.verificarVoz(t, AUDIO);
        assertEquals("AUTENTICADO", voz.estado());
        assertEquals(pin.desafio().texto(), s.voz.desafiosRecibidos.getFirst());
        var sesion = s.sesiones.validar(voz.sesion()).orElseThrow();
        assertEquals(usuario.usuarioId(), sesion.usuarioId());
        assertEquals(usuario.dispositivoId(), sesion.dispositivoId());
        // La transacción termina: no puede reutilizarse.
        assertEquals("TRANSACCION_NO_VALIDA",
                assertThrows(NayraException.class, () -> s.autenticacion.verificarVoz(t, AUDIO)).getCodigo());
        assertTrue(s.auditoriaRepo.todos().stream().anyMatch(e -> e.accion().equals("AUTENTICACION_EXITOSA")
                && usuario.usuarioId().equals(e.usuarioAfectadoId()) && usuario.dispositivoId().equals(e.dispositivoId())));
    }

    @Test
    void elDesafioNoSeEmiteAntesDelPin() throws Exception {
        String t = s.abrirTransaccion(usuario);
        assertEquals("PASO_NO_VALIDO", assertThrows(NayraException.class, () -> s.autenticacion.verificarVoz(t, AUDIO)).getCodigo());
    }

    @Test
    void tresPinIncorrectosBloqueanLaCuentaYRevocanSusSesiones() throws Exception {
        String token = s.iniciarSesion(usuario);
        String t = s.abrirTransaccion(usuario);
        assertEquals(2, s.autenticacion.verificarPin(t, "000000").intentosRestantes());
        assertEquals(1, s.autenticacion.verificarPin(t, "111111").intentosRestantes());
        assertEquals("BLOQUEADA", s.autenticacion.verificarPin(t, "222222").estado());
        assertEquals(Usuario.Estado.BLOQUEADO, s.usuarios.obtener(usuario.usuarioId()).getEstado());
        assertTrue(s.sesiones.validar(token).isEmpty());
        assertEquals("CUENTA_BLOQUEADA", assertThrows(NayraException.class, () -> s.abrirTransaccion(usuario)).getCodigo());
        assertTrue(s.auditoriaRepo.todos().stream().anyMatch(e -> e.accion().equals("CUENTA_BLOQUEADA")
                && "INTENTOS_AGOTADOS".equals(e.motivo()) && e.actorId() == null));
    }

    @Test
    void calidadYServicioCaidoNoCuentanComoIntento() throws Exception {
        String t = s.abrirTransaccion(usuario);
        s.autenticacion.verificarPin(t, Soporte.PIN);
        s.voz.encolar(false, "CALIDAD_INSUFICIENTE");
        ResultadoPaso r = s.autenticacion.verificarVoz(t, AUDIO);
        assertEquals("REINTENTAR", r.estado());
        assertEquals(3, r.intentosRestantes());
        assertNotNull(r.desafio(), "Cada reintento recibe un desafío nuevo");

        s.voz.caido = true;
        ResultadoPaso caido = s.autenticacion.verificarVoz(t, AUDIO);
        assertEquals("SERVICIO_NO_DISPONIBLE", caido.estado());
        assertEquals(3, caido.intentosRestantes());
    }

    @Test
    void fallosDeVozContenidoOSpoofingNoCuentanComoIntentosDePin() throws Exception {
        // v4 §2.2 y §4.3: solo el PIN incorrecto cuenta; el límite propio de la biometría es P-8 (pendiente).
        String t = s.abrirTransaccion(usuario);
        s.autenticacion.verificarPin(t, Soporte.PIN);
        s.voz.encolar(false, "CONTENIDO_INCORRECTO");
        s.voz.encolar(false, "POSIBLE_SPOOFING");
        s.voz.encolar(false, "NO_COINCIDE");
        s.voz.encolar(false, "NO_COINCIDE");
        ResultadoPaso r1 = s.autenticacion.verificarVoz(t, AUDIO);
        ResultadoPaso r2 = s.autenticacion.verificarVoz(t, AUDIO);
        ResultadoPaso r3 = s.autenticacion.verificarVoz(t, AUDIO);
        ResultadoPaso r4 = s.autenticacion.verificarVoz(t, AUDIO);
        for (ResultadoPaso r : List.of(r1, r2, r3, r4)) {
            assertEquals("REINTENTAR", r.estado());
            assertEquals(3, r.intentosRestantes());
            assertNull(r.sesion());
        }
        assertNotEquals(r1.desafio().desafioId(), r2.desafio().desafioId());
        assertEquals(Usuario.Estado.ACTIVO, s.usuarios.obtener(usuario.usuarioId()).getEstado());
        assertEquals(0, s.credencialesRepo.deUsuario(usuario.usuarioId()).orElseThrow().getIntentosFallidos());
    }

    @Test
    void unPinCorrectoReiniciaElContadorAunqueLaVozFalle() throws Exception {
        String t = s.abrirTransaccion(usuario);
        assertEquals(2, s.autenticacion.verificarPin(t, "000000").intentosRestantes());
        assertEquals(1, s.autenticacion.verificarPin(t, "111111").intentosRestantes());
        assertEquals(2, s.credencialesRepo.deUsuario(usuario.usuarioId()).orElseThrow().getIntentosFallidos());
        // El contador vuelve a 0 con el PIN correcto, antes de la voz (v4 §2.2).
        assertEquals(3, s.autenticacion.verificarPin(t, Soporte.PIN).intentosRestantes());
        assertEquals(0, s.credencialesRepo.deUsuario(usuario.usuarioId()).orElseThrow().getIntentosFallidos());
        s.voz.encolar(false, "NO_COINCIDE");
        assertEquals(3, s.autenticacion.verificarVoz(t, AUDIO).intentosRestantes());
        String t2 = s.abrirTransaccion(usuario);
        assertEquals(2, s.autenticacion.verificarPin(t2, "000000").intentosRestantes());
    }

    @Test
    void elDesbloqueoAdministrativoReiniciaElContadorProvisional() throws Exception {
        // PROVISIONAL (P-11): el desbloqueo reinicia el contador de la credencial.
        String t = s.abrirTransaccion(usuario);
        s.autenticacion.verificarPin(t, "000000");
        s.autenticacion.verificarPin(t, "111111");
        s.autenticacion.verificarPin(t, "222222");
        assertEquals(3, s.credencialesRepo.deUsuario(usuario.usuarioId()).orElseThrow().getIntentosFallidos());
        s.admin.desbloquearUsuario(admin.usuarioId(), usuario.usuarioId());
        assertEquals(0, s.credencialesRepo.deUsuario(usuario.usuarioId()).orElseThrow().getIntentosFallidos());
        assertEquals(3, s.autenticacion.verificarPin(s.abrirTransaccion(usuario), Soporte.PIN).intentosRestantes());
    }

    @Test
    void desafioVencidoDaOtroSinContar() throws Exception {
        String t = s.abrirTransaccion(usuario);
        s.autenticacion.verificarPin(t, Soporte.PIN);
        s.reloj.avanzar(Duration.ofSeconds(121));
        ResultadoPaso r = s.autenticacion.verificarVoz(t, AUDIO);
        assertEquals("DESAFIO_VENCIDO", r.motivo());
        assertEquals(3, r.intentosRestantes());
        s.voz.encolar(true, null);
        assertEquals("AUTENTICADO", s.autenticacion.verificarVoz(t, AUDIO).estado());
    }

    @Test
    void transaccionVenceALosCincoMinutos() throws Exception {
        String t = s.abrirTransaccion(usuario);
        s.reloj.avanzar(Duration.ofMinutes(5).plusSeconds(1));
        assertEquals("TRANSACCION_NO_VALIDA",
                assertThrows(NayraException.class, () -> s.autenticacion.verificarPin(t, Soporte.PIN)).getCodigo());
    }

    @Test
    void pinDictadoSeVerificaIgualQueElTecleado() throws Exception {
        String t = s.abrirTransaccion(usuario);
        s.voz.pinDictado = null;
        assertEquals("PIN_NO_RECONOCIDO", s.autenticacion.verificarPinDictado(t, AUDIO).motivo());
        s.voz.pinDictado = "999999";
        assertEquals(2, s.autenticacion.verificarPinDictado(t, AUDIO).intentosRestantes());
        s.voz.pinDictado = Soporte.PIN;
        assertEquals("CONTINUAR", s.autenticacion.verificarPinDictado(t, AUDIO).estado());
    }

    @Test
    void auditoriaNoContieneElPin() throws Exception {
        String t = s.abrirTransaccion(usuario);
        s.autenticacion.verificarPin(t, "000000");
        s.autenticacion.verificarPin(t, Soporte.PIN);
        assertTrue(s.auditoriaRepo.todos().stream().noneMatch(e -> String.valueOf(e).contains(Soporte.PIN)
                || String.valueOf(e).contains("000000")));
    }

    @Test
    void sinReferenciaBiometricaSeRechazaSinContar() throws Exception {
        String t = s.abrirTransaccion(usuario);
        s.autenticacion.verificarPin(t, Soporte.PIN);
        s.voz.encolar(false, "SIN_REFERENCIA");
        ResultadoPaso r = s.autenticacion.verificarVoz(t, AUDIO);
        assertEquals("RECHAZADO", r.estado());
        assertEquals(3, r.intentosRestantes());
    }

    @Test
    void dispositivoRevocadoNoPuedeIniciarSesion() throws Exception {
        s.admin.revocarDispositivo(admin.usuarioId(), usuario.usuarioId());
        assertThrows(SecurityException.class, () -> s.autenticacion.emitirNonce(usuario.dispositivoId()));
    }
}
