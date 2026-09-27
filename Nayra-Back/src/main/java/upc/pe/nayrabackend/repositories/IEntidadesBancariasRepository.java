package upc.pe.nayrabackend.repositories;

import upc.pe.nayrabackend.entities.EntidadBancaria;

import java.util.Optional;

/** Catálogo ENTIDADES_BANCARIAS (D-026). Adaptador: PostgreSQL (D-051). */
public interface IEntidadesBancariasRepository {
    void guardar(EntidadBancaria entidad);
    Optional<EntidadBancaria> porId(String id);
}
