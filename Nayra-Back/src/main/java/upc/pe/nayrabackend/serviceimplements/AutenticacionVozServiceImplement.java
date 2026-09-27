package upc.pe.nayrabackend.serviceimplements;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import upc.pe.nayrabackend.config.VozProperties;
import upc.pe.nayrabackend.dtos.AutenticacionVozDTOs.DesafioDTO;
import upc.pe.nayrabackend.dtos.AutenticacionVozDTOs.ResultadoPaso;
import upc.pe.nayrabackend.entities.Auditoria.Resultado;
import upc.pe.nayrabackend.entities.Usuario;
import upc.pe.nayrabackend.excepciones.NayraException;
import upc.pe.nayrabackend.repositories.IUsuariosRepository;
import upc.pe.nayrabackend.serviceinterfaces.IAuditoriaService;
import upc.pe.nayrabackend.serviceinterfaces.IAutenticacionVozService;
import upc.pe.nayrabackend.serviceinterfaces.IDesafioService;
import upc.pe.nayrabackend.serviceinterfaces.IDesafioService.Contexto;
import upc.pe.nayrabackend.serviceinterfaces.IDesafioService.Desafio;
import upc.pe.nayrabackend.serviceinterfaces.IDispositivoService;
import upc.pe.nayrabackend.serviceinterfaces.IPinService;
import upc.pe.nayrabackend.serviceinterfaces.IServicioVozCliente;
import upc.pe.nayrabackend.serviceinterfaces.IServicioVozCliente.ResultadoTecnico;
import upc.pe.nayrabackend.serviceinterfaces.IServicioVozCliente.ServicioVozNoDisponibleException;
import upc.pe.nayrabackend.serviceinterfaces.ISesionesService;
import upc.pe.nayrabackend.serviceinterfaces.IUsuarioService;

import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Spring Boot toma la decisión final de autenticación y controla intentos, bloqueo y auditoría (D-056).
 *
 * PROVISIONAL:
 * - Transacciones en memoria (D-051) con vida PROVISIONAL — PENDIENTE DE VALIDACIÓN.
 * - Qué cuenta como intento: {@link PoliticaIntentosProvisional} (detalle de D-044 pendiente).
 * - Al autenticar se crea la sesión con {@link ISesionesService}, cuyo mecanismo es PROVISIONAL (D-018).
 * - El ADMIN usa este mismo flujo (dispositivo + PIN + voz) solo para el prototipo; D-050 sigue pendiente.
 *
 * Usa la cuenta de acceso, el dispositivo y la sesión del backend general; el backend general no depende de AG-13.
 */
@Profile("prototipo")
@Service
public class AutenticacionVozServiceImplement implements IAutenticacionVozService {

    enum Paso { DISPOSITIVO_VERIFICADO, PIN_VERIFICADO, TERMINADA }

    static final class Transaccion {
        final String id;
        final String cuentaId;
        final String dispositivoId;
        final Instant expira;
        Paso paso = Paso.DISPOSITIVO_VERIFICADO;
        String desafioId;

        Transaccion(String id, String cuentaId, String dispositivoId, Instant expira) {
            this.id = id;
            this.cuentaId = cuentaId;
            this.dispositivoId = dispositivoId;
            this.expira = expira;
        }
    }

    private final IDispositivoService dispositivos;
    private final IPinService pines;
    private final IDesafioService desafios;
    private final IServicioVozCliente servicioVoz;
    private final IAuditoriaService auditoria;
    private final PoliticaIntentosProvisional politica;
    private final IUsuariosRepository usuarios;
    private final IUsuarioService usuarioService;
    private final ISesionesService sesiones;
    private final VozProperties props;
    private final Clock reloj;
    private final Map<String, Transaccion> transacciones = new ConcurrentHashMap<>();

    public AutenticacionVozServiceImplement(IDispositivoService dispositivos, IPinService pines, IDesafioService desafios,
                                            IServicioVozCliente servicioVoz, IAuditoriaService auditoria,
                                            PoliticaIntentosProvisional politica, IUsuariosRepository usuarios,
                                            IUsuarioService usuarioService, ISesionesService sesiones,
                                            VozProperties props, Clock reloj) {
        this.dispositivos = dispositivos;
        this.pines = pines;
        this.desafios = desafios;
        this.servicioVoz = servicioVoz;
        this.auditoria = auditoria;
        this.politica = politica;
        this.usuarios = usuarios;
        this.usuarioService = usuarioService;
        this.sesiones = sesiones;
        this.props = props;
        this.reloj = reloj;
    }

