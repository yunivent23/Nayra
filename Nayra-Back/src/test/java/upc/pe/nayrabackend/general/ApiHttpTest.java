package upc.pe.nayrabackend.general;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import upc.pe.nayrabackend.serviceinterfaces.IServicioVozCliente;
import upc.pe.nayrabackend.soporte.Soporte;

import java.security.KeyPair;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

/**
 * Recorrido HTTP completo con la cadena de seguridad real, el perfil "prototipo" y un servicio de voz falso:
 * arranque del administrador → registro asistido → enrolamiento → inicio de sesión → API con sesión → bloqueo.
 * No usa PostgreSQL: la persistencia es en memoria (D-051) y se excluye la autoconfiguración JPA.
 */
@SpringBootTest(properties = {
        "spring.autoconfigure.exclude=org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration,"
                + "org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration,"
                + "org.springframework.boot.data.jpa.autoconfigure.DataJpaRepositoriesAutoConfiguration"})
@AutoConfigureMockMvc
@ActiveProfiles("prototipo")
class ApiHttpTest {

    @TestConfiguration
    static class VozDePrueba {
        @Bean
        @Primary
        Soporte.VozFalsa vozFalsa() {
            return new Soporte.VozFalsa();
        }
    }

    @Autowired
    MockMvc mvc;
    @Autowired
    Soporte.VozFalsa voz;

    private final JsonMapper json = JsonMapper.builder().build();
    private final MockMultipartFile audio = new MockMultipartFile("audio", "m.wav", "audio/wav", new byte[]{1, 2});

    private record Resp(int estado, JsonNode cuerpo) {
    }

