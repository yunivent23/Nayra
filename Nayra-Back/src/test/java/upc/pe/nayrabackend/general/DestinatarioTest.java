package upc.pe.nayrabackend.general;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import upc.pe.nayrabackend.dtos.DestinatarioDTOs.DestinatarioEncontrado;
import upc.pe.nayrabackend.dtos.RegistroDTOs.SolicitudDatosRegistro;
import upc.pe.nayrabackend.entities.Celular;
import upc.pe.nayrabackend.excepciones.NayraException;
import upc.pe.nayrabackend.serviceimplements.OperacionesServiceImplement;
import upc.pe.nayrabackend.soporte.Soporte;
import upc.pe.nayrabackend.soporte.Soporte.Persona;

import java.lang.reflect.RecordComponent;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Destinatario de una transferencia por celular (G-1, modificada el 2026-09-28): formato de Perú, normalización de
 * "+51", celular único en el registro y búsqueda que solo devuelve el nombre parcial.
 */
class DestinatarioTest {

    private Soporte s;
    private Persona admin;

    @BeforeEach
    void preparar() throws Exception {
        s = new Soporte();
        admin = s.crearAdministrador();
    }

    private static String codigo(NayraException e) {
        return e.getCodigo();
    }

    private String celularDe(Persona p) {
        return s.usuariosRepo.porId(p.usuarioId()).orElseThrow().getCelular();
    }

    @Test
    void formatoPeruanoYNormalizacionDelPrefijo() {
        assertEquals("987654321", Celular.leer("987654321").numero());
        assertEquals("987654321", Celular.leer("+51987654321").numero());
        assertEquals("987654321", Celular.leer(" 987 654 321 ").numero());
        for (String invalido : List.of("", "abc", "87654321", "887654321", "9876543210", "51987654321", "+52987654321",
                "+51 87654321", "98765432a")) {
            assertEquals("CELULAR_INVALIDO", codigo(assertThrows(NayraException.class, () -> Celular.leer(invalido))), invalido);
        }
        assertEquals("CELULAR_INVALIDO", codigo(assertThrows(NayraException.class, () -> Celular.leer(null))));
    }

    @Test
    void elRegistroGuardaElFormatoCanonico() throws Exception {
        String codigo = s.registro.iniciar(admin.usuarioId(), "DNI", Soporte.DNI_USUARIO).codigoRegistro();
        s.registro.validarIdentidad(admin.usuarioId(), codigo);
        Persona p = s.completarRegistro(codigo, Soporte.PIN, "+51912345678");
        assertEquals("912345678", celularDe(p));
    }

    @Test
    void elRegistroRechazaUnCelularYaAsociadoAOtroUsuario() throws Exception {
        String celularAdmin = celularDe(admin);
        String codigo = s.registro.iniciar(admin.usuarioId(), "DNI", Soporte.DNI_USUARIO).codigoRegistro();
        s.registro.validarIdentidad(admin.usuarioId(), codigo);
        String clave = Soporte.publica(Soporte.claveP256());
        // Variante equivalente con +51: es el mismo número.
        for (String variante : List.of(celularAdmin, "+51" + celularAdmin)) {
            assertEquals("CELULAR_REGISTRADO", codigo(assertThrows(NayraException.class,
                    () -> s.registro.completarDatos(codigo, new SolicitudDatosRegistro(true, variante, Soporte.PIN, clave)))));
        }
        // Con otro número el mismo registro continúa.
        s.completarRegistro(codigo, Soporte.PIN, Soporte.celularNuevo());
    }

    /** Enrolamiento de voz aceptado (3 muestras) de un registro con los datos ya completos. */
    private void enrolar(String codigo) throws Exception {
        for (int i = 1; i <= 3; i++) {
            var d = s.enrolamiento.emitirDesafio(codigo);
            s.voz.respuestas.add(new upc.pe.nayrabackend.serviceinterfaces.IServicioVozCliente.ResultadoTecnico(true, null, List.of(), i));
            assertTrue(s.enrolamiento.enviarMuestra(codigo, d.desafioId(), Soporte.AUDIO).aceptada());
        }
        s.voz.respuestas.add(new upc.pe.nayrabackend.serviceinterfaces.IServicioVozCliente.ResultadoEnrolamiento(true, null, 3));
        assertTrue(s.enrolamiento.finalizar(codigo).correcto());
    }

    @Test
    void dosRegistrosEnCursoConElMismoCelularSoloTerminaUno() throws Exception {
        String celular = Soporte.celularNuevo();
        String c1 = s.registro.iniciar(admin.usuarioId(), "DNI", Soporte.DNI_USUARIO).codigoRegistro();
        s.registro.validarIdentidad(admin.usuarioId(), c1);
        String c2 = s.registro.iniciar(admin.usuarioId(), "DNI", Soporte.DNI_USUARIO_2).codigoRegistro();
        s.registro.validarIdentidad(admin.usuarioId(), c2);
        // Ambos pasan el paso de datos porque ninguno terminó todavía.
        s.registro.completarDatos(c1, new SolicitudDatosRegistro(true, celular, Soporte.PIN, Soporte.publica(Soporte.claveP256())));
        s.registro.completarDatos(c2, new SolicitudDatosRegistro(true, "+51" + celular, Soporte.PIN, Soporte.publica(Soporte.claveP256())));
        enrolar(c1);
        enrolar(c2);
        s.registro.finalizar(c1);
        assertEquals("CELULAR_REGISTRADO", codigo(assertThrows(NayraException.class, () -> s.registro.finalizar(c2))));
        assertTrue(s.usuariosRepo.porDocumento(upc.pe.nayrabackend.entities.TipoDocumentoIdentidad.DNI, Soporte.DNI_USUARIO_2).isEmpty());
    }

