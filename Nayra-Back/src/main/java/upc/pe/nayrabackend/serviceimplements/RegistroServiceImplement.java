package upc.pe.nayrabackend.serviceimplements;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import upc.pe.nayrabackend.config.NayraProperties;
import upc.pe.nayrabackend.dtos.RegistroDTOs.DatosParaConfirmar;
import upc.pe.nayrabackend.dtos.RegistroDTOs.EstadoRegistro;
import upc.pe.nayrabackend.dtos.RegistroDTOs.RegistroFinalizado;
import upc.pe.nayrabackend.dtos.RegistroDTOs.RegistroIniciado;
import upc.pe.nayrabackend.dtos.RegistroDTOs.SolicitudDatosRegistro;
import upc.pe.nayrabackend.entities.Auditoria.Resultado;
import upc.pe.nayrabackend.entities.Celular;
import upc.pe.nayrabackend.entities.Cuentas;
import upc.pe.nayrabackend.entities.DocumentoIdentidad;
import upc.pe.nayrabackend.entities.Dispositivos;
import upc.pe.nayrabackend.entities.Identificadores;
import upc.pe.nayrabackend.entities.RegistroEnCurso;
import upc.pe.nayrabackend.entities.RegistroEnCurso.Paso;
import upc.pe.nayrabackend.entities.RegistroIdentidadSimulado;
import upc.pe.nayrabackend.entities.Rol;
import upc.pe.nayrabackend.entities.Usuario;
import upc.pe.nayrabackend.excepciones.NayraException;
import upc.pe.nayrabackend.repositories.IRegistroIdentidadRepository;
import upc.pe.nayrabackend.repositories.IUsuariosRepository;
import upc.pe.nayrabackend.serviceinterfaces.IAuditoriaService;
import upc.pe.nayrabackend.serviceinterfaces.ICredencialesService;
import upc.pe.nayrabackend.serviceinterfaces.ICuentaService;
import upc.pe.nayrabackend.serviceinterfaces.IDispositivoService;
import upc.pe.nayrabackend.serviceinterfaces.IPinService;
import upc.pe.nayrabackend.serviceinterfaces.IRegistroService;

import java.security.PublicKey;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registro inicial asistido (D-052). Los registros en curso viven en memoria (PROVISIONAL, D-051) y el
 * código de un solo uso para continuar en el celular de la persona es un mecanismo PROVISIONAL del prototipo,
 * no una decisión de D-052. Que solo un ADMIN actúe como representante también es PROVISIONAL.
 *
 * BLOQUEO / DECISIÓN PENDIENTE (D-052, 2026-09-27): la FK auditoria.usuario_afectado_id → usuarios (D-051)
 * no admite el identificador previsto de una cuenta que todavía no existe. Mientras no se decida cómo se
 * audita el registro asistido, los eventos anteriores a la creación de la cuenta se registran SIN usuario
 * afectado (PROVISIONAL); el actor (representante) sí se conserva.
 */
@Service
public class RegistroServiceImplement implements IRegistroService {

    private final IUsuariosRepository usuarios;
    private final IRegistroIdentidadRepository identidades;
    private final ICuentaService cuentas;
    private final IPinService pines;
    private final ICredencialesService credenciales;
    private final IDispositivoService dispositivos;
    private final IAuditoriaService auditoria;
    private final NayraProperties props;
    private final SecureRandom random;
    private final Clock reloj;
    private final Map<String, RegistroEnCurso> enCurso = new ConcurrentHashMap<>();

    public RegistroServiceImplement(IUsuariosRepository usuarios, IRegistroIdentidadRepository identidades,
                                    ICuentaService cuentas, IPinService pines, ICredencialesService credenciales,
                                    IDispositivoService dispositivos,
                                    IAuditoriaService auditoria, NayraProperties props, SecureRandom random, Clock reloj) {
        this.usuarios = usuarios;
        this.identidades = identidades;
        this.cuentas = cuentas;
        this.pines = pines;
        this.credenciales = credenciales;
        this.dispositivos = dispositivos;
        this.auditoria = auditoria;
        this.props = props;
        this.random = random;
        this.reloj = reloj;
    }

    @Override
    public RegistroIniciado iniciar(String representanteId, String tipoDocumento, String numeroDocumento) {
        RegistroIdentidadSimulado identidad = identidadDisponible(tipoDocumento, numeroDocumento, representanteId);
        if (cuentas.localizarPorTitular(identidad.tipoDocumentoIdentidad(), identidad.numeroDocumento()).isEmpty()) {
            auditoria.fallo("REGISTRO_INICIO", representanteId, null, "CUENTA_FINANCIERA_NO_ENCONTRADA");
            throw NayraException.conflicto("CUENTA_FINANCIERA_NO_ENCONTRADA");
        }
        RegistroEnCurso registro = nuevo(identidad, Rol.USER, representanteId);
        auditoria.exito("REGISTRO_INICIO", representanteId, null);
        return new RegistroIniciado(registro.getCodigo(), identidad.nombres(), identidad.apellidos());
    }

