package upc.pe.nayrabackend.repositories.memoria;

import org.springframework.stereotype.Repository;
import upc.pe.nayrabackend.entities.Auditoria;
import upc.pe.nayrabackend.repositories.IAuditoriaRepository;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/** PROVISIONAL (D-051): solo inserción; los eventos no se modifican ni se eliminan. */
@Repository
public class AuditoriaEnMemoria implements IAuditoriaRepository {

    private final List<Auditoria> eventos = new CopyOnWriteArrayList<>();

    @Override
    public void guardar(Auditoria evento) { eventos.add(evento); }

    @Override
    public List<Auditoria> todos() { return List.copyOf(eventos); }
}
