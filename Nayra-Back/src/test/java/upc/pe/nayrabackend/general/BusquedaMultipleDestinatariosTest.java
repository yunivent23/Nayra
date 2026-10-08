package upc.pe.nayrabackend.general;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import upc.pe.nayrabackend.dtos.DestinatarioDTOs.DestinatarioDisponible;
import upc.pe.nayrabackend.entities.Cuentas;
import upc.pe.nayrabackend.entities.Usuario;
import upc.pe.nayrabackend.excepciones.NayraException;
import upc.pe.nayrabackend.serviceinterfaces.IOperacionesService;
import upc.pe.nayrabackend.soporte.Soporte;
import upc.pe.nayrabackend.soporte.Soporte.Persona;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Búsqueda múltiple de destinatarios a partir de la agenda del teléfono (D-042, HU-69; 2026-10-08): solo devuelve los
 * celulares de usuarios y cuentas activos, sin el propio usuario, sin repetidos y con un máximo de 500 por consulta.
 * Las pruebas HTTP (sesión obligatoria, 400 al pasar el máximo) están en {@link ApiHttpTest}.
 */
class BusquedaMultipleDestinatariosTest {

    private Soporte s;
    private Persona admin;
    private Persona usuario;
    private Persona otro;
    private Persona tercero;

    @BeforeEach
    void preparar() throws Exception {
        s = new Soporte();
        admin = s.crearAdministrador();
        usuario = s.registrarUsuario(admin, Soporte.DNI_USUARIO);
        otro = s.registrarUsuario(admin, Soporte.DNI_USUARIO_2);
        tercero = s.registrarUsuario(admin, "10000004");
    }

    private String celularDe(Persona p) {
        return s.usuariosRepo.porId(p.usuarioId()).orElseThrow().getCelular();
    }

    /** Celulares válidos que no pertenecen a nadie (los usuarios de prueba empiezan por 91). */
    private static List<String> desconocidos(int cantidad) {
        return IntStream.range(0, cantidad).mapToObj(i -> String.format("95%07d", i)).toList();
    }

    private List<DestinatarioDisponible> buscar(List<String> celulares) {
        return s.operaciones.buscarDestinatarios(usuario.usuarioId(), celulares).destinatarios();
    }

    private static String codigo(NayraException e) {
        return e.getCodigo();
    }

    @Test
    void cienNumerosYUnoRegistradoDevuelveSoloEse() {
        List<String> agenda = new ArrayList<>(desconocidos(99));
        agenda.add(50, celularDe(otro));
        assertEquals(100, agenda.size());
        List<DestinatarioDisponible> r = buscar(agenda);
        assertEquals(1, r.size());
        assertEquals(celularDe(otro), r.get(0).celular());
        assertEquals("Persona Fict...", r.get(0).nombreVisible());
    }

    @Test
    void cienNumerosSinRegistradosDevuelveListaVacia() {
        assertEquals(List.of(), buscar(desconocidos(100)));
    }

    @Test
    void cienNumerosConVariosRegistradosDevuelveSoloLosRegistrados() {
        List<String> agenda = new ArrayList<>(desconocidos(97));
        agenda.add(10, celularDe(otro));
        agenda.add(60, celularDe(tercero));
        agenda.add(90, celularDe(admin)); // el administrador inicial no tiene cuenta financiera
        List<DestinatarioDisponible> r = buscar(agenda);
        assertEquals(List.of(celularDe(otro), celularDe(tercero)), r.stream().map(DestinatarioDisponible::celular).toList());
    }

    @Test
    void usuarioInactivoOBloqueadoNoAparece() {
        Usuario inactivo = s.usuariosRepo.porId(otro.usuarioId()).orElseThrow();
        ReflectionTestUtils.setField(inactivo, "estado", Usuario.Estado.INACTIVO);
        s.usuariosRepo.porId(tercero.usuarioId()).orElseThrow().bloquear(s.reloj.instant());
        assertEquals(List.of(), buscar(List.of(celularDe(otro), celularDe(tercero))));
    }

    @Test
    void cuentaNoActivaNoAparece() {
        Cuentas cuenta = s.entorno.porPropietario(otro.usuarioId()).orElseThrow();
        ReflectionTestUtils.setField(cuenta, "estado", Cuentas.Estado.BLOQUEADA);
        Cuentas cerrada = s.entorno.porPropietario(tercero.usuarioId()).orElseThrow();
        ReflectionTestUtils.setField(cerrada, "estado", Cuentas.Estado.CERRADA);
        assertEquals(List.of(), buscar(List.of(celularDe(otro), celularDe(tercero))));
    }

    @Test
    void elNumeroPropioNoApareceComoDestinatario() {
        assertEquals(List.of(), buscar(List.of(celularDe(usuario))));
        List<DestinatarioDisponible> r = buscar(List.of(celularDe(usuario), celularDe(otro)));
        assertEquals(List.of(celularDe(otro)), r.stream().map(DestinatarioDisponible::celular).toList());
    }

    @Test
    void formatosEquivalentesDanUnaSolaCoincidenciaCanonica() {
        String n = celularDe(otro);
        String espaciado = n.substring(0, 3) + " " + n.substring(3, 6) + " " + n.substring(6);
        List<DestinatarioDisponible> r = buscar(List.of(n, "+51" + n, "+51 " + espaciado, espaciado, " " + n + " "));
        assertEquals(1, r.size());
        assertEquals(n, r.get(0).celular());
    }

