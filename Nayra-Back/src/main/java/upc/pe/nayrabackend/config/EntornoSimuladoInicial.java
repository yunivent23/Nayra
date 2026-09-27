package upc.pe.nayrabackend.config;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import upc.pe.nayrabackend.entities.Cuentas;
import upc.pe.nayrabackend.entities.EntidadBancaria;
import upc.pe.nayrabackend.entities.RegistroIdentidadSimulado;
import upc.pe.nayrabackend.entities.TipoDocumentoIdentidad;
import upc.pe.nayrabackend.repositories.ICuentasRepository;
import upc.pe.nayrabackend.repositories.IEntidadesBancariasRepository;
import upc.pe.nayrabackend.repositories.IRegistroIdentidadRepository;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;

/**
 * Carga los datos FICTICIOS del entorno bancario simulado (D-021, D-035) al arrancar.
 * La carga es idempotente: solo inserta lo que falta (por id de la entidad, documento del registro y titular de la
 * cuenta) y nunca sobrescribe, de modo que al reiniciar no se pierden el saldo ni la vinculación de las cuentas.
 * PROVISIONAL: la carga de datos de referencia por migración no está decidida; se mantiene este cargador.
 */
@Component
public class EntornoSimuladoInicial {

    static final String RECURSO = "entorno-simulado/datos-ficticios.json";

    public EntornoSimuladoInicial(IEntidadesBancariasRepository entidades, IRegistroIdentidadRepository identidades,
                                  ICuentasRepository cuentas, Clock reloj) throws IOException {
        JsonNode datos;
        try (InputStream in = new ClassPathResource(RECURSO).getInputStream()) {
            datos = JsonMapper.builder().build().readTree(in);
        }
        Instant ahora = Instant.now(reloj);
        datos.get("entidadesBancarias").forEach(e -> {
            if (entidades.porId(e.get("id").asString()).isEmpty()) {
                entidades.guardar(new EntidadBancaria(e.get("id").asString(), e.get("nombre").asString()));
            }
        });
        datos.get("registroIdentidad").forEach(r -> {
            TipoDocumentoIdentidad tipo = TipoDocumentoIdentidad.valueOf(r.get("tipoDocumentoIdentidad").asString());
            String numero = r.get("numeroDocumento").asString();
            if (identidades.porDocumento(tipo, numero).isEmpty()) {
                identidades.guardar(new RegistroIdentidadSimulado(r.get("id").asString(), tipo, numero,
                        r.get("nombres").asString(), r.get("apellidos").asString()));
            }
        });
        datos.get("cuentas").forEach(c -> {
            if (cuentas.porTitular(c.get("titularId").asString()).isEmpty()) {
                cuentas.guardar(new Cuentas(c.get("id").asString(), c.get("titularId").asString(),
                        c.get("entidadBancariaId").asString(), c.get("codigoCuenta").asString(),
                        new BigDecimal(c.get("saldo").asString()), c.get("moneda").asString(), ahora));
            }
        });
    }
}
