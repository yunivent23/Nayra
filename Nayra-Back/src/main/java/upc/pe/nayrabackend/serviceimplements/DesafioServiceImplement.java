package upc.pe.nayrabackend.serviceimplements;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import upc.pe.nayrabackend.config.NayraSeguridadProperties;
import upc.pe.nayrabackend.entities.DesafioAutenticacion;
import upc.pe.nayrabackend.entities.Dispositivos;
import upc.pe.nayrabackend.entities.Users;
import upc.pe.nayrabackend.exceptions.CodigoError;
import upc.pe.nayrabackend.exceptions.NayraException;
import upc.pe.nayrabackend.repositories.IDesafiosRepository;
import upc.pe.nayrabackend.serviceinterfaces.IDesafioService;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Service
public class DesafioServiceImplement implements IDesafioService {

    /** Dígitos en palabras, tal como se pronuncian y como los reconoce la gramática de Vosk (D-036). */
    static final List<String> DIGITOS = List.of(
            "cero", "uno", "dos", "tres", "cuatro", "cinco", "seis", "siete", "ocho", "nueve");
    private static final int BYTES_NONCE = 32;

    private final IDesafiosRepository repository;
    private final SecureRandom random;
    private final Clock reloj;
    private final NayraSeguridadProperties propiedades;
    private final List<String> palabras;

    public DesafioServiceImplement(IDesafiosRepository repository, SecureRandom random, Clock reloj,
                                   NayraSeguridadProperties propiedades) {
        this.repository = repository;
        this.random = random;
        this.reloj = reloj;
        this.propiedades = propiedades;
        this.palabras = cargarPalabras();
    }

    @Override
    public List<String> generarElementos() {
        // Nunca 6 dígitos seguidos (D-037): el formato fija exactamente 3 dígitos entre dos palabras.
        List<String> elementos = new ArrayList<>(5);
        elementos.add(palabras.get(random.nextInt(palabras.size())));
        for (int i = 0; i < 3; i++) {
            elementos.add(DIGITOS.get(random.nextInt(DIGITOS.size())));
        }
        elementos.add(palabras.get(random.nextInt(palabras.size())));
        return elementos;
    }

    @Override
    @Transactional
    public DesafioAutenticacion emitir(String proposito, Users usuario, Dispositivos dispositivo, boolean conVoz) {
        OffsetDateTime ahora = OffsetDateTime.now(reloj);
        byte[] nonce = new byte[BYTES_NONCE];
        random.nextBytes(nonce);

        DesafioAutenticacion d = new DesafioAutenticacion();
        d.setIdPublico(UUID.randomUUID());
        d.setUsuario(usuario);
        d.setDispositivo(dispositivo);
        d.setProposito(proposito);
        d.setElementos(conVoz ? String.join(" ", generarElementos()) : null);
        d.setNonce(Base64.getUrlEncoder().withoutPadding().encodeToString(nonce));
        d.setFechaEmision(ahora);
        d.setFechaExpiracion(ahora.plus(propiedades.desafio().vigencia()));
        return repository.save(d);
    }

    @Override
    @Transactional
    public DesafioAutenticacion consumir(UUID idPublico, String proposito) {
        DesafioAutenticacion d = repository.findByIdPublico(idPublico)
                .filter(x -> x.getProposito().equals(proposito))
                .orElseThrow(() -> new NayraException(CodigoError.DESAFIO_INVALIDO));
        if (repository.consumir(d.getId(), OffsetDateTime.now(reloj)) != 1) {
            throw new NayraException(CodigoError.DESAFIO_INVALIDO);
        }
        return d;
    }

    List<String> palabras() {
        return palabras;
    }

    private static List<String> cargarPalabras() {
        try (InputStream in = new ClassPathResource("challenge-words.es.txt").getInputStream()) {
            List<String> lista = new String(in.readAllBytes(), StandardCharsets.UTF_8).lines()
                    .map(String::strip)
                    .filter(l -> !l.isEmpty() && !l.startsWith("#"))
                    .toList();
            if (lista.size() < 2) {
                throw new IllegalStateException("La lista de palabras del desafío está vacía");
            }
            return lista;
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo leer challenge-words.es.txt", e);
        }
    }
}
