package upc.pe.nayrabackend.prototipo;

import org.junit.jupiter.api.Test;
import upc.pe.nayrabackend.excepciones.NayraException;
import upc.pe.nayrabackend.soporte.Soporte;
import upc.pe.nayrabackend.serviceinterfaces.IDesafioService.Contexto;
import upc.pe.nayrabackend.serviceinterfaces.IDesafioService.Desafio;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.spec.ECGenParameterSpec;
import java.time.Duration;
import java.util.Base64;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class DesafioYDispositivoTest {

    /** Los identificadores de usuario son UUID (D-051). */
    private static final String USUARIO = "3d7a1f52-6b0e-4c9a-8e21-4f6b2d9c0a11";

    private static final Set<String> DIGITOS = Set.of(
            "cero", "uno", "dos", "tres", "cuatro", "cinco", "seis", "siete", "ocho", "nueve");

    @Test
    void desafioTienePalabraTresDigitosPalabraDistintas() throws Exception {
        Soporte s = new Soporte();
        for (int i = 0; i < 200; i++) {
            List<String> partes = List.of(s.desafios.emitir("c1", Contexto.INICIO_SESION).texto().split(", "));
            assertEquals(5, partes.size());
            assertFalse(DIGITOS.contains(partes.get(0)));
            assertTrue(DIGITOS.containsAll(partes.subList(1, 4)));
            assertFalse(DIGITOS.contains(partes.get(4)));
            assertNotEquals(partes.get(0), partes.get(4));
        }
    }

    @Test
    void desafioEsDeUnSoloUsoYDelContextoYCuentaCorrectos() throws Exception {
        Soporte s = new Soporte();
        Desafio d = s.desafios.emitir("c1", Contexto.INICIO_SESION);
        assertTrue(s.desafios.consumir(d.id(), "c2", Contexto.INICIO_SESION).isEmpty());
        Desafio d2 = s.desafios.emitir("c1", Contexto.INICIO_SESION);
        assertTrue(s.desafios.consumir(d2.id(), "c1", Contexto.ENROLAMIENTO).isEmpty());
        Desafio d3 = s.desafios.emitir("c1", Contexto.INICIO_SESION);
        assertTrue(s.desafios.consumir(d3.id(), "c1", Contexto.INICIO_SESION).isPresent());
        assertTrue(s.desafios.consumir(d3.id(), "c1", Contexto.INICIO_SESION).isEmpty());
    }

    @Test
    void desafioVence() throws Exception {
        Soporte s = new Soporte();
        Desafio d = s.desafios.emitir("c1", Contexto.INICIO_SESION);
        s.reloj.avanzar(Duration.ofSeconds(121));
        assertTrue(s.desafios.consumir(d.id(), "c1", Contexto.INICIO_SESION).isEmpty());
    }

    @Test
    void firmaCorrectaDevuelveCuentaYNonceNoSeReutiliza() throws Exception {
        Soporte s = new Soporte();
        KeyPair par = Soporte.claveP256();
        String disp = s.dispositivos.vincular(USUARIO, par.getPublic()).getId();
        String nonce = s.dispositivos.emitirNonce(disp);
        String firma = Soporte.firmar(par, nonce, disp, "INICIO_SESION");
        assertEquals(USUARIO, s.dispositivos.verificarFirma(disp, nonce, firma, "INICIO_SESION"));
        assertThrows(SecurityException.class, () -> s.dispositivos.verificarFirma(disp, nonce, firma, "INICIO_SESION"));
    }

    @Test
    void firmaConOtraClaveOtroPropositoONonceVencidoSeRechaza() throws Exception {
        Soporte s = new Soporte();
        KeyPair par = Soporte.claveP256();
        KeyPair otra = Soporte.claveP256();
        String disp = s.dispositivos.vincular(USUARIO, par.getPublic()).getId();

        String n1 = s.dispositivos.emitirNonce(disp);
        String f1 = Soporte.firmar(otra, n1, disp, "INICIO_SESION");
        assertThrows(SecurityException.class, () -> s.dispositivos.verificarFirma(disp, n1, f1, "INICIO_SESION"));

        String n2 = s.dispositivos.emitirNonce(disp);
        String f2 = Soporte.firmar(par, n2, disp, "OTRO");
        assertThrows(SecurityException.class, () -> s.dispositivos.verificarFirma(disp, n2, f2, "INICIO_SESION"));

        String n3 = s.dispositivos.emitirNonce(disp);
        String f3 = Soporte.firmar(par, n3, disp, "INICIO_SESION");
        s.reloj.avanzar(Duration.ofSeconds(61));
        assertThrows(SecurityException.class, () -> s.dispositivos.verificarFirma(disp, n3, f3, "INICIO_SESION"));
    }

    @Test
    void vincularOtroDispositivoRevocaElAnterior() throws Exception {
        Soporte s = new Soporte();
        String viejo = s.dispositivos.vincular(USUARIO, Soporte.claveP256().getPublic()).getId();
        s.dispositivos.vincular(USUARIO, Soporte.claveP256().getPublic());
        assertThrows(SecurityException.class, () -> s.dispositivos.emitirNonce(viejo));
    }

    @Test
    void soloSeAceptanClavesP256() throws Exception {
        Soporte s = new Soporte();
        KeyPairGenerator g = KeyPairGenerator.getInstance("EC");
        g.initialize(new ECGenParameterSpec("secp384r1"));
        String p384 = Base64.getEncoder().encodeToString(g.generateKeyPair().getPublic().getEncoded());
        assertEquals("CLAVE_DISPOSITIVO_INVALIDA",
                assertThrows(NayraException.class, () -> s.dispositivos.leerClavePublica(p384)).getCodigo());
        assertThrows(NayraException.class, () -> s.dispositivos.leerClavePublica("no-es-base64!"));
        assertThrows(NayraException.class, () -> s.dispositivos.leerClavePublica(null));
    }

    @Test
    void pinSoloSeGuardaComoHash() throws Exception {
        Soporte s = new Soporte();
        String hash = s.pines.hashear(Soporte.PIN);
        assertFalse(hash.contains(Soporte.PIN));
        assertTrue(s.pines.coincide(Soporte.PIN, hash));
        assertFalse(s.pines.coincide("000000", hash));
        assertFalse(s.pines.formatoValido("12345"));
        assertFalse(s.pines.formatoValido("12345a"));
        assertThrows(IllegalArgumentException.class, () -> s.pines.hashear("1234"));
    }
}
