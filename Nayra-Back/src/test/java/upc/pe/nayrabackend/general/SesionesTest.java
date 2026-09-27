package upc.pe.nayrabackend.general;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import upc.pe.nayrabackend.securities.TokenSesionJwt;
import upc.pe.nayrabackend.soporte.Soporte;
import upc.pe.nayrabackend.soporte.Soporte.Persona;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

/** Sesiones: regla aprobada de 5 minutos de inactividad (D-018) y JWT con jti (v4 §4.1; algoritmo y clave PROVISIONALES). */
class SesionesTest {

    private Soporte s;
    private Persona admin;
    private Persona usuario;

    @BeforeEach
    void preparar() throws Exception {
        s = new Soporte();
        admin = s.crearAdministrador();
        usuario = s.registrarUsuario(admin, Soporte.DNI_USUARIO);
    }

    @Test
    void laActividadMantieneLaSesionYCincoMinutosSinActividadLaCierran() throws Exception {
        String token = s.iniciarSesion(usuario);
        s.reloj.avanzar(Duration.ofMinutes(4));
        assertTrue(s.sesiones.validar(token).isPresent());
        s.reloj.avanzar(Duration.ofMinutes(4));
        assertTrue(s.sesiones.validar(token).isPresent(), "Cada petición reinicia la inactividad");
        s.reloj.avanzar(Duration.ofMinutes(5).plusSeconds(1));
        assertTrue(s.sesiones.validar(token).isEmpty());
        s.reloj.avanzar(Duration.ofSeconds(-301));
        assertTrue(s.sesiones.validar(token).isEmpty(), "Una sesión cerrada por inactividad no se reabre");
        assertTrue(s.auditoriaRepo.todos().stream().anyMatch(e -> e.accion().equals("SESION_CERRADA_POR_INACTIVIDAD")));
    }

    @Test
    void cerrarSesionInvalidaElToken() throws Exception {
        String token = s.iniciarSesion(usuario);
        s.sesiones.cerrar(s.sesiones.validar(token).orElseThrow().sesionId());
        assertTrue(s.sesiones.validar(token).isEmpty());
    }

    @Test
    void elJwtLlevaElJtiDeLaSesionYNoSeGuarda() throws Exception {
        String token = s.iniciarSesion(usuario);
        String jti = s.jwt.jti(token).orElseThrow();
        var sesion = s.sesionesRepo.porId(jti).orElseThrow();
        assertEquals(usuario.usuarioId(), sesion.getUsuarioId());
        assertEquals(usuario.dispositivoId(), sesion.getDispositivoId());
        assertEquals(jti, s.sesiones.validar(token).orElseThrow().sesionId());
        assertTrue(s.auditoriaRepo.todos().stream().noneMatch(e -> String.valueOf(e).contains(token)));
    }

    @Test
    void elJwtNoLlevaExpNiOtrosClaimsYSinFirmaValidaNoAutentica() throws Exception {
        String token = s.iniciarSesion(usuario);
        String[] partes = token.split("\\.");
        assertEquals(3, partes.length);
        String cabecera = new String(Base64.getUrlDecoder().decode(partes[0]), StandardCharsets.UTF_8);
        String cuerpo = new String(Base64.getUrlDecoder().decode(partes[1]), StandardCharsets.UTF_8);
        assertTrue(cabecera.contains("\"HS256\""), cabecera);
        assertTrue(cuerpo.matches("\\{\"jti\":\"[0-9a-f-]{36}\"}"), "Solo jti, sin exp ni refresh: " + cuerpo);
        // Firma alterada o algoritmo "none": no autentica.
        char otro = partes[2].charAt(0) == 'A' ? 'B' : 'A';
        String alterado = partes[0] + "." + partes[1] + "." + otro + partes[2].substring(1);
        assertTrue(s.sesiones.validar(alterado).isEmpty(), "firma alterada");
        String sinFirma = Base64.getUrlEncoder().withoutPadding().encodeToString("{\"alg\":\"none\"}".getBytes(StandardCharsets.UTF_8))
                + "." + partes[1] + ".";
        assertTrue(s.sesiones.validar(sinFirma).isEmpty(), "alg none");
        // Un JWT firmado con otra clave no autentica aunque el jti exista.
        var otraClave = new TokenSesionJwt(s.props, new java.security.SecureRandom());
        assertTrue(s.sesiones.validar(otraClave.emitir(s.jwt.jti(token).orElseThrow())).isEmpty(), "otra clave");
        assertTrue(s.sesiones.validar(token).isPresent(), "token original");
    }

    @Test
    void laInactividadDetectadaRevocaLaSesionEnLaTabla() throws Exception {
        String token = s.iniciarSesion(usuario);
        String jti = s.jwt.jti(token).orElseThrow();
        s.reloj.avanzar(Duration.ofMinutes(5).plusSeconds(1));
        assertTrue(s.sesiones.validar(token).isEmpty());
        assertNotNull(s.sesionesRepo.porId(jti).orElseThrow().getFechaRevocacion());
    }

    @Test
    void tokenInexistenteOVacioNoAutentica() {
        assertTrue(s.sesiones.validar(null).isEmpty());
        assertTrue(s.sesiones.validar("").isEmpty());
        assertTrue(s.sesiones.validar("inventado").isEmpty());
    }

    @Test
    void bloquearLaCuentaORevocarElDispositivoCierraLasSesiones() throws Exception {
        String t1 = s.iniciarSesion(usuario);
        s.admin.bloquearUsuario(admin.usuarioId(), usuario.usuarioId());
        assertTrue(s.sesiones.validar(t1).isEmpty());
        s.admin.desbloquearUsuario(admin.usuarioId(), usuario.usuarioId());
        assertTrue(s.sesiones.validar(t1).isEmpty(), "Desbloquear no revive sesiones revocadas");

        String t2 = s.iniciarSesion(usuario);
        s.admin.revocarDispositivo(admin.usuarioId(), usuario.usuarioId());
        assertTrue(s.sesiones.validar(t2).isEmpty());
    }
}