    @Test
    void buscaAlDestinatarioYSoloDevuelveElNombreParcial() throws Exception {
        Persona usuario = s.registrarUsuario(admin, Soporte.DNI_USUARIO);
        Persona otro = s.registrarUsuario(admin, Soporte.DNI_USUARIO_2);
        DestinatarioEncontrado d = s.operaciones.buscarDestinatario(usuario.usuarioId(), "+51" + celularDe(otro));
        assertEquals("Persona Fict...", d.nombreVisible());
        // La respuesta no tiene identificadores internos, documento, celular ni estado.
        assertEquals(List.of("nombreVisible"),
                Arrays.stream(DestinatarioEncontrado.class.getRecordComponents()).map(RecordComponent::getName).toList());
    }

    @Test
    void numeroNoRegistradoInvalidoOPropio() throws Exception {
        Persona usuario = s.registrarUsuario(admin, Soporte.DNI_USUARIO);
        assertEquals("DESTINATARIO_NO_ENCONTRADO", codigo(assertThrows(NayraException.class,
                () -> s.operaciones.buscarDestinatario(usuario.usuarioId(), "999999999"))));
        assertEquals("CELULAR_INVALIDO", codigo(assertThrows(NayraException.class,
                () -> s.operaciones.buscarDestinatario(usuario.usuarioId(), "12345"))));
        assertEquals("CUENTAS_IGUALES", codigo(assertThrows(NayraException.class,
                () -> s.operaciones.buscarDestinatario(usuario.usuarioId(), celularDe(usuario)))));
    }

    @Test
    void unaCuentaQueNoPuedeRecibirRespondeComoNoEncontrada() throws Exception {
        Persona usuario = s.registrarUsuario(admin, Soporte.DNI_USUARIO);
        // El administrador inicial no tiene cuenta financiera (DNI 10000001).
        NayraException sinCuenta = assertThrows(NayraException.class,
                () -> s.operaciones.buscarDestinatario(usuario.usuarioId(), celularDe(admin)));
        assertEquals("DESTINATARIO_NO_ENCONTRADO", sinCuenta.getCodigo());
        assertEquals(404, sinCuenta.getEstado().value());
        // Una cuenta de acceso bloqueada tampoco.
        Persona otro = s.registrarUsuario(admin, Soporte.DNI_USUARIO_2);
        s.admin.bloquearUsuario(admin.usuarioId(), otro.usuarioId());
        assertEquals("DESTINATARIO_NO_ENCONTRADO", codigo(assertThrows(NayraException.class,
                () -> s.operaciones.buscarDestinatario(usuario.usuarioId(), celularDe(otro)))));
    }

    @Test
    void apellidoSimpleCortoSeMuestraEnteroYLargoSeAcortaACuatroLetras() {
        assertEquals("María Pérez", OperacionesServiceImplement.nombreVisible("María", "Pérez"));
        assertEquals("María Sala...", OperacionesServiceImplement.nombreVisible("María", "Salazar"));
        assertEquals("María Pere...", OperacionesServiceImplement.nombreVisible("María Elena", "Pereira Soto"));
        assertEquals("Ángel Ñañez", OperacionesServiceImplement.nombreVisible(" Ángel ", "Ñañez"));
        assertEquals("Ángel Ñañe...", OperacionesServiceImplement.nombreVisible("Ángel", "Ñañezco"));
        assertEquals("Ana", OperacionesServiceImplement.nombreVisible("Ana", " "));
    }

    @Test
    void apellidoCortoSeMuestraEntero() {
        assertEquals("Ana Paz", OperacionesServiceImplement.nombreVisible("Ana", "Paz Ruiz"));
        assertEquals("Luis Río", OperacionesServiceImplement.nombreVisible("Luis", "Río"));
        assertEquals("Luis De Paz", OperacionesServiceImplement.nombreVisible("Luis", "De Paz"));
    }

    @Test
    void apellidoCompuestoConservaLasParticulasCompletas() {
        assertEquals("María De la...", OperacionesServiceImplement.nombreVisible("María", "De la Cruz"));
        assertEquals("Juan Del R...", OperacionesServiceImplement.nombreVisible("Juan", "Del Río"));
        assertEquals("Ana De Los...", OperacionesServiceImplement.nombreVisible("Ana", "De Los Santos"));
        assertEquals("Rosa de las...", OperacionesServiceImplement.nombreVisible("Rosa", "de las Casas Vega"));
        assertEquals("Rosa La To...", OperacionesServiceImplement.nombreVisible("Rosa", "La Torre"));
        assertEquals("José Delg...", OperacionesServiceImplement.nombreVisible("José", "Delgado"));
    }
}
