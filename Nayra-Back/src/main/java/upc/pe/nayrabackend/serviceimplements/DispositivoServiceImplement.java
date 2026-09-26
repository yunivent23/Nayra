package upc.pe.nayrabackend.serviceimplements;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import upc.pe.nayrabackend.entities.DesafioAutenticacion;
import upc.pe.nayrabackend.entities.Dispositivos;
import upc.pe.nayrabackend.entities.Users;
import upc.pe.nayrabackend.exceptions.CodigoError;
import upc.pe.nayrabackend.exceptions.NayraException;
import upc.pe.nayrabackend.repositories.IDispositivosRepositoritory;
import upc.pe.nayrabackend.repositories.ISesionesRepository;
import upc.pe.nayrabackend.serviceinterfaces.IDesafioService;
import upc.pe.nayrabackend.serviceinterfaces.IDispositivoService;
import upc.pe.nayrabackend.serviceinterfaces.IFirmaDispositivoService;
import upc.pe.nayrabackend.serviceinterfaces.ISesionesService;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class DispositivoServiceImplement implements IDispositivoService {

    private static final Set<String> PLATAFORMAS = Set.of("ANDROID", "IOS");

    private final IDispositivosRepositoritory repository;
    private final ISesionesRepository sesiones;
    private final IDesafioService desafios;
    private final IFirmaDispositivoService firmas;
    private final Clock reloj;

    public DispositivoServiceImplement(IDispositivosRepositoritory repository, ISesionesRepository sesiones,
                                       IDesafioService desafios, IFirmaDispositivoService firmas, Clock reloj) {
        this.repository = repository;
        this.sesiones = sesiones;
        this.desafios = desafios;
        this.firmas = firmas;
        this.reloj = reloj;
    }

    @Override
    @Transactional
    public Dispositivos registrar(Users usuario, String clavePublica, String plataforma, String nombre,
                                  UUID desafioId, String firma) {
        if (!PLATAFORMAS.contains(plataforma)) {
            throw new IllegalArgumentException("Plataforma no soportada");
        }
        String clave = firmas.validarClavePublica(clavePublica);
        DesafioAutenticacion desafio = desafios.consumir(desafioId, IDesafioService.REGISTRO_DISPOSITIVO);
        if (desafio.getUsuario() == null || !desafio.getUsuario().getId().equals(usuario.getId())) {
            throw new NayraException(CodigoError.DESAFIO_INVALIDO);
        }
        String mensaje = firmas.mensajeCanonico(IDesafioService.REGISTRO_DISPOSITIVO, desafio.getNonce(), null);
        if (!firmas.verificar(clave, mensaje, firma)) {
            throw new NayraException(CodigoError.AUTENTICACION_FALLIDA);
        }

        OffsetDateTime ahora = OffsetDateTime.now(reloj);
        revocarActivo(usuario, ahora);

        Dispositivos d = new Dispositivos();
        d.setIdPublico(UUID.randomUUID());
        d.setUsuario(usuario);
        d.setPlataforma(plataforma);
        d.setNombre(nombre);
        d.setClavePublica(clave);
        d.setAlgoritmo(IFirmaDispositivoService.ALGORITMO);
        d.setEstado(ACTIVO);
        d.setFechaRegistro(ahora);
        return repository.save(d);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Dispositivos> buscarActivo(UUID idPublico) {
        return repository.findByIdPublicoAndEstado(idPublico, ACTIVO);
    }

    @Override
    @Transactional
    public void revocar(Users usuario) {
        revocarActivo(usuario, OffsetDateTime.now(reloj));
    }

    private void revocarActivo(Users usuario, OffsetDateTime ahora) {
        if (repository.revocarActivos(usuario.getId(), ahora) > 0) {
            sesiones.cerrarTodasDelUsuario(usuario.getId(), ISesionesService.DISPOSITIVO_REVOCADO, ahora);
        }
    }
}
