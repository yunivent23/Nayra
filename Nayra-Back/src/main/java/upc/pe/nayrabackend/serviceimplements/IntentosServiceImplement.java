package upc.pe.nayrabackend.serviceimplements;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import upc.pe.nayrabackend.config.NayraSeguridadProperties;
import upc.pe.nayrabackend.entities.Users;
import upc.pe.nayrabackend.repositories.ISesionesRepository;
import upc.pe.nayrabackend.repositories.IUsersRepository;
import upc.pe.nayrabackend.serviceinterfaces.IIntentosService;

import java.time.Clock;
import java.time.OffsetDateTime;

@Service
public class IntentosServiceImplement implements IIntentosService {

    private final IUsersRepository usuarios;
    private final ISesionesRepository sesiones;
    private final NayraSeguridadProperties propiedades;
    private final Clock reloj;

    public IntentosServiceImplement(IUsersRepository usuarios, ISesionesRepository sesiones,
                                    NayraSeguridadProperties propiedades, Clock reloj) {
        this.usuarios = usuarios;
        this.sesiones = sesiones;
        this.propiedades = propiedades;
        this.reloj = reloj;
    }

    // REQUIRES_NEW: el fallo queda registrado aunque la autenticación termine con una excepción
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public int registrarFallo(Users usuario) {
        usuarios.incrementarIntentosFallidos(usuario.getId());
        Users actual = usuarios.findById(usuario.getId()).orElseThrow();
        int maximo = propiedades.intentos().maximo();
        if (actual.getIntentosFallidos() >= maximo && !actual.getBloqueado()) {
            OffsetDateTime ahora = OffsetDateTime.now(reloj);
            actual.setBloqueado(true);
            actual.setFechaBloqueo(ahora);
            usuarios.save(actual);
            sesiones.cerrarTodasDelUsuario(actual.getId(), "BLOQUEO", ahora);
        }
        return Math.max(0, maximo - actual.getIntentosFallidos());
    }

    @Override
    @Transactional
    public void registrarExito(Users usuario) {
        usuarios.findById(usuario.getId()).ifPresent(u -> {
            u.setIntentosFallidos(0);
            usuarios.save(u);
        });
    }
}
