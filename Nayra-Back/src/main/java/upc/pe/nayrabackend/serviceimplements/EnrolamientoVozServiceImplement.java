package upc.pe.nayrabackend.serviceimplements;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import upc.pe.nayrabackend.config.VozProperties;
import upc.pe.nayrabackend.dtos.AutenticacionVozDTOs.DesafioDTO;
import upc.pe.nayrabackend.dtos.AutenticacionVozDTOs.ResultadoEnrolamiento;
import upc.pe.nayrabackend.dtos.AutenticacionVozDTOs.ResultadoMuestra;
import upc.pe.nayrabackend.serviceinterfaces.IAuditoriaService;
import upc.pe.nayrabackend.serviceinterfaces.IDesafioService;
import upc.pe.nayrabackend.serviceinterfaces.IDesafioService.Contexto;
import upc.pe.nayrabackend.serviceinterfaces.IEnrolamientoVozService;
import upc.pe.nayrabackend.serviceinterfaces.IRegistroService;
import upc.pe.nayrabackend.serviceinterfaces.IServicioVozCliente;
import upc.pe.nayrabackend.serviceinterfaces.IServicioVozCliente.ResultadoTecnico;
import upc.pe.nayrabackend.serviceinterfaces.IServicioVozCliente.ServicioVozNoDisponibleException;

/**
 * Coordina el enrolamiento dentro del registro asistido (D-052 paso 11): Spring Boot emite un desafío por
 * muestra y solo recibe "correcto" o el motivo del fallo; el embedding y su centroide quedan en Python
 * (D-013, 05 §27.5). El registro en curso (con PIN y clave del dispositivo ya definidos) lo da el backend general.
 */
@Profile("prototipo")
@Service
public class EnrolamientoVozServiceImplement implements IEnrolamientoVozService {

    private final IDesafioService desafios;
    private final IServicioVozCliente servicioVoz;
    private final IAuditoriaService auditoria;
    private final IRegistroService registro;
    private final VozProperties props;

    public EnrolamientoVozServiceImplement(IDesafioService desafios, IServicioVozCliente servicioVoz,
                                           IAuditoriaService auditoria, IRegistroService registro, VozProperties props) {
        this.desafios = desafios;
        this.servicioVoz = servicioVoz;
        this.auditoria = auditoria;
        this.registro = registro;
        this.props = props;
    }

    @Override
    public DesafioDTO emitirDesafio(String codigoRegistro) {
        String usuarioId = registro.usuarioParaEnrolar(codigoRegistro);
        IDesafioService.Desafio d = desafios.emitir(usuarioId, Contexto.ENROLAMIENTO);
        return new DesafioDTO(d.id(), d.texto());
    }

    @Override
    public ResultadoMuestra enviarMuestra(String codigoRegistro, String desafioId, byte[] audioWav) {
        String usuarioId = registro.usuarioParaEnrolar(codigoRegistro);
        IDesafioService.Desafio desafio = desafios.consumir(desafioId, usuarioId, Contexto.ENROLAMIENTO).orElse(null);
        if (desafio == null) {
            return new ResultadoMuestra(false, "DESAFIO_VENCIDO", null, props.muestrasEnrolamiento());
        }
        try {
            ResultadoTecnico r = servicioVoz.agregarMuestraEnrolamiento(usuarioId, desafio.texto(), audioWav);
            return new ResultadoMuestra(r.aprobado(), r.motivo(), r.muestrasValidas(), props.muestrasEnrolamiento());
        } catch (ServicioVozNoDisponibleException e) {
            return new ResultadoMuestra(false, "SERVICIO_NO_DISPONIBLE", null, props.muestrasEnrolamiento());
        }
    }

    @Override
    public ResultadoEnrolamiento finalizar(String codigoRegistro) {
        String usuarioId = registro.usuarioParaEnrolar(codigoRegistro);
        try {
            IServicioVozCliente.ResultadoEnrolamiento r = servicioVoz.finalizarEnrolamiento(usuarioId);
            if (r.correcto()) {
                registro.marcarVozEnrolada(codigoRegistro);
                // Sin usuario afectado: la cuenta aún no existe (FK de D-051; auditoría del registro pendiente, D-052).
                auditoria.exito("ENROLAMIENTO_VOZ_CORRECTO", null, null);
            } else {
                auditoria.fallo("ENROLAMIENTO_VOZ_FALLIDO", null, null, r.motivo());
            }
            return new ResultadoEnrolamiento(r.correcto(), r.motivo(), r.muestrasValidas());
        } catch (ServicioVozNoDisponibleException e) {
            return new ResultadoEnrolamiento(false, "SERVICIO_NO_DISPONIBLE", 0);
        }
    }
}
