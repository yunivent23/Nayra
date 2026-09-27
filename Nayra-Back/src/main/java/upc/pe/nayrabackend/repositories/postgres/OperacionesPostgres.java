package upc.pe.nayrabackend.repositories.postgres;

import org.springframework.stereotype.Repository;
import upc.pe.nayrabackend.entities.Identificadores;
import upc.pe.nayrabackend.entities.Operaciones;
import upc.pe.nayrabackend.repositories.IOperacionesRepository;

import java.util.Optional;

/**
 * Adaptador PostgreSQL de OPERACIONES (nayra.operaciones, v4 §2.8). Cada guardado se escribe de inmediato, así un
 * código de referencia repetido se detecta al guardar (UNIQUE) y el llamador puede reintentar con otro.
 */
@Repository
public class OperacionesPostgres implements IOperacionesRepository {

    private final OperacionJpaRepository jpa;

    OperacionesPostgres(OperacionJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public void guardar(Operaciones operacion) { jpa.saveAndFlush(operacion); }

    @Override
    public Optional<Operaciones> porId(String id) { return Identificadores.leer(id).flatMap(jpa::findById); }

    @Override
    public Optional<Operaciones> porCodigoReferencia(String codigoReferencia) {
        return codigoReferencia == null ? Optional.empty() : jpa.findByCodigoReferencia(codigoReferencia);
    }
}
