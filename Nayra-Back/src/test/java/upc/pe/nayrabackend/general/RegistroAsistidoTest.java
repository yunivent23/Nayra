package upc.pe.nayrabackend.general;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import upc.pe.nayrabackend.dtos.RegistroDTOs.RegistroIniciado;
import upc.pe.nayrabackend.dtos.RegistroDTOs.SolicitudDatosRegistro;
import upc.pe.nayrabackend.entities.RegistroIdentidadSimulado;
import upc.pe.nayrabackend.entities.Rol;
import upc.pe.nayrabackend.entities.Usuario;
import upc.pe.nayrabackend.excepciones.NayraException;
import upc.pe.nayrabackend.soporte.Soporte;
import upc.pe.nayrabackend.soporte.Soporte.Persona;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

/** Registro inicial asistido (D-052, D-053) con el registro de identidad y las cuentas simuladas (D-035, D-028). */
class RegistroAsistidoTest {

    private Soporte s;
    private Persona admin;

    @BeforeEach
    void preparar() throws Exception {
        s = new Soporte();
        admin = s.crearAdministrador();
    }

    private String codigo(NayraException e) {
        return e.getCodigo();
    }

    @Test
    void registroCompletoCreaUsuarioConRolUserDispositivoYCuentaFinanciera() throws Exception {
        Persona p = s.registrarUsuario(admin, Soporte.DNI_USUARIO);
        Usuario u = s.usuarios.obtener(p.usuarioId());
        assertEquals(Rol.USER, u.getRol());
        assertEquals(Usuario.Estado.ACTIVA, u.getEstado());
        assertEquals("Persona", u.getNombres().split(" ")[0]);
        assertEquals(p.dispositivoId(), s.dispositivosRepo.activoDeUsuario(p.usuarioId()).orElseThrow().getId());
        assertEquals(p.usuarioId(), s.entorno.porTitularDni(Soporte.DNI_USUARIO).orElseThrow().getPropietarioUsuarioId());
        assertTrue(s.auditoriaRepo.todos().stream().anyMatch(e -> e.accion().equals("REGISTRO_VALIDACION_IDENTIDAD_ASISTIDA")
                && admin.usuarioId().equals(e.actorId())));
    }

    @Test
    void pinSoloSeGuardaComoHash() throws Exception {
        Persona p = s.registrarUsuario(admin, Soporte.DNI_USUARIO);
        String hash = s.usuarios.obtener(p.usuarioId()).getPinHash();
        assertNotEquals(Soporte.PIN, hash);
        assertFalse(hash.contains(Soporte.PIN));
        assertTrue(s.pines.coincide(Soporte.PIN, hash));
    }

    @Test
    void dniYaRegistradoSeRechazaYSeAudita() throws Exception {
        s.registrarUsuario(admin, Soporte.DNI_USUARIO);
        assertEquals("DNI_REGISTRADO", codigo(assertThrows(NayraException.class,
                () -> s.registro.iniciar(admin.usuarioId(), Soporte.DNI_USUARIO))));
        assertTrue(s.auditoriaRepo.todos().stream().anyMatch(e -> "DNI_REGISTRADO".equals(e.motivo())));
    }

    @Test
    void dniInvalidoInexistenteOSinCuentaFinanciera() {
        assertEquals("DNI_INVALIDO", codigo(assertThrows(NayraException.class, () -> s.registro.iniciar(admin.usuarioId(), "123"))));
        assertEquals("DNI_NO_ENCONTRADO", codigo(assertThrows(NayraException.class,
                () -> s.registro.iniciar(admin.usuarioId(), "99999999"))));
        s.entorno.guardar(new RegistroIdentidadSimulado("10000009", "Sin", "Cuenta"));
        assertEquals("CUENTA_FINANCIERA_NO_ENCONTRADA", codigo(assertThrows(NayraException.class,
                () -> s.registro.iniciar(admin.usuarioId(), "10000009"))));
    }

    @Test
    void sinValidacionDeIdentidadNoSeContinua() throws Exception {
        String c = s.registro.iniciar(admin.usuarioId(), Soporte.DNI_USUARIO).codigoRegistro();
        assertEquals("IDENTIDAD_NO_VALIDADA", codigo(assertThrows(NayraException.class, () -> s.registro.datosParaConfirmar(c))));
        assertEquals("IDENTIDAD_NO_VALIDADA", codigo(assertThrows(NayraException.class, () -> s.registro.completarDatos(c,
                new SolicitudDatosRegistro(true, Soporte.CELULAR, Soporte.PIN, Soporte.publica(Soporte.claveP256()))))));
    }

    @Test
    void soloElMismoRepresentanteValidaLaIdentidad() throws Exception {
        String c = s.registro.iniciar(admin.usuarioId(), Soporte.DNI_USUARIO).codigoRegistro();
        assertEquals("PASO_NO_VALIDO", codigo(assertThrows(NayraException.class, () -> s.registro.validarIdentidad("otro", c))));
    }

