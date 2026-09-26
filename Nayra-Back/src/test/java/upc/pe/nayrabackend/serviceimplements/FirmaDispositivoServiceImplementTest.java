package upc.pe.nayrabackend.serviceimplements;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Signature;
import java.security.spec.ECGenParameterSpec;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

public class FirmaDispositivoServiceImplementTest {

    private final FirmaDispositivoServiceImplement servicio = new FirmaDispositivoServiceImplement();

    public static KeyPair par(String curva) throws Exception {
        KeyPairGenerator g = KeyPairGenerator.getInstance("EC");
        g.initialize(new ECGenParameterSpec(curva));
        return g.generateKeyPair();
    }

    public static String firmar(KeyPair par, String mensaje) throws Exception {
        Signature s = Signature.getInstance("SHA256withECDSA");
        s.initSign(par.getPrivate());
        s.update(mensaje.getBytes(StandardCharsets.UTF_8));
        return Base64.getUrlEncoder().withoutPadding().encodeToString(s.sign());
    }

    @Test
    void aceptaFirmaP256DelMensajeCanonico() throws Exception {
        KeyPair par = par("secp256r1");
        String clave = servicio.validarClavePublica(Base64.getEncoder().encodeToString(par.getPublic().getEncoded()));
        String mensaje = servicio.mensajeCanonico("LOGIN", "nonce123", "disp-1");
        assertEquals("NAYRA|v1|LOGIN|nonce123|disp-1", mensaje);
        assertTrue(servicio.verificar(clave, mensaje, firmar(par, mensaje)));
    }

    @Test
    void rechazaFirmaDeOtroMensajeOClave() throws Exception {
        KeyPair par = par("secp256r1");
        KeyPair otro = par("secp256r1");
        String clave = Base64.getEncoder().encodeToString(par.getPublic().getEncoded());
        String mensaje = servicio.mensajeCanonico("LOGIN", "n1", "d");
        assertFalse(servicio.verificar(clave, servicio.mensajeCanonico("LOGIN", "n2", "d"), firmar(par, mensaje)));
        assertFalse(servicio.verificar(clave, mensaje, firmar(otro, mensaje)));
        assertFalse(servicio.verificar(clave, mensaje, "no-es-una-firma"));
    }

    @Test
    void rechazaClavesQueNoSonP256() throws Exception {
        String p384 = Base64.getEncoder().encodeToString(par("secp384r1").getPublic().getEncoded());
        assertThrows(IllegalArgumentException.class, () -> servicio.validarClavePublica(p384));
        KeyPairGenerator rsa = KeyPairGenerator.getInstance("RSA");
        rsa.initialize(2048);
        String claveRsa = Base64.getEncoder().encodeToString(rsa.generateKeyPair().getPublic().getEncoded());
        assertThrows(IllegalArgumentException.class, () -> servicio.validarClavePublica(claveRsa));
        assertThrows(IllegalArgumentException.class, () -> servicio.validarClavePublica("basura"));
    }
}
