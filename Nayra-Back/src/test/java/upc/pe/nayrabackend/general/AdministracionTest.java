package upc.pe.nayrabackend.general;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import upc.pe.nayrabackend.dtos.AdministracionDTOs.EventoAuditoriaDTO;
import upc.pe.nayrabackend.entities.Usuario;
import upc.pe.nayrabackend.excepciones.NayraException;
import upc.pe.nayrabackend.soporte.Soporte;
import upc.pe.nayrabackend.soporte.Soporte.Persona;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Funciones del administrador (D-041): consultas, bloqueo, dispositivo y auditoría, todo auditado. */
class AdministracionTest {

    private Soporte s;
    private Persona admin;
    private Persona usuario;

    @BeforeEach
    void preparar() throws Exception {
        s = new Soporte();
        admin = s.crearAdministrador();
        usuario = s.registrarUsuario(admin, Soporte.DNI_USUARIO);
        s.registrarUsuario(admin, Soporte.DNI_USUARIO_2);
    }

    @Test
    void listaYBuscaUsuarios() {
        assertEquals(3, s.admin.buscarUsuarios(admin.usuarioId(), null, null, null).size());
        assertEquals(List.of(usuario.usuarioId()),
                s.admin.buscarUsuarios(admin.usuarioId(), "DNI", Soporte.DNI_USUARIO, null).stream().map(u -> u.id()).toList());
        String nombres = s.usuarios.obtener(usuario.usuarioId()).getNombres();
        assertTrue(s.admin.buscarUsuarios(admin.usuarioId(), null, null, nombres.toUpperCase()).stream()
                .anyMatch(u -> u.id().equals(usuario.usuarioId())));
    }

    @Test
    void detalleNoExponeElHashDelPin() {
        var d = s.admin.detalleUsuario(admin.usuarioId(), usuario.usuarioId());
        assertEquals("ACTIVO", d.estado());
        assertEquals("DNI", d.tipoDocumentoIdentidad());
        assertEquals(Soporte.DNI_USUARIO, d.numeroDocumento());
        assertEquals(usuario.dispositivoId(), d.dispositivoActivo().id());
        String hash = s.credencialesRepo.deUsuario(usuario.usuarioId()).orElseThrow().getPinHash();
        assertFalse(String.valueOf(d).contains(hash));
        assertEquals("USUARIO_NO_ENCONTRADO",
                assertThrows(NayraException.class, () -> s.admin.detalleUsuario(admin.usuarioId(), "x")).getCodigo());
    }

    @Test
    void bloqueaYDesbloquea() {
        s.admin.bloquearUsuario(admin.usuarioId(), usuario.usuarioId());
        assertEquals(Usuario.Estado.BLOQUEADO, s.usuarios.obtener(usuario.usuarioId()).getEstado());
        assertEquals("CUENTA_YA_BLOQUEADA", assertThrows(NayraException.class,
                () -> s.admin.bloquearUsuario(admin.usuarioId(), usuario.usuarioId())).getCodigo());
        s.admin.desbloquearUsuario(admin.usuarioId(), usuario.usuarioId());
        assertEquals(Usuario.Estado.ACTIVO, s.usuarios.obtener(usuario.usuarioId()).getEstado());
        assertEquals("CUENTA_NO_BLOQUEADA", assertThrows(NayraException.class,
                () -> s.admin.desbloquearUsuario(admin.usuarioId(), usuario.usuarioId())).getCodigo());
    }

    @Test
    void elAdministradorNoActuaSobreSiMismo() {
        assertEquals("ACCION_SOBRE_SI_MISMO", assertThrows(NayraException.class,
                () -> s.admin.bloquearUsuario(admin.usuarioId(), admin.usuarioId())).getCodigo());
        assertEquals("ACCION_SOBRE_SI_MISMO", assertThrows(NayraException.class,
                () -> s.admin.revocarDispositivo(admin.usuarioId(), admin.usuarioId())).getCodigo());
    }

    @Test
    void revocaElDispositivo() {
        assertTrue(s.admin.dispositivoActivo(admin.usuarioId(), usuario.usuarioId()).isPresent());
        s.admin.revocarDispositivo(admin.usuarioId(), usuario.usuarioId());
        assertTrue(s.admin.dispositivoActivo(admin.usuarioId(), usuario.usuarioId()).isEmpty());
        assertEquals("SIN_DISPOSITIVO_ACTIVO", assertThrows(NayraException.class,
                () -> s.admin.revocarDispositivo(admin.usuarioId(), usuario.usuarioId())).getCodigo());
    }

    @Test
    void lasAccionesDelAdministradorQuedanAuditadasConActorYUsuarioAfectado() {
        s.admin.bloquearUsuario(admin.usuarioId(), usuario.usuarioId());
        s.admin.detalleUsuario(admin.usuarioId(), usuario.usuarioId());
        List<EventoAuditoriaDTO> acciones = s.admin.accionesAdministrativas(admin.usuarioId());
        assertTrue(acciones.stream().allMatch(e -> e.accion().startsWith("ADMIN_")));
        assertTrue(acciones.stream().anyMatch(e -> e.accion().equals("ADMIN_BLOQUEO_CUENTA")
                && admin.usuarioId().equals(e.actorId()) && usuario.usuarioId().equals(e.usuarioAfectadoId())));
        assertTrue(acciones.stream().anyMatch(e -> e.accion().equals("ADMIN_CONSULTA_USUARIO")));
    }

    @Test
    void consultaDeIntentosFallidos() throws Exception {
        String t = s.abrirTransaccion(usuario);
        s.autenticacion.verificarPin(t, "000000");
        List<EventoAuditoriaDTO> fallos = s.admin.intentosFallidos(admin.usuarioId(), usuario.usuarioId());
        assertEquals(1, fallos.size());
        assertEquals("AUTENTICACION_PIN_FALLIDO", fallos.getFirst().accion());
        assertEquals("PIN_INCORRECTO", fallos.getFirst().motivo());
        assertTrue(s.admin.auditoria(admin.usuarioId(), usuario.usuarioId()).stream()
                .allMatch(e -> usuario.usuarioId().equals(e.usuarioAfectadoId())));
    }

    @Test
    void laBusquedaNoGuardaElDocumentoEnLaAuditoria() {
        s.admin.buscarUsuarios(admin.usuarioId(), "DNI", Soporte.DNI_USUARIO, null);
        assertTrue(s.auditoriaRepo.todos().stream().noneMatch(e -> String.valueOf(e).contains(Soporte.DNI_USUARIO)));
    }
}
