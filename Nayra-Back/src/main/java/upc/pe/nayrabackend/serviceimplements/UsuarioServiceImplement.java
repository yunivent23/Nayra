package upc.pe.nayrabackend.serviceimplements;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import upc.pe.nayrabackend.dtos.UsuarioDTOs.DatosPropios;
import upc.pe.nayrabackend.entities.Auditoria;
import upc.pe.nayrabackend.entities.Usuario;
import upc.pe.nayrabackend.excepciones.NayraException;
import upc.pe.nayrabackend.repositories.IDispositivosRepository;
import upc.pe.nayrabackend.repositories.IUsuariosRepository;
import upc.pe.nayrabackend.serviceinterfaces.IAuditoriaService;
import upc.pe.nayrabackend.serviceinterfaces.ICredencialesService;
import upc.pe.nayrabackend.serviceinterfaces.ISesionesService;
import upc.pe.nayrabackend.serviceinterfaces.IUsuarioService;

import java.time.Clock;
import java.time.Instant;

@Service
public class UsuarioServiceImplement implements IUsuarioService {

    private final IUsuariosRepository usuarios;
    private final IDispositivosRepository dispositivos;
    private final ISesionesService sesiones;
    private final ICredencialesService credenciales;
    private final IAuditoriaService auditoria;
    private final Clock reloj;

    public UsuarioServiceImplement(IUsuariosRepository usuarios, IDispositivosRepository dispositivos,
                                   ISesionesService sesiones, ICredencialesService credenciales,
                                   IAuditoriaService auditoria, Clock reloj) {
        this.usuarios = usuarios;
        this.dispositivos = dispositivos;
        this.sesiones = sesiones;
        this.credenciales = credenciales;
        this.auditoria = auditoria;
        this.reloj = reloj;
    }

    @Override
    public Usuario obtener(String usuarioId) {
        return usuarios.porId(usuarioId).orElseThrow(() -> NayraException.noEncontrado("USUARIO_NO_ENCONTRADO"));
    }

    @Override
    public DatosPropios datosPropios(String usuarioId) {
        Usuario u = obtener(usuarioId);
        boolean dispositivoActivo = dispositivos.activoDeUsuario(usuarioId).isPresent();
        return new DatosPropios(u.getNombres(), u.getApellidos(), u.getCelular(), u.getRol().name(), u.getEstado().name(),
                dispositivoActivo);
    }

    @Override
    @Transactional
    public void bloquear(String usuarioId, String actorId, String motivo) {
        Usuario u = obtener(usuarioId);
        u.bloquear(Instant.now(reloj));
        usuarios.guardar(u);
        sesiones.revocarDeUsuario(usuarioId);
        auditoria.registrar(actorId == null ? "CUENTA_BLOQUEADA" : "ADMIN_BLOQUEO_CUENTA",
                Auditoria.Resultado.EXITOSO, actorId, usuarioId, motivo, null);
    }

    @Override
    @Transactional
    public void desbloquear(String usuarioId, String actorId) {
        Usuario u = obtener(usuarioId);
        u.desbloquear(Instant.now(reloj));
        usuarios.guardar(u);
        // PROVISIONAL (P-11): Negocio avisa a Autenticación, dueña del contador, para reiniciarlo.
        credenciales.reiniciarIntentos(usuarioId);
        auditoria.exito("ADMIN_DESBLOQUEO_CUENTA", actorId, usuarioId);
    }
}
