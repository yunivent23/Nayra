package upc.pe.nayrabackend.serviceimplements;

import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import upc.pe.nayrabackend.serviceinterfaces.IServicioVozCliente;

/**
 * Cliente REST del servicio de voz (D-010): multipart/form-data, sin reintentos automáticos.
 * CONTRATO PROVISIONAL bajo /prototipo/v1 — pendiente de docs/04_API.md (D-014).
 * Nunca se registra el audio ni el PIN.
 */
@Profile("prototipo")
@Service
public class ServicioVozClienteImplement implements IServicioVozCliente {

    private record RespuestaPin(boolean reconocido, String pin) {
    }

    private final RestClient cliente;

    public ServicioVozClienteImplement(RestClient servicioVozRestClient) {
        this.cliente = servicioVozRestClient;
    }

    private static ByteArrayResource wav(byte[] audio) {
        return new ByteArrayResource(audio) {
            @Override
            public String getFilename() {
                return "muestra.wav";
            }
        };
    }

    private <T> T post(String ruta, MultiValueMap<String, Object> cuerpo, Class<T> tipo) {
        try {
            RestClient.RequestBodySpec solicitud = cliente.post().uri(ruta);
            if (cuerpo != null) {
                solicitud = solicitud.contentType(MediaType.MULTIPART_FORM_DATA).body(cuerpo);
            }
            T respuesta = solicitud.retrieve().body(tipo);
            if (respuesta == null) {
                throw new ServicioVozNoDisponibleException("Respuesta vacía del servicio de voz.", null);
            }
            return respuesta;
        } catch (RestClientException e) {
            // Timeouts, conexión rechazada, 4xx/5xx: servicio de voz no disponible.
            throw new ServicioVozNoDisponibleException("Servicio de voz no disponible.", e);
        }
    }

    @Override
    public ResultadoTecnico verificar(String usuarioId, String desafio, byte[] audioWav) {
        MultiValueMap<String, Object> cuerpo = new LinkedMultiValueMap<>();
        cuerpo.add("usuarioId", usuarioId);
        cuerpo.add("desafio", desafio);
        cuerpo.add("audio", wav(audioWav));
        return post("/prototipo/v1/verificaciones", cuerpo, ResultadoTecnico.class);
    }

    @Override
    public ResultadoTecnico agregarMuestraEnrolamiento(String usuarioId, String desafio, byte[] audioWav) {
        MultiValueMap<String, Object> cuerpo = new LinkedMultiValueMap<>();
        cuerpo.add("desafio", desafio);
        cuerpo.add("audio", wav(audioWav));
        return post("/prototipo/v1/enrolamientos/" + usuarioId + "/muestras", cuerpo, ResultadoTecnico.class);
    }

    @Override
    public ResultadoEnrolamiento finalizarEnrolamiento(String usuarioId) {
        return post("/prototipo/v1/enrolamientos/" + usuarioId + "/finalizacion", null, ResultadoEnrolamiento.class);
    }

    @Override
    public String transcribirPin(byte[] audioWav) {
        MultiValueMap<String, Object> cuerpo = new LinkedMultiValueMap<>();
        cuerpo.add("audio", wav(audioWav));
        RespuestaPin respuesta = post("/prototipo/v1/transcripciones-pin", cuerpo, RespuestaPin.class);
        return respuesta.reconocido() ? respuesta.pin() : null;
    }
}