    @Override
    public String emitirNonce(String dispositivoId) {
        return dispositivos.emitirNonce(dispositivoId);
    }

    @Override
    public String abrirTransaccion(String dispositivoId, String nonce, String firma) {
        String cuentaId;
        try {
            cuentaId = dispositivos.verificarFirma(dispositivoId, nonce, firma, PROPOSITO_INICIO_SESION);
        } catch (SecurityException e) {
            auditoria.registrar("AUTENTICACION_DISPOSITIVO_RECHAZADO", Resultado.FALLO, null, null, "FIRMA_O_NONCE_INVALIDO",
                    dispositivoId);
            throw e;
        }
        Usuario cuenta = cuenta(cuentaId);
        if (cuenta.getEstado() == Usuario.Estado.BLOQUEADA) {
            auditoria.registrar("AUTENTICACION_RECHAZADA", Resultado.FALLO, cuentaId, cuentaId, "CUENTA_BLOQUEADA", dispositivoId);
            throw NayraException.conflicto("CUENTA_BLOQUEADA");
        }
        // La cuenta de acceso solo existe cuando el registro terminó con la voz enrolada (D-052), así que no
        // hace falta comprobar aquí el enrolamiento: si Python no tiene referencia, responde SIN_REFERENCIA.
        purgarVencidas();
        Transaccion t = new Transaccion(UUID.randomUUID().toString(), cuentaId, dispositivoId,
                Instant.now(reloj).plus(props.vidaTransaccion()));
        transacciones.put(t.id, t);
        auditoria.registrar("AUTENTICACION_DISPOSITIVO_VERIFICADO", Resultado.EXITO, cuentaId, cuentaId, null, dispositivoId);
        return t.id;
    }

    @Override
    public ResultadoPaso verificarPin(String transaccionId, String pin) {
        Transaccion t = transaccion(transaccionId, Paso.DISPOSITIVO_VERIFICADO);
        Usuario cuenta = cuenta(t.cuentaId);
        synchronized (t) {
            if (!pines.coincide(pin, cuenta.getPinHash())) {
                return fallo(t, cuenta, PoliticaIntentosProvisional.PIN_INCORRECTO, false);
            }
            // El desafío se emite después de validar el dispositivo y el PIN (D-054).
            t.paso = Paso.PIN_VERIFICADO;
            auditoria.registrar("AUTENTICACION_PIN_VERIFICADO", Resultado.EXITO, cuenta.getId(), cuenta.getId(), null, t.dispositivoId);
            return new ResultadoPaso("CONTINUAR", null, restantes(cuenta), nuevoDesafio(t));
        }
    }

    @Override
    public ResultadoPaso verificarPinDictado(String transaccionId, byte[] audioWav) {
        transaccion(transaccionId, Paso.DISPOSITIVO_VERIFICADO);
        String pin;
        try {
            pin = servicioVoz.transcribirPin(audioWav);
        } catch (ServicioVozNoDisponibleException e) {
            return new ResultadoPaso("SERVICIO_NO_DISPONIBLE", "SERVICIO_NO_DISPONIBLE", null, null);
        }
        if (pin == null) {
            // No se entendió el dictado: se pide repetir sin contar intento.
            return new ResultadoPaso("REINTENTAR", "PIN_NO_RECONOCIDO", null, null);
        }
        return verificarPin(transaccionId, pin);
    }

