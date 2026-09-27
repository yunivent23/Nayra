package upc.pe.nayrabackend.serviceimplements;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import upc.pe.nayrabackend.config.NayraProperties;
import upc.pe.nayrabackend.entities.Dispositivos;
import upc.pe.nayrabackend.entities.Identificadores;
import upc.pe.nayrabackend.excepciones.NayraException;
import upc.pe.nayrabackend.repositories.IDispositivosRepository;
import upc.pe.nayrabackend.serviceinterfaces.IDispositivoService;

import java.nio.charset.StandardCharsets;
import java.security.AlgorithmParameters;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.ECParameterSpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Clock;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Vinculación del dispositivo con par de claves (D-048).
 * La clave privada nunca sale del teléfono; aquí solo se guarda la clave pública.
 * Nonces en memoria: PROVISIONAL (su almacenamiento sigue pendiente, 03 §16.6). Su vida es un valor PROVISIONAL DEL PROTOTIPO.
 */
@Service
public class DispositivoServiceImplement implements IDispositivoService {

    /** PROVISIONAL: la app del prototipo solo existe para Android (D-007, D-048). */
    static final String PLATAFORMA_PROTOTIPO = "ANDROID";

    private record NonceEmitido(String dispositivoId, Instant expira) {
    }

    private final IDispositivosRepository dispositivos;
    private final NayraProperties props;
    private final SecureRandom random;
    private final Clock reloj;
    private final Map<String, NonceEmitido> nonces = new ConcurrentHashMap<>();

    public DispositivoServiceImplement(IDispositivosRepository dispositivos, NayraProperties props, SecureRandom random, Clock reloj) {
        this.dispositivos = dispositivos;
        this.props = props;
        this.random = random;
        this.reloj = reloj;
    }

    @Override
    public PublicKey leerClavePublica(String base64) {
        try {
            PublicKey clave = KeyFactory.getInstance("EC").generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode(base64)));
            if (!(clave instanceof ECPublicKey ec) || !esP256(ec.getParams())) {
                throw NayraException.solicitudInvalida("CLAVE_DISPOSITIVO_INVALIDA");
            }
            return clave;
        } catch (GeneralSecurityException | IllegalArgumentException | NullPointerException e) {
            throw NayraException.solicitudInvalida("CLAVE_DISPOSITIVO_INVALIDA");
        }
    }

    @Override
    @Transactional
    public synchronized Dispositivos vincular(String usuarioId, PublicKey clavePublica) {
        Instant ahora = Instant.now(reloj);
        // Un único dispositivo activo (D-039): el anterior queda revocado y se guarda antes de insertar el nuevo.
        dispositivos.activoDeUsuario(usuarioId).ifPresent(d -> {
            d.revocar(ahora);
            dispositivos.guardar(d);
        });
        Dispositivos nuevo = new Dispositivos(Identificadores.nuevo(), usuarioId, clavePublica, PLATAFORMA_PROTOTIPO, ahora);
        dispositivos.guardar(nuevo);
        return nuevo;
    }

    @Override
    public void revocarActivo(String usuarioId) {
        dispositivos.activoDeUsuario(usuarioId).ifPresent(d -> {
            d.revocar(Instant.now(reloj));
            dispositivos.guardar(d);
        });
    }

    @Override
    public boolean existe(String dispositivoId) {
        return dispositivos.porId(dispositivoId).isPresent();
    }

    @Override
    public String emitirNonce(String dispositivoId) {
        Dispositivos dispositivo = dispositivos.porId(dispositivoId)
                .filter(Dispositivos::activo)
                .orElseThrow(() -> new SecurityException("Dispositivo no vinculado."));
        purgarVencidos();
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String nonce = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        nonces.put(nonce, new NonceEmitido(dispositivo.getId(), Instant.now(reloj).plus(props.dispositivo().vidaNonce())));
        return nonce;
    }

    @Override
    public String verificarFirma(String dispositivoId, String nonce, String firmaBase64, String proposito) {
        NonceEmitido emitido = nonce == null ? null : nonces.remove(nonce); // un solo uso
        if (emitido == null || !emitido.dispositivoId().equals(dispositivoId) || Instant.now(reloj).isAfter(emitido.expira())) {
            throw new SecurityException("Nonce inválido, usado o vencido.");
        }
        Dispositivos dispositivo = dispositivos.porId(dispositivoId)
                .filter(Dispositivos::activo)
                .orElseThrow(() -> new SecurityException("Dispositivo no vinculado."));
        try {
            Signature verificador = Signature.getInstance("SHA256withECDSA");
            verificador.initVerify(dispositivo.getClavePublica());
            verificador.update(mensajeFirmado(nonce, dispositivoId, proposito));
            if (firmaBase64 == null || !verificador.verify(Base64.getDecoder().decode(firmaBase64))) {
                throw new SecurityException("Firma del dispositivo no válida.");
            }
        } catch (GeneralSecurityException | IllegalArgumentException e) {
            throw new SecurityException("Firma del dispositivo no válida.");
        }
        return dispositivo.getUsuarioId();
    }

    public static byte[] mensajeFirmado(String nonce, String dispositivoId, String proposito) {
        return (nonce + "|" + dispositivoId + "|" + proposito).getBytes(StandardCharsets.UTF_8);
    }

    private void purgarVencidos() {
        Instant ahora = Instant.now(reloj);
        nonces.values().removeIf(n -> ahora.isAfter(n.expira()));
    }

    private static boolean esP256(ECParameterSpec params) throws GeneralSecurityException {
        AlgorithmParameters p256 = AlgorithmParameters.getInstance("EC");
        p256.init(new ECGenParameterSpec("secp256r1"));
        ECParameterSpec esperado = p256.getParameterSpec(ECParameterSpec.class);
        return esperado.getCurve().equals(params.getCurve())
                && esperado.getOrder().equals(params.getOrder())
                && esperado.getGenerator().equals(params.getGenerator());
    }
}