    @Test
    void numerosRepetidosNoRepitenAlDestinatario() {
        String n = celularDe(otro);
        List<DestinatarioDisponible> r = buscar(List.of(n, n, celularDe(tercero), n, celularDe(tercero)));
        assertEquals(List.of(n, celularDe(tercero)), r.stream().map(DestinatarioDisponible::celular).toList());
    }

    @Test
    void exactamenteElMaximoEsValido() {
        assertEquals(500, IOperacionesService.MAX_CELULARES_POR_CONSULTA);
        List<String> agenda = new ArrayList<>(desconocidos(499));
        agenda.add(celularDe(otro));
        assertEquals(500, agenda.size());
        assertEquals(1, buscar(agenda).size());
    }

    @Test
    void masDelMaximoSeRechazaConDemasiadosCelularesSinNombrarNumeros() {
        List<String> agenda = new ArrayList<>(desconocidos(IOperacionesService.MAX_CELULARES_POR_CONSULTA));
        agenda.add(celularDe(otro));
        NayraException e = assertThrows(NayraException.class, () -> buscar(agenda));
        assertEquals("DEMASIADOS_CELULARES", codigo(e));
        assertEquals(400, e.getEstado().value());
        assertFalse(String.valueOf(e.getMessage()).contains(celularDe(otro)));
        // El tamaño se revisa antes de normalizar: un elemento inválido no cambia el error.
        List<String> conInvalido = new ArrayList<>(agenda);
        conInvalido.set(0, "abc");
        assertEquals("DEMASIADOS_CELULARES", codigo(assertThrows(NayraException.class, () -> buscar(conInvalido))));
    }

    @Test
    void listaVaciaDevuelveListaVaciaYSinListaEsInvalida() {
        assertEquals(List.of(), buscar(List.of()));
        NayraException e = assertThrows(NayraException.class, () -> buscar(null));
        assertEquals("CELULARES_REQUERIDOS", codigo(e));
        assertEquals(400, e.getEstado().value());
    }

    @Test
    void losFormatosHabitualesDeAgendaSeNormalizanAlMismoCelular() {
        String n = celularDe(otro);
        String sep = n.substring(0, 3) + "-" + n.substring(3, 6) + "-" + n.substring(6);
        String esp = n.substring(0, 3) + " " + n.substring(3, 6) + " " + n.substring(6);
        for (String formato : List.of(n, sep, esp, "+51 " + esp, "+51" + n, "+51-" + sep)) {
            List<DestinatarioDisponible> r = buscar(List.of(formato));
            assertEquals(1, r.size(), formato);
            assertEquals(n, r.get(0).celular(), formato);
        }
        // Los cinco formatos de la agenda juntos son una sola coincidencia.
        assertEquals(1, buscar(List.of(n, sep, esp, "+51 " + esp, "+51" + n)).size());
    }

    @Test
    void unContactoNoUtilizableNoHaceFallarLaSolicitud() {
        String n = celularDe(otro);
        String sep = n.substring(0, 3) + "-" + n.substring(3, 6) + "-" + n.substring(6);
        List<String> agenda = java.util.Arrays.asList(
                "12345", n, "887654321", "+52987654321", "abc", "", null, "01 234 5678", sep, "9-9-9", celularDe(tercero));
        List<DestinatarioDisponible> r = buscar(agenda);
        assertEquals(List.of(n, celularDe(tercero)), r.stream().map(DestinatarioDisponible::celular).toList(),
                "Se conservan los válidos, sin duplicados y en el orden de la agenda");
    }

    @Test
    void siNingunNumeroEsUtilizableDevuelveListaVaciaSinError() {
        assertEquals(List.of(), buscar(java.util.Arrays.asList("12345", "abc", "", null, "+52987654321", "9999-99", "01 234 5678")));
    }

    @Test
    void elGuionNoVuelveValidaCualquierCadena() {
        // Solo se quita el guion; el resultado debe cumplir exactamente la validación de Celular.
        assertEquals(List.of(), buscar(List.of("999-88-87", "99a-888-777", "888-888-888", "+52-999-888-777")));
        assertThrows(NayraException.class, () -> upc.pe.nayrabackend.entities.Celular.leer("999-888-777"));
    }

    @Test
    void elLimiteSeAplicaAElementosRecibidosAntesDeFiltrar() {
        List<String> todosInvalidos = java.util.Collections.nCopies(IOperacionesService.MAX_CELULARES_POR_CONSULTA, "abc");
        assertEquals(List.of(), buscar(todosInvalidos));
        List<String> excedido = java.util.Collections.nCopies(IOperacionesService.MAX_CELULARES_POR_CONSULTA + 1, "abc");
        assertEquals("DEMASIADOS_CELULARES", codigo(assertThrows(NayraException.class, () -> buscar(excedido))));
    }

    @Test
    void laRespuestaSoloTieneCelularYNombreVisible() {
        assertEquals(List.of("celular", "nombreVisible"),
                java.util.Arrays.stream(DestinatarioDisponible.class.getRecordComponents()).map(c -> c.getName()).toList());
    }

    @Test
    void laBusquedaIndividualSigueIgual() {
        assertEquals("Persona Fict...", s.operaciones.buscarDestinatario(usuario.usuarioId(), "+51" + celularDe(otro)).nombreVisible());
        assertEquals("CUENTAS_IGUALES", codigo(assertThrows(NayraException.class,
                () -> s.operaciones.buscarDestinatario(usuario.usuarioId(), celularDe(usuario)))));
    }
}