    @Override
    public ResultadoPaso verificarVoz(String transaccionId, byte[] audioWav) {
        Transaccion t = transaccion(transaccionId, Paso.PIN_VERIFICADO);
        Usuario cuenta = cuenta(t.cuentaId);
        synchronized (t) {
            // Enviar un audio consume el desafío (D-054).
            Desafio desafio = desafios.consumir(t.desafioId, t.cuentaId, Contexto.INICIO_SESION).orElse(null);
            t.desafioId = null;
            if (desafio == null) {
                return new ResultadoPaso("REINTENTAR", "DESAFIO_VENCIDO", restantes(cuenta), nuevoDesafio(t));
            }
            ResultadoTecnico resultado;
            try {
                resultado = servicioVoz.verificar(t.cuentaId, desafio.texto(), audioWav);
            } catch (ServicioVozNoDisponibleException e) {
                auditoria.registrar("AUTENTICACION_SERVICIO_VOZ_NO_DISPONIBLE", Resultado.FALLO, t.cuentaId, t.cuentaId,
                        "SERVICIO_NO_DISPONIBLE", t.dispositivoId);
                return new ResultadoPaso("SERVICIO_NO_DISPONIBLE", "SERVICIO_NO_DISPONIBLE", restantes(cuenta), nuevoDesafio(t));
            }
            if (resultado.aprobado()) {
                cuenta.reiniciarIntentos();
                t.paso = Paso.TERMINADA;
                transacciones.remove(t.id);
                auditoria.registrar("AUTENTICACION_EXITOSA", Resultado.EXITO, t.cuentaId, t.cuentaId, null, t.dispositivoId);
                // Sesión con mecanismo PROVISIONAL (D-018); el token solo viaja en esta respuesta.
                String token = sesiones.crear(t.cuentaId, t.dispositivoId);
                return new ResultadoPaso("AUTENTICADO", null, null, null, token);
            }
            if ("SIN_REFERENCIA".equals(resultado.motivo())) {
                // Sin referencia biométrica utilizable (p. ej. cambio de modelo): exige re-enrolar (D-013). No cuenta como intento.
                t.paso = Paso.TERMINADA;
                transacciones.remove(t.id);
                auditoria.registrar("AUTENTICACION_RECHAZADA", Resultado.FALLO, t.cuentaId, t.cuentaId, "SIN_REFERENCIA",
                        t.dispositivoId);
                return new ResultadoPaso("RECHAZADO", "SIN_REFERENCIA", restantes(cuenta), null);
            }
            return fallo(t, cuenta, resultado.motivo(), true);
        }
    }

    private ResultadoPaso fallo(Transaccion t, Usuario cuenta, String motivo, boolean etapaVoz) {
        auditoria.registrar(etapaVoz ? "AUTENTICACION_VOZ_FALLIDA" : "AUTENTICACION_PIN_FALLIDO", Resultado.FALLO,
                cuenta.getId(), cuenta.getId(), motivo, t.dispositivoId);
        if (politica.cuentaComoIntentoFallido(motivo) && cuenta.registrarFallo() >= props.intentosMaximos()) {
            t.paso = Paso.TERMINADA;
            transacciones.remove(t.id);
            // Bloqueo por el sistema: revoca las sesiones y queda auditado como CUENTA_BLOQUEADA (D-044).
            usuarioService.bloquear(cuenta.getId(), null, "INTENTOS_AGOTADOS");
            return new ResultadoPaso("BLOQUEADA", motivo, 0, null, null);
        }
        DesafioDTO desafio = etapaVoz ? nuevoDesafio(t) : null;
        return new ResultadoPaso("REINTENTAR", motivo, restantes(cuenta), desafio, null);
    }

    private DesafioDTO nuevoDesafio(Transaccion t) {
        Desafio d = desafios.emitir(t.cuentaId, Contexto.INICIO_SESION);
        t.desafioId = d.id();
        return new DesafioDTO(d.id(), d.texto());
    }

    private int restantes(Usuario cuenta) {
        return Math.max(0, props.intentosMaximos() - cuenta.getIntentosFallidos());
    }

    private Usuario cuenta(String cuentaId) {
        return usuarios.porId(cuentaId).orElseThrow(() -> NayraException.noEncontrado("USUARIO_NO_ENCONTRADO"));
    }

    private Transaccion transaccion(String id, Paso esperado) {
        Transaccion t = id == null ? null : transacciones.get(id);
        if (t == null || Instant.now(reloj).isAfter(t.expira)) {
            if (t != null) {
                transacciones.remove(id);
            }
            throw NayraException.noEncontrado("TRANSACCION_NO_VALIDA");
        }
        if (t.paso != esperado) {
            throw NayraException.conflicto("PASO_NO_VALIDO");
        }
        if (cuenta(t.cuentaId).getEstado() == Usuario.Estado.BLOQUEADA) {
            transacciones.remove(id);
            throw NayraException.conflicto("CUENTA_BLOQUEADA");
        }
        return t;
    }

    private void purgarVencidas() {
        Instant ahora = Instant.now(reloj);
        transacciones.values().removeIf(t -> ahora.isAfter(t.expira));
    }
}
