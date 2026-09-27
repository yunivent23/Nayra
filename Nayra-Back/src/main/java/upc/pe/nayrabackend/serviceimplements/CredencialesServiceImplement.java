package upc.pe.nayrabackend.serviceimplements;

import org.springframework.stereotype.Service;
import upc.pe.nayrabackend.entities.Credenciales;
import upc.pe.nayrabackend.entities.Identificadores;
import upc.pe.nayrabackend.excepciones.NayraException;
import upc.pe.nayrabackend.repositories.ICredencialesRepository;
import upc.pe.nayrabackend.serviceinterfaces.ICredencialesService;
import upc.pe.nayrabackend.serviceinterfaces.IPinService;

import java.time.Clock;
import java.time.Instant;

/**
 * Credenciales del PIN (servicio de autenticación, v4 §2.2). Hash con {@link IPinService}: BCrypt PROVISIONAL
 * mientras D-047 siga pendiente.
 */
@Service
public class CredencialesServiceImplement implements ICredencialesService {

    private final ICredencialesRepository credenciales;
    private final IPinService pines;
    private final Clock reloj;

    public CredencialesServiceImplement(ICredencialesRepository credenciales, IPinService pines, Clock reloj) {
        this.credenciales = credenciales;
        this.pines = pines;
        this.reloj = reloj;
    }

    @Override
    public void crear(String usuarioId, String pinHash) {
        credenciales.guardar(new Credenciales(Identificadores.nuevo(), usuarioId, pinHash, Instant.now(reloj)));
    }

    /**
     * El hash se lee de la entidad, pero el contador nunca se guarda desde una copia leída antes: el fallo y el
     * reinicio son sentencias atómicas del repositorio (E-01), así que dos PIN incorrectos simultáneos cuentan como dos.
     */
    @Override
    public ResultadoPin verificar(String usuarioId, String pin) {
        Credenciales c = credencial(usuarioId);
        if (pines.coincide(pin, c.getPinHash())) {
            credenciales.reiniciarIntentos(usuarioId, Instant.now(reloj));
            return new ResultadoPin(true, INTENTOS_MAXIMOS, false);
        }
        int fallos = credenciales.registrarFallo(usuarioId, INTENTOS_MAXIMOS, Instant.now(reloj))
                .orElseThrow(() -> NayraException.noEncontrado("CREDENCIAL_NO_ENCONTRADA"));
        return new ResultadoPin(false, INTENTOS_MAXIMOS - fallos, fallos >= INTENTOS_MAXIMOS);
    }

    @Override
    public int intentosRestantes(String usuarioId) {
        return INTENTOS_MAXIMOS - credencial(usuarioId).getIntentosFallidos();
    }

    @Override
    public void reiniciarIntentos(String usuarioId) {
        credenciales.reiniciarIntentos(usuarioId, Instant.now(reloj));
    }

    private Credenciales credencial(String usuarioId) {
        return credenciales.deUsuario(usuarioId).orElseThrow(() -> NayraException.noEncontrado("CREDENCIAL_NO_ENCONTRADA"));
    }
}
