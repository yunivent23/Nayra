package upc.pe.nayrabackend.serviceimplements;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import upc.pe.nayrabackend.config.VozProperties;
import upc.pe.nayrabackend.serviceinterfaces.IDesafioService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Generación del desafío en Spring Boot con SecureRandom (D-054).
 *
 * - Estructura: palabra + 3 dígitos + palabra; nunca 6 dígitos seguidos.
 * - Vocabulario: archivo versionado compartido con el servicio Python
 *   (shared/desafio/vocabulario_v2.json, lista inicial PROVISIONAL — PENDIENTE DE VALIDACIÓN).
 * - Vida del desafío: PROVISIONAL — PENDIENTE DE VALIDACIÓN (nayra.voz.vida-desafio).
 * - Almacenamiento en memoria: PROVISIONAL (D-051).
 */
@Profile("prototipo")
@Service
public class DesafioServiceImplement implements IDesafioService {

    private final List<String> palabras;
    private final List<String> digitos;
    private final SecureRandom random;
    private final VozProperties props;
    private final Clock reloj;
    private final Map<String, Desafio> vigentes = new ConcurrentHashMap<>();

    public DesafioServiceImplement(VozProperties props, SecureRandom random, Clock reloj) throws IOException {
        JsonNode vocabulario = JsonMapper.builder().build().readTree(Files.readString(Path.of(props.vocabularioRuta())));
        this.palabras = textos(vocabulario.get("palabras"));
        this.digitos = textos(vocabulario.get("digitos"));
        if (palabras.size() < 2 || digitos.size() != 10) {
            throw new IllegalStateException("Vocabulario del desafío incompleto.");
        }
        this.random = random;
        this.props = props;
        this.reloj = reloj;
    }

    private static List<String> textos(JsonNode nodo) {
        List<String> lista = new ArrayList<>();
        nodo.forEach(n -> lista.add(n.asString()));
        return List.copyOf(lista);
    }

    /** Texto del desafío, p. ej. "casa, cuatro, siete, dos, mesa". */
    String generarTexto() {
        String primera = palabras.get(random.nextInt(palabras.size()));
        String ultima;
        do {
            ultima = palabras.get(random.nextInt(palabras.size()));
        } while (ultima.equals(primera));
        List<String> elementos = new ArrayList<>(5);
        elementos.add(primera);
        for (int i = 0; i < 3; i++) {
            elementos.add(digitos.get(random.nextInt(10)));
        }
        elementos.add(ultima);
        return String.join(", ", elementos);
    }

    @Override
    public Desafio emitir(String cuentaId, Contexto contexto) {
        purgarVencidos();
        Desafio desafio = new Desafio(UUID.randomUUID().toString(), generarTexto(), cuentaId, contexto,
                Instant.now(reloj).plus(props.vidaDesafio()));
        vigentes.put(desafio.id(), desafio);
        return desafio;
    }

    @Override
    public Optional<Desafio> consumir(String desafioId, String cuentaId, Contexto contexto) {
        Desafio desafio = desafioId == null ? null : vigentes.remove(desafioId); // un solo uso
        if (desafio == null || !desafio.cuentaId().equals(cuentaId) || desafio.contexto() != contexto
                || Instant.now(reloj).isAfter(desafio.expira())) {
            return Optional.empty();
        }
        return Optional.of(desafio);
    }

    private void purgarVencidos() {
        Instant ahora = Instant.now(reloj);
        vigentes.values().removeIf(d -> ahora.isAfter(d.expira()));
    }
}
