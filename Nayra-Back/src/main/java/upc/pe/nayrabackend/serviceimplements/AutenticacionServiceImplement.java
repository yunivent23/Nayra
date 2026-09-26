package upc.pe.nayrabackend.serviceimplements;

import org.springframework.stereotype.Service;
import upc.pe.nayrabackend.clients.VozClient;
import upc.pe.nayrabackend.config.NayraUmbralesProperties;
import upc.pe.nayrabackend.dtos.ResultadoVozDTO;
import upc.pe.nayrabackend.entities.DesafioAutenticacion;
import upc.pe.nayrabackend.entities.Dispositivos;
import upc.pe.nayrabackend.entities.Users;
import upc.pe.nayrabackend.exceptions.CodigoError;
import upc.pe.nayrabackend.exceptions.NayraException;
import upc.pe.nayrabackend.repositories.IUsersRepository;
import upc.pe.nayrabackend.serviceinterfaces.IAutenticacionService;
import upc.pe.nayrabackend.serviceinterfaces.IDesafioService;
import upc.pe.nayrabackend.serviceinterfaces.IDispositivoService;
import upc.pe.nayrabackend.serviceinterfaces.IFirmaDispositivoService;
import upc.pe.nayrabackend.serviceinterfaces.IIntentosService;
import upc.pe.nayrabackend.serviceinterfaces.IPinService;
import upc.pe.nayrabackend.serviceinterfaces.ISesionesService;

import java.util.UUID;

/**
 * Orquesta el inicio de sesión aprobado (D-046): desafío vigente → firma del dispositivo activo → PIN → voz.
 * Python devuelve puntajes; aquí se aplican los umbrales configurables (D-038) y se decide.
 */
@Service
public class AutenticacionServiceImplement implements IAutenticacionService {

    private final IDesafioService desafios;
    private final IDispositivoService dispositivos;
    private final IFirmaDispositivoService firmas;
    private final IPinService pines;
    private final IIntentosService intentos;
    private final ISesionesService sesiones;
    private final IUsersRepository usuarios;
    private final VozClient voz;
    private final NayraUmbralesProperties umbrales;

    public AutenticacionServiceImplement(IDesafioService desafios, IDispositivoService dispositivos,
                                         IFirmaDispositivoService firmas, IPinService pines,
                                         IIntentosService intentos, ISesionesService sesiones,
                                         IUsersRepository usuarios, VozClient voz,
                                         NayraUmbralesProperties umbrales) {
        this.desafios = desafios;
        this.dispositivos = dispositivos;
        this.firmas = firmas;
        this.pines = pines;
        this.intentos = intentos;
        this.sesiones = sesiones;
        this.usuarios = usuarios;
        this.voz = voz;
        this.umbrales = umbrales;
    }

    @Override
    public DesafioAutenticacion emitirDesafioLogin(UUID dispositivoId) {
        // Si el dispositivo no existe se emite igualmente un desafío que fallará: no permite enumerar dispositivos
        Dispositivos d = dispositivos.buscarActivo(dispositivoId).orElse(null);
        return desafios.emitir(IDesafioService.LOGIN, d == null ? null : d.getUsuario(), d, true);
    }

    @Override
    public String autenticar(UUID desafioId, String firma, String pin, byte[] audio) {
        DesafioAutenticacion desafio = desafios.consumir(desafioId, IDesafioService.LOGIN);
        if (desafio.getDispositivo() == null || desafio.getUsuario() == null) {
            throw new NayraException(CodigoError.AUTENTICACION_FALLIDA);
        }
        Dispositivos dispositivo = dispositivos.buscarActivo(desafio.getDispositivo().getIdPublico())
                .orElseThrow(() -> new NayraException(CodigoError.AUTENTICACION_FALLIDA));
        Users usuario = usuarios.findById(desafio.getUsuario().getId())
                .orElseThrow(() -> new NayraException(CodigoError.AUTENTICACION_FALLIDA));

        // 1. Firma del dispositivo
        String mensaje = firmas.mensajeCanonico(IDesafioService.LOGIN, desafio.getNonce(),
                dispositivo.getIdPublico().toString());
        if (!firmas.verificar(dispositivo.getClavePublica(), mensaje, firma)) {
            // No cuenta para el bloqueo (D-047 cuenta PIN y voz): evita que un tercero bloquee la cuenta sin la clave
            throw new NayraException(CodigoError.AUTENTICACION_FALLIDA);
        }
        if (Boolean.TRUE.equals(usuario.getBloqueado()) || !Boolean.TRUE.equals(usuario.getEnabled())) {
            throw new NayraException(CodigoError.CUENTA_BLOQUEADA);
        }

        // 2. PIN
        if (!pines.verificar(pin, usuario.getPinHash())) {
            fallo(usuario);
        }

        // 3. Voz: sin umbrales aprobados no se autentica (modo calibración, D-038)
        if (!umbrales.completosParaVerificacion()) {
            throw new NayraException(CodigoError.CALIBRACION);
        }
        ResultadoVozDTO r = voz.verificar(usuario.getId(), desafio.getElementos(), audio);
        evaluarVoz(r, usuario);

        intentos.registrarExito(usuario);
        return sesiones.crear(usuario, dispositivo);
    }

    private void evaluarVoz(ResultadoVozDTO r, Users usuario) {
        ResultadoVozDTO.Calidad c = r.calidad();
        if (c == null || menor(c.vozNetaS(), umbrales.vozNetaMinimaSegundos())
                || menor(c.snrDb(), umbrales.snrMinimoDb())
                || mayor(c.saturacion(), umbrales.saturacionMaxima())) {
            throw new NayraException(CodigoError.CALIDAD_INSUFICIENTE);
        }
        ResultadoVozDTO.Contenido t = r.contenido();
        if (t == null || !Boolean.TRUE.equals(t.coincide()) || menor(t.confianza(), umbrales.confianzaContenidoMinima())) {
            throw new NayraException(CodigoError.CONTENIDO_INCORRECTO);
        }
        // Anti-spoofing antes que la similitud (05_BIOMETRIA §10): una muestra sospechosa nunca se acepta
        if (r.spoofing() == null || menor(r.spoofing().puntaje(), umbrales.spoofingMinimo())) {
            fallo(usuario);
        }
        ResultadoVozDTO.Biometria b = r.biometria();
        if (b != null && Boolean.TRUE.equals(b.requiereReenrolamiento())) {
            // El modelo cambió de versión (D-039): no es un intento del usuario, debe volver a registrar su voz
            throw new NayraException(CodigoError.AUTENTICACION_FALLIDA);
        }
        if (b == null || !Boolean.TRUE.equals(b.perfilEncontrado()) || menor(b.similitud(), umbrales.similitudMinima())) {
            fallo(usuario);
        }
    }

    private void fallo(Users usuario) {
        int restantes = intentos.registrarFallo(usuario);
        if (restantes == 0) {
            throw new NayraException(CodigoError.CUENTA_BLOQUEADA);
        }
        throw new NayraException(CodigoError.AUTENTICACION_FALLIDA,
                CodigoError.AUTENTICACION_FALLIDA.getMensaje() + " Te quedan " + restantes
                        + (restantes == 1 ? " intento." : " intentos."));
    }

    private static boolean menor(Double valor, Double minimo) {
        return valor == null || valor < minimo;
    }

    private static boolean mayor(Double valor, Double maximo) {
        return valor == null || valor > maximo;
    }
}
