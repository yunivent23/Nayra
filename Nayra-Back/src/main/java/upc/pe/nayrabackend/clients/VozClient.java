package upc.pe.nayrabackend.clients;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import upc.pe.nayrabackend.config.NayraVozProperties;
import upc.pe.nayrabackend.dtos.ResultadoVozDTO;
import upc.pe.nayrabackend.exceptions.CodigoError;
import upc.pe.nayrabackend.exceptions.NayraException;

import java.net.http.HttpClient;

/**
 * Cliente REST interno del servicio de voz (D-040). Sin reintentos automáticos: un timeout o error
 * se informa como SERVICIO_VOZ_NO_DISPONIBLE y no cuenta como intento fallido del usuario.
 */
@Component
public class VozClient {

    static final String CABECERA_TOKEN = "X-Nayra-Service-Token";

    private final RestClient cliente;

    public VozClient(NayraVozProperties propiedades) {
        HttpClient http = HttpClient.newBuilder().connectTimeout(propiedades.timeoutConexion()).build();
        JdkClientHttpRequestFactory fabrica = new JdkClientHttpRequestFactory(http);
        fabrica.setReadTimeout(propiedades.timeoutLectura());
        this.cliente = RestClient.builder()
                .baseUrl(propiedades.url())
                .requestFactory(fabrica)
                .defaultHeader(CABECERA_TOKEN, propiedades.token())
                .build();
    }

    public ResultadoVozDTO verificar(Long usuarioId, String textoEsperado, byte[] audio) {
        MultiValueMap<String, Object> partes = new LinkedMultiValueMap<>();
        partes.add("usuario_id", String.valueOf(usuarioId));
        partes.add("texto_esperado", textoEsperado);
        partes.add("audio", wav(audio));
        try {
            ResultadoVozDTO r = cliente.post()
                    .uri("/interno/v1/verificaciones")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(partes)
                    .retrieve()
                    .body(ResultadoVozDTO.class);
            if (r == null) {
                throw new NayraException(CodigoError.SERVICIO_VOZ_NO_DISPONIBLE);
            }
            return r;
        } catch (RestClientException e) {
            throw new NayraException(CodigoError.SERVICIO_VOZ_NO_DISPONIBLE);
        }
    }

    private static ByteArrayResource wav(byte[] audio) {
        return new ByteArrayResource(audio) {
            @Override
            public String getFilename() {
                return "muestra.wav";
            }
        };
    }
}
