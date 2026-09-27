package upc.pe.nayrabackend.repositories;

import upc.pe.nayrabackend.entities.Auditoria;
import upc.pe.nayrabackend.entities.Auditoria.Resultado;

import java.util.List;

/** Puerto de persistencia de AUDITORÍA (solo inserción y consulta). Adaptador: PostgreSQL (D-051). */
public interface IAuditoriaRepository {
    void guardar(Auditoria evento);

    /**
     * Eventos del más reciente al más antiguo. Cada filtro nulo no se aplica.
     *
     * @param usuarioId     eventos en los que el usuario es actor o afectado
     * @param accionPrefijo acciones que empiezan por este prefijo
     */
    List<Auditoria> consultar(String usuarioId, String accionPrefijo, Resultado resultado);
}
