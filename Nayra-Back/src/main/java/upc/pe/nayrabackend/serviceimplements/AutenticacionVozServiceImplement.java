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
import upc.pe.nayrabackend.serviceinterfaces.ICredencialesService;
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
 * - Qué cuenta como intento: {@link PoliticaIntentosProvisional}. Solo el PIN incorrecto (v4 §2.2); el contador vive
 *   en nayra.credenciales ({@link ICredencialesService}) y vuelve a 0 tras un PIN correcto. Límite propio de la
 *   biometría: P-8, PENDIENTE NO BLOQUEANTE (sin contador).
 * - Al autenticar se crea la sesión con {@link ISesionesService} (JWT con jti; algoritmo y clave PROVISIONALES, P-5).
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
    private final ICredencialesService credenciales;
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

    public AutenticacionVozServiceImplement(IDispositivoService dispositivos, ICredencialesService credenciales, IDesafioService desafios,
                                            IServicioVozCliente servicioVoz, IAuditoriaService auditoria,
                                            PoliticaIntentosProvisional politica, IUsuariosRepository usuarios,
                                            IUsuarioService usuarioService, ISesionesService sesiones,
                                            VozProperties props, Clock reloj) {
        this.dispositivos = dispositivos;
        this.credenciales = credenciales;
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
            // La FK de auditoria.dispositivo_id solo admite dispositivos existentes (D-051): un identificador
            // desconocido se audita sin dispositivo.
            auditoria.registrar("AUTENTICACION_DISPOSITIVO_RECHAZADO", Resultado.FALLIDO, null, null, "FIRMA_O_NONCE_INVALIDO",
                    dispositivos.existe(dispositivoId) ? dispositivoId : null);
            throw e;
        }
        Usuario cuenta = cuenta(cuentaId);
        if (cuenta.getEstado() != Usuario.Estado.ACTIVO) {
            String motivo = cuenta.getEstado() == Usuario.Estado.BLOQUEADO ? "CUENTA_BLOQUEADA" : "CUENTA_INACTIVA";
            auditoria.registrar("AUTENTICACION_RECHAZADA", Resultado.FALLIDO, cuentaId, cuentaId, motivo, dispositivoId);
            throw NayraException.conflicto(motivo);
        }
        // La cuenta de acceso solo existe cuando el registro terminó con la voz enrolada (D-052), así que no
        // hace falta comprobar aquí el enrolamiento: si Python no tiene referencia, responde SIN_REFERENCIA.
        purgarVencidas();
        Transaccion t = new Transaccion(UUID.randomUUID().toString(), cuentaId, dispositivoId,
                Instant.now(reloj).plus(props.vidaTransaccion()));
        transacciones.put(t.id, t);
        auditoria.registrar("AUTENTICACION_DISPOSITIVO_VERIFICADO", Resultado.EXITOSO, cuentaId, cuentaId, null, dispositivoId);
        return t.id;
    }

    @Override
    public ResultadoPaso verificarPin(String transaccionId, String pin) {
        Transaccion t = transaccion(transaccionId, Paso.DISPOSITIVO_VERIFICADO);
        Usuario cuenta = cuenta(t.cuentaId);
        synchronized (t) {
            // Un PIN correcto devuelve el contador a 0; uno incorrecto lo incrementa (v4 §2.2).
            ICredencialesService.ResultadoPin r = credenciales.verificar(cuenta.getId(), pin);
            if (!r.correcto()) {
                return falloPin(t, cuenta, r);
            }
            // El desafío se emite después de validar el dispositivo y el PIN (D-054).
            t.paso = Paso.PIN_VERIFICADO;
            auditoria.registrar("AUTENTICACION_PIN_VERIFICADO", Resultado.EXITOSO, cuenta.getId(), cuenta.getId(), null, t.dispositivoId);
            return new ResultadoPaso("CONTINUAR", null, r.intentosRestantes(), nuevoDesafio(t));
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
                auditoria.registrar("AUTENTICACION_SERVICIO_VOZ_NO_DISPONIBLE", Resultado.FALLIDO, t.cuentaId, t.cuentaId,
                        "SERVICIO_NO_DISPONIBLE", t.dispositivoId);
                return new ResultadoPaso("SERVICIO_NO_DISPONIBLE", "SERVICIO_NO_DISPONIBLE", restantes(cuenta), nuevoDesafio(t));
            }
            if (resultado.aprobado()) {
                t.paso = Paso.TERMINADA;
                transacciones.remove(t.id);
                auditoria.registrar("AUTENTICACION_EXITOSA", Resultado.EXITOSO, t.cuentaId, t.cuentaId, null, t.dispositivoId);
                // JWT de sesión (jti = nayra.sesiones.id); solo viaja en esta respuesta.
                String token = sesiones.crear(t.cuentaId, t.dispositivoId);
                return new ResultadoPaso("AUTENTICADO", null, null, null, token);
            }
            if ("SIN_REFERENCIA".equals(resultado.motivo())) {
                // Sin referencia biométrica utilizable (p. ej. cambio de modelo): exige re-enrolar (D-013). No cuenta como intento.
                t.paso = Paso.TERMINADA;
                transacciones.remove(t.id);
                auditoria.registrar("AUTENTICACION_RECHAZADA", Resultado.FALLIDO, t.cuentaId, t.cuentaId, "SIN_REFERENCIA",
                        t.dispositivoId);
                return new ResultadoPaso("RECHAZADO", "SIN_REFERENCIA", restantes(cuenta), null);
            }
            // Los fallos de voz, contenido o anti-spoofing no cuentan como intentos de PIN (v4 §2.2, P-8).
            auditoria.registrar("AUTENTICACION_VOZ_FALLIDA", Resultado.FALLIDO, cuenta.getId(), cuenta.getId(),
                    resultado.motivo(), t.dispositivoId);
            return new ResultadoPaso("REINTENTAR", resultado.motivo(), restantes(cuenta), nuevoDesafio(t), null);
        }
    }

    private ResultadoPaso falloPin(Transaccion t, Usuario cuenta, ICredencialesService.ResultadoPin r) {
        String motivo = PoliticaIntentosProvisional.PIN_INCORRECTO;
        auditoria.registrar("AUTENTICACION_PIN_FALLIDO", Resultado.FALLIDO, cuenta.getId(), cuenta.getId(), motivo, t.dispositivoId);
        if (r.agotados() && politica.cuentaComoIntentoFallido(motivo)) {
            t.paso = Paso.TERMINADA;
            transacciones.remove(t.id);
            // Autenticación pide a Negocio el bloqueo; Negocio revoca las sesiones y lo audita como CUENTA_BLOQUEADA (v4 §2.2).
            usuarioService.bloquear(cuenta.getId(), null, "INTENTOS_AGOTADOS");
            return new ResultadoPaso("BLOQUEADA", motivo, 0, null, null);
        }
        return new ResultadoPaso("REINTENTAR", motivo, r.intentosRestantes(), null, null);
    }

    private DesafioDTO nuevoDesafio(Transaccion t) {
        Desafio d = desafios.emitir(t.cuentaId, Contexto.INICIO_SESION);
        t.desafioId = d.id();
        return new DesafioDTO(d.id(), d.texto());
    }

    private int restantes(Usuario cuenta) {
        return credenciales.intentosRestantes(cuenta.getId());
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
        Usuario.Estado estado = cuenta(t.cuentaId).getEstado();
        if (estado != Usuario.Estado.ACTIVO) {
            transacciones.remove(id);
            throw NayraException.conflicto(estado == Usuario.Estado.BLOQUEADO ? "CUENTA_BLOQUEADA" : "CUENTA_INACTIVA");
        }
        return t;
    }

    private void purgarVencidas() {
        Instant ahora = Instant.now(reloj);
        transacciones.values().removeIf(t -> ahora.isAfter(t.expira));
    }
}
