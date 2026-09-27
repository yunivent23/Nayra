package upc.pe.nayrabackend.prototipo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import upc.pe.nayrabackend.config.VozConfiguracion;
import upc.pe.nayrabackend.config.VozProperties;
import upc.pe.nayrabackend.serviceimplements.ServicioVozClienteImplement;
import upc.pe.nayrabackend.serviceinterfaces.IServicioVozCliente.ResultadoEnrolamiento;
import upc.pe.nayrabackend.serviceinterfaces.IServicioVozCliente.ResultadoTecnico;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Prueba de contrato PROVISIONAL (D-014) entre el cliente Java y un servicio Python en ejecución.
 * Solo corre si se define NAYRA_VOZ_URL_PRUEBA (más NAYRA_VOZ_TOKEN_PRUEBA y NAYRA_VOZ_WAV_PRUEBA).
 */
@EnabledIfEnvironmentVariable(named = "NAYRA_VOZ_URL_PRUEBA", matches = ".+")
class ContratoServicioVozTest {

    @Test
    void enrolamientoVerificacionYPinContraElServicioReal() throws Exception {
        VozProperties props = new VozProperties(System.getenv("NAYRA_VOZ_URL_PRUEBA"), System.getenv("NAYRA_VOZ_TOKEN_PRUEBA"),
                Duration.ofSeconds(2), Duration.ofSeconds(60), null, null, null, 3, 3);
        var cliente = new ServicioVozClienteImplement(new VozConfiguracion().servicioVozRestClient(props));
        byte[] wav = Files.readAllBytes(Path.of(System.getenv("NAYRA_VOZ_WAV_PRUEBA")));
        String usuario = UUID.randomUUID().toString();
        String desafio = "llave, cuatro, siete, dos, mesa";

        for (int i = 1; i <= 3; i++) {
            ResultadoTecnico r = cliente.agregarMuestraEnrolamiento(usuario, desafio, wav);
            assertTrue(r.aprobado(), "motivo=" + r.motivo());
            assertEquals(i, r.muestrasValidas());
        }
        ResultadoEnrolamiento fin = cliente.finalizarEnrolamiento(usuario);
        assertTrue(fin.correcto());
        assertEquals(3, fin.muestrasValidas());

        ResultadoTecnico v = cliente.verificar(usuario, desafio, wav);
        assertTrue(v.aprobado(), "motivo=" + v.motivo());
        assertEquals("VERIFICACION", v.etapas().getLast().etapa());

        ResultadoTecnico formato = cliente.verificar(usuario, desafio, "RIFF basura".getBytes());
        assertFalse(formato.aprobado());
        assertEquals("FORMATO_INVALIDO", formato.motivo());

        ResultadoTecnico sinPerfil = cliente.verificar(UUID.randomUUID().toString(), desafio, wav);
        assertEquals("SIN_REFERENCIA", sinPerfil.motivo());
    }
}