    @Override
    public void validarIdentidad(String representanteId, String codigoRegistro) {
        RegistroEnCurso r = vigente(codigoRegistro);
        if (r.getPaso() != Paso.INICIADO || r.getRepresentanteId() == null || !r.getRepresentanteId().equals(representanteId)) {
            throw NayraException.conflicto("PASO_NO_VALIDO");
        }
        r.validarIdentidad();
        // Quién validó queda como actor del evento; el registro técnico definitivo sigue pendiente (D-052).
        auditoria.exito("REGISTRO_VALIDACION_IDENTIDAD_ASISTIDA", representanteId, null);
    }

    @Override
    public synchronized RegistroIniciado iniciarAdministradorInicial(String tipoDocumento, String numeroDocumento) {
        boolean adminPendiente = enCurso.values().stream().anyMatch(r -> r.getRol() == Rol.ADMIN && !vencido(r));
        if (usuarios.existeConRol(Rol.ADMIN) || adminPendiente) {
            throw NayraException.conflicto("ADMINISTRADOR_YA_EXISTE");
        }
        RegistroIdentidadSimulado identidad = identidadDisponible(tipoDocumento, numeroDocumento, null);
        RegistroEnCurso registro = nuevo(identidad, Rol.ADMIN, null);
        registro.validarIdentidad();
        auditoria.exito("ARRANQUE_ADMINISTRADOR_PROTOTIPO", null, null);
        return new RegistroIniciado(registro.getCodigo(), identidad.nombres(), identidad.apellidos());
    }

    @Override
    public DatosParaConfirmar datosParaConfirmar(String codigoRegistro) {
        RegistroEnCurso r = vigente(codigoRegistro);
        if (r.getPaso() == Paso.INICIADO) {
            throw NayraException.conflicto("IDENTIDAD_NO_VALIDADA");
        }
        return new DatosParaConfirmar(r.getNombres(), r.getApellidos(), r.getPaso().name());
    }

    @Override
    public EstadoRegistro completarDatos(String codigoRegistro, SolicitudDatosRegistro s) {
        RegistroEnCurso r = vigente(codigoRegistro);
        if (r.getPaso() != Paso.IDENTIDAD_VALIDADA) {
            throw NayraException.conflicto(r.getPaso() == Paso.INICIADO ? "IDENTIDAD_NO_VALIDADA" : "PASO_NO_VALIDO");
        }
        if (s == null || s.confirmaDatos() == null) {
            throw NayraException.solicitudInvalida("CONFIRMACION_REQUERIDA");
        }
        if (!s.confirmaDatos()) {
            // PROVISIONAL: si la persona no reconoce los datos, el registro se cancela (D-052 no lo establece).
            enCurso.remove(r.getCodigo());
            auditoria.fallo("REGISTRO_CANCELADO", null, null, "DATOS_NO_CONFIRMADOS");
            return new EstadoRegistro("CANCELADO");
        }
        // Celular de Perú en formato canónico y único: localiza al destinatario de una transferencia (G-1, V012).
        Celular celular = Celular.leer(s.celular());
        if (usuarios.porCelular(celular.numero()).isPresent()) {
            throw NayraException.conflicto("CELULAR_REGISTRADO");
        }
        if (!pines.formatoValido(s.pin())) {
            throw NayraException.solicitudInvalida("PIN_INVALIDO");
        }
        PublicKey clave = dispositivos.leerClavePublica(s.clavePublicaDispositivo());
        r.completarDatos(celular.numero(), pines.hashear(s.pin()), clave);
        auditoria.exito("REGISTRO_DATOS_COMPLETOS", null, null);
        return new EstadoRegistro(r.getPaso().name());
    }

    @Override
    public String usuarioParaEnrolar(String codigoRegistro) {
        RegistroEnCurso r = vigente(codigoRegistro);
        if (r.getPaso() != Paso.DATOS_COMPLETOS) {
            throw NayraException.conflicto(r.getPaso() == Paso.VOZ_ENROLADA ? "VOZ_YA_ENROLADA" : "REGISTRO_NO_LISTO_PARA_ENROLAR");
        }
        return r.getUsuarioIdPrevisto();
    }

    @Override
    public void marcarVozEnrolada(String codigoRegistro) {
        RegistroEnCurso r = vigente(codigoRegistro);
        if (r.getPaso() != Paso.DATOS_COMPLETOS) {
            throw NayraException.conflicto("PASO_NO_VALIDO");
        }
        r.marcarVozEnrolada();
        auditoria.exito("REGISTRO_VOZ_ENROLADA", null, null);
    }

