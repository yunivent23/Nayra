package upc.pe.nayrabackend.integracion;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import upc.pe.nayrabackend.clients.VozClient;
import upc.pe.nayrabackend.dtos.ResultadoVozDTO;
import upc.pe.nayrabackend.entities.DesafioAutenticacion;
import upc.pe.nayrabackend.entities.Dispositivos;
import upc.pe.nayrabackend.entities.Role;
import upc.pe.nayrabackend.entities.Users;
import upc.pe.nayrabackend.exceptions.NayraException;
import upc.pe.nayrabackend.repositories.IUsersRepository;
import upc.pe.nayrabackend.serviceinterfaces.IDesafioService;
import upc.pe.nayrabackend.serviceinterfaces.IDispositivoService;
import upc.pe.nayrabackend.serviceinterfaces.IPinService;

import java.security.KeyPair;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Base64;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static upc.pe.nayrabackend.serviceimplements.FirmaDispositivoServiceImplementTest.firmar;
import static upc.pe.nayrabackend.serviceimplements.FirmaDispositivoServiceImplementTest.par;

/**
 * Flujo completo contra PostgreSQL real: migraciones Flyway, dispositivo, desafío de un solo uso,
 * PIN, umbrales, sesión opaca, inactividad de 5 minutos, bloqueo y autorización.
 * El servicio de voz se simula (sus puntajes son entradas del flujo, no resultados biométricos).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("it")
@EnabledIfEnvironmentVariable(named = "NAYRA_IT_DB_URL", matches = ".+")
@TestPropertySource(properties = {
        // Valores de prueba para ejercitar la lógica de decisión; NO son umbrales del proyecto (D-038)
        "nayra.umbrales.similitud-minima=0.5",
        "nayra.umbrales.spoofing-minimo=0.0",
        "nayra.umbrales.voz-neta-minima-segundos=1.0",
        "nayra.umbrales.snr-minimo-db=10.0",
        "nayra.umbrales.saturacion-maxima=0.1",
        "nayra.umbrales.confianza-contenido-minima=0.5"
})
class AutenticacionIntegracionTest {

    private static final String PIN = "482913";

    @TestConfiguration
    static class Config {
        @Bean
        @Primary
        RelojMutable relojMutable() {
            return new RelojMutable();
        }
    }

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired Flyway flyway;
    @Autowired JdbcTemplate jdbc;
    @Autowired IUsersRepository usuarios;
    @Autowired IPinService pines;
    @Autowired IDesafioService desafios;
    @Autowired IDispositivoService dispositivos;
    @Autowired RelojMutable reloj;
    @MockitoBean VozClient voz;

    Users usuario;
    KeyPair clave;
    Dispositivos dispositivo;

    @BeforeEach
    void preparar() throws Exception {
        flyway.clean();
        flyway.migrate();
        usuario = crearUsuario("ana", "12345678", "USUARIO");
        clave = par("secp256r1");
        dispositivo = registrarDispositivo(usuario, clave);
        when(voz.verificar(anyLong(), anyString(), any())).thenReturn(vozAprobada());
    }

    @Test
    void migracionesAplicadas() {
        assertEquals("2", flyway.info().current().getVersion().getVersion());
    }

    @Test
    void inicioDeSesionCompletoYCierre() throws Exception {
        String token = iniciarSesion(PIN, clave);
        mvc.perform(get("/api/v1/sesiones").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].actual").value(true));
        String hashGuardado = jdbc.queryForObject("select token_hash from sesiones", String.class);
        assertNotEquals(token, hashGuardado);
        assertEquals(64, hashGuardado.length());

        mvc.perform(delete("/api/v1/auth/sesiones/actual").header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
        mvc.perform(get("/api/v1/sesiones").header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void desafioEsDeUnSoloUso() throws Exception {
        JsonNode d = pedirDesafio();
        enviarLogin(d, PIN, clave).andExpect(status().isCreated());
        enviarLogin(d, PIN, clave).andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.codigo").value("DESAFIO_INVALIDO"));
    }

    @Test
    void desafioExpira() throws Exception {
        JsonNode d = pedirDesafio();
        reloj.avanzar(Duration.ofSeconds(91));
        enviarLogin(d, PIN, clave).andExpect(jsonPath("$.codigo").value("DESAFIO_INVALIDO"));
    }

    @Test
    void cierrePorCincoMinutosDeInactividad() throws Exception {
        String token = iniciarSesion(PIN, clave);
        reloj.avanzar(Duration.ofMinutes(4));
        mvc.perform(get("/api/v1/sesiones").header("Authorization", "Bearer " + token)).andExpect(status().isOk());
        reloj.avanzar(Duration.ofMinutes(4));
        mvc.perform(get("/api/v1/sesiones").header("Authorization", "Bearer " + token)).andExpect(status().isOk());
        reloj.avanzar(Duration.ofMinutes(5));
        mvc.perform(get("/api/v1/sesiones").header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
        assertEquals("INACTIVIDAD", jdbc.queryForObject("select motivo_cierre from sesiones", String.class));
    }

    @Test
    void firmaDeOtraClaveEsRechazadaSinContarIntento() throws Exception {
        enviarLogin(pedirDesafio(), PIN, par("secp256r1"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.codigo").value("AUTENTICACION_FALLIDA"));
        assertEquals(0, usuarios.findById(usuario.getId()).orElseThrow().getIntentosFallidos());
        verifyNoInteractions(voz);
    }

    @Test
    void pinIncorrectoBloqueaAlTercerIntento() throws Exception {
        enviarLogin(pedirDesafio(), "000000", clave).andExpect(jsonPath("$.codigo").value("AUTENTICACION_FALLIDA"));
        enviarLogin(pedirDesafio(), "000000", clave).andExpect(jsonPath("$.codigo").value("AUTENTICACION_FALLIDA"));
        enviarLogin(pedirDesafio(), "000000", clave).andExpect(status().isLocked());
        // Aun con el PIN correcto la cuenta sigue bloqueada
        enviarLogin(pedirDesafio(), PIN, clave).andExpect(jsonPath("$.codigo").value("CUENTA_BLOQUEADA"));
        verifyNoInteractions(voz);
    }

    @Test
    void vozSinCoincidenciaCuentaComoFallo() throws Exception {
        when(voz.verificar(anyLong(), anyString(), any())).thenReturn(new ResultadoVozDTO("r",
                new ResultadoVozDTO.Calidad(4.0, 3.5, 20.0, 0.0),
                new ResultadoVozDTO.Contenido("x", true, 0.9),
                new ResultadoVozDTO.Spoofing(1.0),
                new ResultadoVozDTO.Biometria(0.2, true)));
        enviarLogin(pedirDesafio(), PIN, clave).andExpect(jsonPath("$.codigo").value("AUTENTICACION_FALLIDA"));
        assertEquals(1, usuarios.findById(usuario.getId()).orElseThrow().getIntentosFallidos());
    }

    @Test
    void calidadYContenidoSeInformanParaCorregirLaCaptura() throws Exception {
        when(voz.verificar(anyLong(), anyString(), any())).thenReturn(new ResultadoVozDTO("r",
                new ResultadoVozDTO.Calidad(4.0, 0.3, 20.0, 0.0), null, null, null));
        enviarLogin(pedirDesafio(), PIN, clave).andExpect(jsonPath("$.codigo").value("CALIDAD_INSUFICIENTE"));
        when(voz.verificar(anyLong(), anyString(), any())).thenReturn(new ResultadoVozDTO("r",
                new ResultadoVozDTO.Calidad(4.0, 3.0, 20.0, 0.0),
                new ResultadoVozDTO.Contenido("sol dos", false, 0.9), null, null));
        enviarLogin(pedirDesafio(), PIN, clave).andExpect(jsonPath("$.codigo").value("CONTENIDO_INCORRECTO"));
    }

    @Test
    void registrarNuevoDispositivoRevocaElAnteriorYSusSesiones() throws Exception {
        String token = iniciarSesion(PIN, clave);
        KeyPair nueva = par("secp256r1");
        registrarDispositivo(usuario, nueva);
        mvc.perform(get("/api/v1/sesiones").header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
        assertEquals(1, jdbc.queryForObject(
                "select count(*) from dispositivos where estado = 'ACTIVO'", Integer.class));
        // La clave anterior ya no sirve: el desafío pedido con el dispositivo viejo falla
        enviarLogin(pedirDesafio(), PIN, clave).andExpect(jsonPath("$.codigo").value("AUTENTICACION_FALLIDA"));
    }

    @Test
    void dispositivoInexistenteRecibeDesafioQueFalla() throws Exception {
        MvcResult r = mvc.perform(post("/api/v1/auth/desafios").contentType("application/json")
                        .content("{\"dispositivoId\":\"00000000-0000-0000-0000-000000000000\"}"))
                .andExpect(status().isCreated()).andReturn();
        JsonNode d = json.readTree(r.getResponse().getContentAsString());
        enviarLogin(d, PIN, clave).andExpect(jsonPath("$.codigo").value("AUTENTICACION_FALLIDA"));
    }

    @Test
    void usuariosSoloParaAdministradorYSinDatosSensibles() throws Exception {
        String tokenUsuario = iniciarSesion(PIN, clave);
        mvc.perform(get("/usuarios").header("Authorization", "Bearer " + tokenUsuario))
                .andExpect(status().isForbidden());

        Users admin = crearUsuario("admin", "87654321", "ADMINISTRADOR");
        KeyPair claveAdmin = par("secp256r1");
        dispositivo = registrarDispositivo(admin, claveAdmin);
        String tokenAdmin = iniciarSesion(PIN, claveAdmin);
        String cuerpo = mvc.perform(get("/usuarios").header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        assertFalse(cuerpo.contains("password"));
        assertFalse(cuerpo.contains("pin"));
        assertFalse(cuerpo.contains("argon2"));
    }

    @Test
    void sinSesionEsNoAutorizado() throws Exception {
        mvc.perform(get("/usuarios")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/v1/sesiones").header("Authorization", "Bearer inventado"))
                .andExpect(status().isUnauthorized());
    }

    // ----------------------------------------------------------------------------------------------

    private Users crearUsuario(String username, String dni, String rol) {
        Users u = new Users();
        u.setUsername(username);
        u.setEnabled(true);
        u.setDni(dni);
        u.setEmail(username + "@nayra.test");
        u.setFechaNacimiento(LocalDate.of(1990, 1, 1));
        u.setTelefono("999999999");
        u.setPinHash(pines.hashear(PIN));
        Role r = new Role();
        r.setRol(rol);
        r.setUser(u);
        u.setRoles(new java.util.ArrayList<>(List.of(r)));
        return usuarios.save(u);
    }

    private Dispositivos registrarDispositivo(Users u, KeyPair par) throws Exception {
        DesafioAutenticacion d = desafios.emitir(IDesafioService.REGISTRO_DISPOSITIVO, u, null, false);
        String mensaje = "NAYRA|v1|REGISTRO_DISPOSITIVO|" + d.getNonce() + "|-";
        return dispositivos.registrar(u, Base64.getEncoder().encodeToString(par.getPublic().getEncoded()),
                "ANDROID", "Teléfono de prueba", d.getIdPublico(), firmar(par, mensaje));
    }

    private JsonNode pedirDesafio() throws Exception {
        MvcResult r = mvc.perform(post("/api/v1/auth/desafios").contentType("application/json")
                        .content("{\"dispositivoId\":\"" + dispositivo.getIdPublico() + "\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.elementos.length()").value(5))
                .andReturn();
        return json.readTree(r.getResponse().getContentAsString());
    }

    private org.springframework.test.web.servlet.ResultActions enviarLogin(JsonNode d, String pin, KeyPair par)
            throws Exception {
        String mensaje = "NAYRA|v1|LOGIN|" + d.get("nonce").asString() + "|" + dispositivo.getIdPublico();
        return mvc.perform(multipart("/api/v1/auth/sesiones")
                .file(new MockMultipartFile("audio", "a.wav", "audio/wav", new byte[]{1, 2, 3}))
                .param("desafioId", d.get("desafioId").asString())
                .param("firma", firmar(par, mensaje))
                .param("pin", pin));
    }

    private String iniciarSesion(String pin, KeyPair par) throws Exception {
        MvcResult r = enviarLogin(pedirDesafio(), pin, par).andExpect(status().isCreated())
                .andExpect(jsonPath("$.inactividadMaximaSegundos").value(300)).andReturn();
        return json.readTree(r.getResponse().getContentAsString()).get("token").asString();
    }

    private static ResultadoVozDTO vozAprobada() {
        return new ResultadoVozDTO("r",
                new ResultadoVozDTO.Calidad(4.0, 3.5, 20.0, 0.0),
                new ResultadoVozDTO.Contenido("x", true, 0.9),
                new ResultadoVozDTO.Spoofing(1.0),
                new ResultadoVozDTO.Biometria(0.9, true));
    }
}
