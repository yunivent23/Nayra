package upc.pe.nayrabackend.general;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import upc.pe.nayrabackend.soporte.Soporte;
import upc.pe.nayrabackend.soporte.Soporte.Persona;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

/** Sesiones: regla aprobada de 5 minutos de inactividad (D-018); mecanismo del token PROVISIONAL. */
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
    void soloSeGuardaElHashDelToken() throws Exception {
        String token = s.iniciarSesion(usuario);
        assertTrue(s.sesionesRepo.deUsuario(usuario.usuarioId()).stream().noneMatch(x -> x.getTokenHash().equals(token)));
        assertTrue(s.auditoriaRepo.todos().stream().noneMatch(e -> String.valueOf(e).contains(token)));
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
