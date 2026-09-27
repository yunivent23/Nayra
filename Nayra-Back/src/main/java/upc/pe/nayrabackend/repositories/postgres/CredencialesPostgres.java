package upc.pe.nayrabackend.repositories.postgres;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import upc.pe.nayrabackend.entities.Credenciales;
import upc.pe.nayrabackend.entities.Identificadores;
import upc.pe.nayrabackend.repositories.ICredencialesRepository;

import java.time.Instant;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.UUID;

/**
 * Adaptador PostgreSQL de CREDENCIALES (nayra.credenciales, v4 §2.2). El alta guarda la entidad; el contador solo
 * cambia con sentencias UPDATE atómicas (E-01), nunca guardando una copia leída antes.
 */
@Repository
public class CredencialesPostgres implements ICredencialesRepository {

    private final CredencialJpaRepository jpa;

    CredencialesPostgres(CredencialJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public void guardar(Credenciales credencial) { jpa.saveAndFlush(credencial); }

    @Override
    public Optional<Credenciales> deUsuario(String usuarioId) {
        return Identificadores.leer(usuarioId).flatMap(jpa::findByUsuarioId);
    }

    /**
     * El UPDATE y la lectura van en la misma transacción: el UPDATE deja la fila bloqueada hasta el commit, así que la
     * lectura devuelve el valor que dejó este intento y ningún otro intento puede intercalarse.
     */
    @Override
    @Transactional
    public OptionalInt registrarFallo(String usuarioId, int maximo, Instant ahora) {
        Optional<UUID> id = Identificadores.leer(usuarioId);
        if (id.isEmpty()) {
            return OptionalInt.empty();
        }
        jpa.incrementarIntentos(id.get(), maximo, ahora);
        return jpa.intentos(id.get()).map(OptionalInt::of).orElseGet(OptionalInt::empty);
    }

    @Override
    @Transactional
    public void reiniciarIntentos(String usuarioId, Instant ahora) {
        Identificadores.leer(usuarioId).ifPresent(id -> jpa.reiniciarIntentos(id, ahora));
    }
}