    @Override
    @Transactional
    public synchronized RegistroFinalizado finalizar(String codigoRegistro) {
        RegistroEnCurso r = vigente(codigoRegistro);
        if (r.getPaso() != Paso.VOZ_ENROLADA) {
            throw NayraException.conflicto("VOZ_NO_ENROLADA");
        }
        if (usuarios.porDocumento(r.getTipoDocumentoIdentidad(), r.getNumeroDocumento()).isPresent()) {
            enCurso.remove(r.getCodigo());
            throw NayraException.conflicto("DOCUMENTO_REGISTRADO");
        }
        if (usuarios.porCelular(r.getCelular()).isPresent()) {
            // Otro registro terminó antes con el mismo número (UNIQUE en V012).
            enCurso.remove(r.getCodigo());
            throw NayraException.conflicto("CELULAR_REGISTRADO");
        }
        Instant ahora = Instant.now(reloj);
        Usuario usuario = new Usuario(r.getUsuarioIdPrevisto(), r.getTipoDocumentoIdentidad(), r.getNumeroDocumento(),
                r.getNombres(), r.getApellidos(), r.getCelular(), r.getRol(), ahora);
        usuarios.guardar(usuario);
        // La credencial del PIN es de Autenticación (v4 §2.2). Coordinación distribuida del registro: P-10 (pendiente);
        // hoy es una sola aplicación y la transacción cubre usuario, credencial, dispositivo y cuenta.
        credenciales.crear(usuario.getId(), r.getPinHash());
        Dispositivos dispositivo = dispositivos.vincular(usuario.getId(), r.getClavePublica());
        // PROVISIONAL: el primer ADMIN puede no tener cuenta financiera; no se asume una exención definitiva de D-025.
        Optional<Cuentas> cuenta = cuentas.localizarPorTitular(r.getTipoDocumentoIdentidad(), r.getNumeroDocumento());
        cuenta.ifPresent(c -> cuentas.vincular(c, usuario.getId()));
        enCurso.remove(r.getCodigo());
        auditoria.registrar("REGISTRO_COMPLETADO", Resultado.EXITOSO, usuario.getId(), usuario.getId(),
                cuenta.isPresent() ? null : "SIN_CUENTA_FINANCIERA", dispositivo.getId());
        auditoria.registrar("DISPOSITIVO_VINCULADO", Resultado.EXITOSO, usuario.getId(), usuario.getId(), null, dispositivo.getId());
        return new RegistroFinalizado(usuario.getId(), dispositivo.getId());
    }

    private RegistroIdentidadSimulado identidadDisponible(String tipoDocumento, String numeroDocumento, String actorId) {
        DocumentoIdentidad doc = DocumentoIdentidad.leer(tipoDocumento, numeroDocumento);
        if (usuarios.porDocumento(doc.tipo(), doc.numero()).isPresent()) {
            // Un documento no puede tener dos cuentas de acceso: se deriva a recuperación/cambio de dispositivo (D-036, D-040).
            auditoria.fallo("REGISTRO_INICIO", actorId, null, "DOCUMENTO_REGISTRADO");
            throw NayraException.conflicto("DOCUMENTO_REGISTRADO");
        }
        return identidades.porDocumento(doc.tipo(), doc.numero()).orElseThrow(() -> {
            auditoria.fallo("REGISTRO_INICIO", actorId, null, "DOCUMENTO_NO_ENCONTRADO");
            return NayraException.noEncontrado("DOCUMENTO_NO_ENCONTRADO");
        });
    }

    private RegistroEnCurso nuevo(RegistroIdentidadSimulado identidad, Rol rol, String representanteId) {
        purgarVencidos();
        // Un nuevo registro para el mismo documento invalida el anterior.
        enCurso.values().removeIf(r -> r.getTipoDocumentoIdentidad() == identidad.tipoDocumentoIdentidad()
                && r.getNumeroDocumento().equals(identidad.numeroDocumento()));
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String codigo = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        RegistroEnCurso registro = new RegistroEnCurso(codigo, Identificadores.nuevo(), identidad, rol,
                representanteId, Instant.now(reloj).plus(props.registro().vidaRegistroEnCurso()));
        enCurso.put(codigo, registro);
        return registro;
    }

    private RegistroEnCurso vigente(String codigo) {
        RegistroEnCurso r = codigo == null ? null : enCurso.get(codigo);
        if (r == null || vencido(r)) {
            if (r != null) {
                enCurso.remove(codigo);
            }
            throw NayraException.noEncontrado("REGISTRO_NO_VALIDO");
        }
        return r;
    }

    private boolean vencido(RegistroEnCurso r) {
        return Instant.now(reloj).isAfter(r.getExpira());
    }

    private void purgarVencidos() {
        enCurso.values().removeIf(this::vencido);
    }
}
