package upc.pe.nayrabackend.integracion;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import upc.pe.nayrabackend.clients.VozClient;
import upc.pe.nayrabackend.config.NayraVozProperties;
import upc.pe.nayrabackend.dtos.ResultadoVozDTO;
import upc.pe.nayrabackend.exceptions.CodigoError;
import upc.pe.nayrabackend.exceptions.NayraException;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.Duration;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Contrato Java → Python (04_API.md §3.2) contra el servicio de voz en ejecución.
 * Requiere NAYRA_IT_VOZ_URL y NAYRA_IT_VOZ_TOKEN.
 */
@EnabledIfEnvironmentVariable(named = "NAYRA_IT_VOZ_URL", matches = ".+")
class VozContratoIntegracionTest {

    private static VozClient cliente(String token) {
        return new VozClient(new NayraVozProperties(System.getenv("NAYRA_IT_VOZ_URL"), token,
                Duration.ofSeconds(2), Duration.ofSeconds(30)));
    }

    static byte[] wav(float frecuencia, int muestras) throws Exception {
        byte[] pcm = new byte[muestras * 2];
        Random r = new Random(0);
        for (int i = 0; i < muestras; i++) {
            short v = (short) (r.nextGaussian() * 2000);
            pcm[2 * i] = (byte) v;
            pcm[2 * i + 1] = (byte) (v >> 8);
        }
        AudioFormat f = new AudioFormat(frecuencia, 16, 1, true, false);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        AudioSystem.write(new AudioInputStream(new ByteArrayInputStream(pcm), f, muestras), AudioFileFormat.Type.WAVE, out);
        return out.toByteArray();
    }

    @Test
    void respuestaDeVerificacionSeInterpreta() throws Exception {
        ResultadoVozDTO r = cliente(System.getenv("NAYRA_IT_VOZ_TOKEN"))
                .verificar(123456L, "sol cuatro siete dos mesa", wav(16000, 48000));
        assertNotNull(r.requestId());
        assertEquals(3.0, r.calidad().duracionS(), 0.001);
        assertNotNull(r.contenido().coincide());
        assertNotNull(r.spoofing().puntaje());
        assertFalse(r.biometria().perfilEncontrado());
    }

    @Test
    void audioConFormatoNoAprobadoEsCalidadInsuficiente() throws Exception {
        NayraException e = assertThrows(NayraException.class, () -> cliente(System.getenv("NAYRA_IT_VOZ_TOKEN"))
                .verificar(1L, "sol", wav(44100, 44100)));
        assertEquals(CodigoError.CALIDAD_INSUFICIENTE, e.getCodigo());
    }

    @Test
    void tokenDeServicioIncorrectoNoAccede() throws Exception {
        NayraException e = assertThrows(NayraException.class, () -> cliente("incorrecto")
                .verificar(1L, "sol", wav(16000, 16000)));
        assertEquals(CodigoError.SERVICIO_VOZ_NO_DISPONIBLE, e.getCodigo());
    }
}