    private Resp llamar(MockHttpServletRequestBuilder req, String token, Object cuerpo) throws Exception {
        if (token != null) {
            req.header("Authorization", "Bearer " + token);
        }
        if (cuerpo != null) {
            req.contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(cuerpo));
        }
        var r = mvc.perform(req).andReturn().getResponse();
        String texto = r.getContentAsString();
        return new Resp(r.getStatus(), texto.isEmpty() ? null : json.readTree(texto));
    }

    private record Persona(String usuarioId, String dispositivoId, KeyPair par) {
    }

    private Persona completarRegistro(String codigo) throws Exception {
        KeyPair par = Soporte.claveP256();
        Resp datos = llamar(get("/api/v1/registros/" + codigo), null, null);
        assertEquals(200, datos.estado());
        assertEquals(200, llamar(post("/api/v1/registros/" + codigo + "/datos"), null,
                Map.of("confirmaDatos", true, "celular", Soporte.CELULAR, "pin", Soporte.PIN,
                        "clavePublicaDispositivo", Soporte.publica(par))).estado());
        String base = "/prototipo/registro/" + codigo + "/enrolamiento";
        for (int i = 1; i <= 3; i++) {
            Resp d = llamar(post(base + "/desafios"), null, null);
            voz.respuestas.add(new IServicioVozCliente.ResultadoTecnico(true, null, List.of(), i));
            var m = mvc.perform(multipart(base + "/muestras").file(audio).param("desafioId", d.cuerpo().get("desafioId").asString()))
                    .andReturn().getResponse();
            assertEquals(200, m.getStatus());
        }
        voz.respuestas.add(new IServicioVozCliente.ResultadoEnrolamiento(true, null, 3));
        assertTrue(llamar(post(base + "/finalizacion"), null, null).cuerpo().get("correcto").asBoolean());
        Resp fin = llamar(post("/api/v1/registros/" + codigo + "/finalizacion"), null, null);
        assertEquals(200, fin.estado());
        return new Persona(fin.cuerpo().get("usuarioId").asString(), fin.cuerpo().get("dispositivoId").asString(), par);
    }

    private String iniciarSesion(Persona p) throws Exception {
        String nonce = llamar(post("/prototipo/autenticacion/nonces"), null, Map.of("dispositivoId", p.dispositivoId()))
                .cuerpo().get("nonce").asString();
        String t = llamar(post("/prototipo/autenticacion/transacciones"), null, Map.of("dispositivoId", p.dispositivoId(),
                "nonce", nonce, "firma", Soporte.firmar(p.par(), nonce, p.dispositivoId(), "INICIO_SESION")))
                .cuerpo().get("transaccionId").asString();
        assertEquals("CONTINUAR", llamar(post("/prototipo/autenticacion/transacciones/" + t + "/pin"), null,
                Map.of("pin", Soporte.PIN)).cuerpo().get("estado").asString());
        voz.encolar(true, null);
        String r = mvc.perform(multipart("/prototipo/autenticacion/transacciones/" + t + "/voz").file(audio))
                .andReturn().getResponse().getContentAsString();
        JsonNode n = json.readTree(r);
        assertEquals("AUTENTICADO", n.get("estado").asString());
        return n.get("sesion").asString();
    }

    @Test
    void recorridoCompletoConAutorizacion() throws Exception {
        // Sin sesión no hay acceso a la API, y las rutas desconocidas se deniegan.
        assertEquals(401, llamar(get("/api/v1/usuarios/me"), null, null).estado());
        assertEquals("NO_AUTENTICADO", llamar(get("/api/v1/usuarios/me"), null, null).cuerpo().get("error").asString());
        assertEquals(401, llamar(get("/api/v1/admin/usuarios"), "token-inventado", null).estado());
        assertEquals(401, llamar(get("/usuarios"), null, null).estado());

        // Primer administrador (arranque del prototipo) y su registro en el celular.
        Resp arranque = llamar(post("/prototipo/arranque/administrador"), null, Map.of("dni", Soporte.DNI_ADMIN));
        assertEquals(200, arranque.estado());
        Persona admin = completarRegistro(arranque.cuerpo().get("codigoRegistro").asString());
        assertEquals(409, llamar(post("/prototipo/arranque/administrador"), null, Map.of("dni", Soporte.DNI_USUARIO)).estado());
        String tokenAdmin = iniciarSesion(admin);

        // Registro asistido de un USER.
        Resp inicio = llamar(post("/api/v1/admin/registros"), tokenAdmin, Map.of("dni", Soporte.DNI_USUARIO));
        assertEquals(200, inicio.estado());
        String codigo = inicio.cuerpo().get("codigoRegistro").asString();
        assertEquals(409, llamar(get("/api/v1/registros/" + codigo), null, null).estado());
        assertEquals(204, llamar(post("/api/v1/admin/registros/" + codigo + "/validacion-identidad"), tokenAdmin, null).estado());
        Persona usuario = completarRegistro(codigo);
        String tokenUsuario = iniciarSesion(usuario);

        // Datos propios, sin hash del PIN.
        Resp me = llamar(get("/api/v1/usuarios/me"), tokenUsuario, null);
        assertEquals(200, me.estado());
        assertEquals("USER", me.cuerpo().get("rol").asString());
        assertFalse(me.cuerpo().has("pinHash"));

        // Un USER no accede a la API del administrador.
        Resp prohibido = llamar(get("/api/v1/admin/usuarios"), tokenUsuario, null);
        assertEquals(403, prohibido.estado());
        assertEquals("ACCESO_DENEGADO", prohibido.cuerpo().get("error").asString());

        // El administrador consulta y bloquea; la sesión del usuario queda revocada.
        assertEquals(2, llamar(get("/api/v1/admin/usuarios"), tokenAdmin, null).cuerpo().size());
        assertEquals(204, llamar(post("/api/v1/admin/usuarios/" + usuario.usuarioId() + "/bloqueo"), tokenAdmin, null).estado());
        assertEquals(401, llamar(get("/api/v1/usuarios/me"), tokenUsuario, null).estado());
        Resp acciones = llamar(get("/api/v1/admin/auditoria/acciones-administrativas"), tokenAdmin, null);
        assertTrue(acciones.cuerpo().toString().contains("ADMIN_BLOQUEO_CUENTA"));

        // Cierre de sesión del administrador.
        assertEquals(204, llamar(delete("/api/v1/sesiones/actual"), tokenAdmin, null).estado());
        assertEquals(401, llamar(get("/api/v1/usuarios/me"), tokenAdmin, null).estado());
    }

    @Test
    void erroresSinDetallesInternos() throws Exception {
        Resp r = llamar(get("/api/v1/registros/no-existe"), null, null);
        assertEquals(404, r.estado());
        assertEquals(Map.of("error", "REGISTRO_NO_VALIDO"), json.convertValue(r.cuerpo(), Map.class));
        assertEquals(401, llamar(post("/prototipo/autenticacion/nonces"), null, Map.of("dispositivoId", "x")).estado());
        assertEquals(404, llamar(post("/prototipo/autenticacion/transacciones/no-existe/pin"), null,
                Map.of("pin", Soporte.PIN)).estado());
        var malFormado = mvc.perform(post("/api/v1/registros/x/datos").contentType(MediaType.APPLICATION_JSON).content("{"))
                .andReturn().getResponse();
        assertEquals(400, malFormado.getStatus());
        assertEquals("{\"error\":\"SOLICITUD_INVALIDA\"}", malFormado.getContentAsString());
    }
}
