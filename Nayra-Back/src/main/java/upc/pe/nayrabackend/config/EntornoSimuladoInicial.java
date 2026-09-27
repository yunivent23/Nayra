package upc.pe.nayrabackend.config;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import upc.pe.nayrabackend.entities.Cuentas;
import upc.pe.nayrabackend.entities.EntidadBancaria;
import upc.pe.nayrabackend.entities.RegistroIdentidadSimulado;
import upc.pe.nayrabackend.repositories.ICuentasRepository;
import upc.pe.nayrabackend.repositories.IEntidadesBancariasRepository;
import upc.pe.nayrabackend.repositories.IRegistroIdentidadRepository;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * Carga los datos FICTICIOS del entorno bancario simulado (D-021, D-035) al arrancar.
 * PROVISIONAL (D-051): cuando existan tablas, estos datos de referencia se cargarán con la
 * estrategia de migraciones que se decida.
 */
@Component
public class EntornoSimuladoInicial {

    static final String RECURSO = "entorno-simulado/datos-ficticios.json";

    public EntornoSimuladoInicial(IEntidadesBancariasRepository entidades, IRegistroIdentidadRepository identidades,
                                  ICuentasRepository cuentas) throws IOException {
        JsonNode datos;
        try (InputStream in = new ClassPathResource(RECURSO).getInputStream()) {
            datos = JsonMapper.builder().build().readTree(in);
        }
        datos.get("entidadesBancarias").forEach(e ->
                entidades.guardar(new EntidadBancaria(e.get("id").asString(), e.get("nombre").asString())));
        datos.get("registroIdentidad").forEach(r -> identidades.guardar(new RegistroIdentidadSimulado(
                r.get("dni").asString(), r.get("nombres").asString(), r.get("apellidos").asString())));
        datos.get("cuentas").forEach(c -> cuentas.guardar(new Cuentas(UUID.randomUUID().toString(),
                c.get("titularDni").asString(), c.get("entidadBancariaId").asString(), c.get("identificador").asString(),
                new BigDecimal(c.get("saldo").asString()), c.get("moneda").asString())));
    }
}
