package upc.pe.nayrabackend.general;

import org.junit.jupiter.api.Test;
import upc.pe.nayrabackend.entities.DocumentoIdentidad;
import upc.pe.nayrabackend.entities.Identificadores;
import upc.pe.nayrabackend.entities.Notificaciones;
import upc.pe.nayrabackend.entities.Operaciones;
import upc.pe.nayrabackend.entities.TipoDocumentoIdentidad;
import upc.pe.nayrabackend.excepciones.NayraException;
import upc.pe.nayrabackend.soporte.Soporte;
import upc.pe.nayrabackend.soporte.Soporte.Persona;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

/** Reglas del modelo de datos v4 que viven en las entidades y en el registro, sin base de datos. */
class ModeloDatosV4Test {

    @Test
    void documentoDniOCeConFormatoProvisional() {
        assertEquals(new DocumentoIdentidad(TipoDocumentoIdentidad.DNI, "10000002"), DocumentoIdentidad.leer("DNI", "10000002"));
        assertEquals(TipoDocumentoIdentidad.DNI, DocumentoIdentidad.leer(null, "10000002").tipo(), "Sin tipo: DNI (PROVISIONAL)");
        assertEquals(new DocumentoIdentidad(TipoDocumentoIdentidad.CE, "AB12345"), DocumentoIdentidad.leer("ce", "ab12345"));
        assertEquals("DOCUMENTO_INVALIDO", assertThrows(NayraException.class, () -> DocumentoIdentidad.leer("DNI", "1234")).getCodigo());
        assertEquals("DOCUMENTO_INVALIDO", assertThrows(NayraException.class, () -> DocumentoIdentidad.leer("CE", "12-34")).getCodigo());
        assertEquals("TIPO_DOCUMENTO_INVALIDO", assertThrows(NayraException.class,
                () -> DocumentoIdentidad.leer("PASAPORTE", "123")).getCodigo());
    }

    @Test
    void registroConCarneDeExtranjeria() throws Exception {
        Soporte s = new Soporte();
        Persona admin = s.crearAdministrador();
        s.entorno.guardar(new upc.pe.nayrabackend.entities.RegistroIdentidadSimulado(Identificadores.nuevo(),
                TipoDocumentoIdentidad.CE, "CE000123", "Ana", "Extranjera"));
        // Existe la identidad CE, pero no tiene cuenta financiera en el entorno simulado.
        assertEquals("CUENTA_FINANCIERA_NO_ENCONTRADA", assertThrows(NayraException.class,
                () -> s.registro.iniciar(admin.usuarioId(), "CE", "CE000123")).getCodigo());
        // El mismo número como DNI es otro documento.
        assertEquals("DOCUMENTO_INVALIDO", assertThrows(NayraException.class,
                () -> s.registro.iniciar(admin.usuarioId(), "DNI", "CE000123")).getCodigo());
    }

    @Test
    void operacionRechazaMontoFueraDeRangoYMismaCuenta() {
        String a = Identificadores.nuevo();
        String b = Identificadores.nuevo();
        Instant ahora = Instant.now();
        for (String monto : new String[]{"0", "-0.01", "500", "500.00", "1000"}) {
            assertThrows(IllegalArgumentException.class, () -> new Operaciones(Identificadores.nuevo(), a, b, new BigDecimal(monto),
                    null, "000001", Operaciones.Estado.EXITOSO, ahora), monto);
        }
        assertThrows(IllegalArgumentException.class, () -> new Operaciones(Identificadores.nuevo(), a, a, BigDecimal.ONE, null,
                "000001", Operaciones.Estado.EXITOSO, ahora));
        Operaciones o = new Operaciones(Identificadores.nuevo(), a, b, new BigDecimal("499.99"), null, "000001",
                Operaciones.Estado.EXITOSO, ahora);
        assertEquals("PEN", o.getMoneda());
        assertEquals(Operaciones.Canal.MOVIL, o.getCanal());
    }

    /** E-03: el monto se valida en la aplicación con la definición de v4 §2.8 (numeric(15,2), > 0 y < 500). */
    @Test
    void montoConMasDeDosDecimalesSeRechazaSinRedondear() {
        String a = Identificadores.nuevo();
        String b = Identificadores.nuevo();
        Instant ahora = Instant.now();
        // Escala superior a 2: se rechaza en lugar de que PostgreSQL lo redondee (0.005 → 0.01, 499.995 → 500.00).
        for (String monto : new String[]{"0.001", "0.005", "150.005", "499.991", "499.995", "1.234567"}) {
            assertEquals("MONTO_ESCALA_INVALIDA", assertThrows(IllegalArgumentException.class, () -> new Operaciones(
                    Identificadores.nuevo(), a, b, new BigDecimal(monto), null, "000001", Operaciones.Estado.EXITOSO, ahora))
                    .getMessage(), monto);
        }
        // Fuera del rango (> 0 y < 500), con escala válida.
        for (String monto : new String[]{"0", "0.00", "-0.01", "500", "500.00", "1000", "12345678901234.00"}) {
            assertEquals("MONTO_FUERA_DE_RANGO", assertThrows(IllegalArgumentException.class, () -> new Operaciones(
                    Identificadores.nuevo(), a, b, new BigDecimal(monto), null, "000001", Operaciones.Estado.EXITOSO, ahora))
                    .getMessage(), monto);
        }
        assertEquals("MONTO_FUERA_DE_RANGO", assertThrows(IllegalArgumentException.class,
                () -> Operaciones.validarMonto(null)).getMessage());
        // Válidos, incluidos los límites 0.01 y 499.99; se guardan con escala 2 sin cambiar el valor.
        String[][] validos = {{"0.01", "0.01"}, {"499.99", "499.99"}, {"150", "150.00"}, {"150.5", "150.50"}, {"150.50", "150.50"},
                {"150.000", "150.00"}, {"1E+2", "100.00"}};
        for (String[] v : validos) {
            Operaciones o = new Operaciones(Identificadores.nuevo(), a, b, new BigDecimal(v[0]), null, "000001",
                    Operaciones.Estado.EXITOSO, ahora);
            assertEquals(new BigDecimal(v[1]), o.getMonto(), v[0]);
            assertEquals(2, o.getMonto().scale(), v[0]);
        }
    }

    @Test
    void codigoDeReferenciaSiempreDeSeisDigitos() {
        SecureRandom r = new SecureRandom();
        for (int i = 0; i < 1000; i++) {
            assertTrue(Operaciones.nuevoCodigoReferencia(r).matches("\\d{6}"));
        }
    }

    @Test
    void textoDeLaNotificacion() {
        assertEquals("Juan Per... te realizó una transferencia de S/ 150.00.",
                Notificaciones.contenidoTransferencia("Juan", "Perez", new BigDecimal("150")));
        assertEquals("María José Ñañ... te realizó una transferencia de S/ 0.50.",
                Notificaciones.contenidoTransferencia("María José", "Ñañez Díaz", new BigDecimal("0.5")));
        assertEquals("Li Wu... te realizó una transferencia de S/ 499.99.",
                Notificaciones.contenidoTransferencia("Li", "Wu", new BigDecimal("499.99")));
    }
}