    @Test
    void siLaPersonaNoConfirmaSusDatosSeCancela() throws Exception {
        String c = s.registro.iniciar(admin.usuarioId(), Soporte.DNI_USUARIO).codigoRegistro();
        s.registro.validarIdentidad(admin.usuarioId(), c);
        assertEquals("CANCELADO", s.registro.completarDatos(c, new SolicitudDatosRegistro(false, null, null, null)).paso());
        assertEquals("REGISTRO_NO_VALIDO", codigo(assertThrows(NayraException.class, () -> s.registro.datosParaConfirmar(c))));
    }

    @Test
    void datosInvalidosSeRechazan() throws Exception {
        String c = s.registro.iniciar(admin.usuarioId(), Soporte.DNI_USUARIO).codigoRegistro();
        s.registro.validarIdentidad(admin.usuarioId(), c);
        String clave = Soporte.publica(Soporte.claveP256());
        assertEquals("CELULAR_INVALIDO", codigo(assertThrows(NayraException.class,
                () -> s.registro.completarDatos(c, new SolicitudDatosRegistro(true, "abc", Soporte.PIN, clave)))));
        assertEquals("PIN_INVALIDO", codigo(assertThrows(NayraException.class,
                () -> s.registro.completarDatos(c, new SolicitudDatosRegistro(true, Soporte.CELULAR, "12345", clave)))));
        assertEquals("CLAVE_DISPOSITIVO_INVALIDA", codigo(assertThrows(NayraException.class,
                () -> s.registro.completarDatos(c, new SolicitudDatosRegistro(true, Soporte.CELULAR, Soporte.PIN, "x")))));
    }

    @Test
    void noSeFinalizaSinVozEnrolada() throws Exception {
        String c = s.registro.iniciar(admin.usuarioId(), Soporte.DNI_USUARIO).codigoRegistro();
        s.registro.validarIdentidad(admin.usuarioId(), c);
        s.registro.completarDatos(c, new SolicitudDatosRegistro(true, Soporte.CELULAR, Soporte.PIN,
                Soporte.publica(Soporte.claveP256())));
        assertEquals("VOZ_NO_ENROLADA", codigo(assertThrows(NayraException.class, () -> s.registro.finalizar(c))));
        assertTrue(s.usuariosRepo.porDni(Soporte.DNI_USUARIO).isEmpty());
    }

    @Test
    void elCodigoDeRegistroVence() throws Exception {
        String c = s.registro.iniciar(admin.usuarioId(), Soporte.DNI_USUARIO).codigoRegistro();
        s.registro.validarIdentidad(admin.usuarioId(), c);
        s.reloj.avanzar(Duration.ofSeconds(901));
        assertEquals("REGISTRO_NO_VALIDO", codigo(assertThrows(NayraException.class, () -> s.registro.datosParaConfirmar(c))));
    }

    @Test
    void elCodigoEsDeUnSoloUso() throws Exception {
        String c = s.registro.iniciar(admin.usuarioId(), Soporte.DNI_USUARIO).codigoRegistro();
        s.registro.validarIdentidad(admin.usuarioId(), c);
        s.completarRegistro(c, Soporte.PIN);
        assertEquals("REGISTRO_NO_VALIDO", codigo(assertThrows(NayraException.class, () -> s.registro.finalizar(c))));
    }

    @Test
    void elEnrolamientoNoSeRepiteNiSeAdelanta() throws Exception {
        String c = s.registro.iniciar(admin.usuarioId(), Soporte.DNI_USUARIO).codigoRegistro();
        s.registro.validarIdentidad(admin.usuarioId(), c);
        assertEquals("REGISTRO_NO_LISTO_PARA_ENROLAR",
                codigo(assertThrows(NayraException.class, () -> s.enrolamiento.emitirDesafio(c))));
    }

    @Test
    void elAdministradorInicialSoloSeCreaUnaVez() {
        assertEquals("ADMINISTRADOR_YA_EXISTE", codigo(assertThrows(NayraException.class,
                () -> s.registro.iniciarAdministradorInicial(Soporte.DNI_USUARIO_2))));
        assertEquals(Rol.ADMIN, s.usuarios.obtener(admin.usuarioId()).getRol());
    }

    @Test
    void elRegistroNoGuardaDatosSensiblesEnLaAuditoria() throws Exception {
        s.registrarUsuario(admin, Soporte.DNI_USUARIO);
        assertTrue(s.auditoriaRepo.todos().stream().noneMatch(e -> {
            String t = String.valueOf(e);
            return t.contains(Soporte.PIN) || t.contains(Soporte.DNI_USUARIO) || t.contains(Soporte.CELULAR);
        }));
    }

    @Test
    void unNuevoRegistroDelMismoDniInvalidaElAnterior() throws Exception {
        RegistroIniciado r1 = s.registro.iniciar(admin.usuarioId(), Soporte.DNI_USUARIO);
        s.registro.iniciar(admin.usuarioId(), Soporte.DNI_USUARIO);
        assertEquals("REGISTRO_NO_VALIDO", codigo(assertThrows(NayraException.class,
                () -> s.registro.validarIdentidad(admin.usuarioId(), r1.codigoRegistro()))));
    }
}
