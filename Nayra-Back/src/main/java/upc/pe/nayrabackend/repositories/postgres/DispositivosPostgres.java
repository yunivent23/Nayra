package upc.pe.nayrabackend.repositories.postgres;

import org.springframework.stereotype.Repository;
import upc.pe.nayrabackend.entities.Dispositivos;
import upc.pe.nayrabackend.entities.Identificadores;
import upc.pe.nayrabackend.repositories.IDispositivosRepository;

import java.util.Optional;

/**
 * Adaptador PostgreSQL de DISPOSITIVOS (nayra.dispositivos, D-051). Cada guardado se escribe de inmediato, así
 * la revocación del dispositivo anterior llega a la base antes de insertar el nuevo (índice único parcial, D-039).
 */
@Repository
public class DispositivosPostgres implements IDispositivosRepository {

    private final DispositivoJpaRepository jpa;

    DispositivosPostgres(DispositivoJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public void guardar(Dispositivos dispositivo) { jpa.saveAndFlush(dispositivo); }

    @Override
    public Optional<Dispositivos> porId(String id) { return Identificadores.leer(id).flatMap(jpa::findById); }

    @Override
    public Optional<Dispositivos> activoDeUsuario(String usuarioId) {
        return Identificadores.leer(usuarioId).flatMap(u -> jpa.findByUsuarioIdAndEstado(u, Dispositivos.Estado.ACTIVO));
    }
}
