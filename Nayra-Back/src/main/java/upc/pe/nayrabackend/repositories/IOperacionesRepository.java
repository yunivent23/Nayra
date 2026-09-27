package upc.pe.nayrabackend.repositories;

import upc.pe.nayrabackend.entities.Operaciones;

import java.util.Optional;

/** Puerto de persistencia de OPERACIONES simuladas (servicio de negocio, v4 §2.8). Adaptador: PostgreSQL. */
public interface IOperacionesRepository {
    void guardar(Operaciones operacion);
    Optional<Operaciones> porId(String id);
    Optional<Operaciones> porCodigoReferencia(String codigoReferencia);
}
