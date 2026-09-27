package upc.pe.nayrabackend.repositories;

import upc.pe.nayrabackend.entities.Auditoria;

import java.util.List;

/** Puerto de persistencia de AUDITORÍA (solo inserción y consulta). Adaptador actual: en memoria (PROVISIONAL, D-051). */
public interface IAuditoriaRepository {
    void guardar(Auditoria evento);
    List<Auditoria> todos();
}
