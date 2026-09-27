package upc.pe.nayrabackend.serviceinterfaces;

import upc.pe.nayrabackend.dtos.RegistroDTOs.DatosParaConfirmar;
import upc.pe.nayrabackend.dtos.RegistroDTOs.EstadoRegistro;
import upc.pe.nayrabackend.dtos.RegistroDTOs.RegistroFinalizado;
import upc.pe.nayrabackend.dtos.RegistroDTOs.RegistroIniciado;
import upc.pe.nayrabackend.dtos.RegistroDTOs.SolicitudDatosRegistro;

/**
 * Registro inicial asistido (D-052, modifica D-036). Usa el DNI solo para consultar el registro de
 * identidad simulado (D-053); la consulta no prueba la identidad: la valida el representante.
 * La biometría no valida la identidad en el registro.
 */
public interface IRegistroService {

    /**
     * Pasos 3–5: el representante proporciona el DNI y ve los datos del registro simulado.
     * PROVISIONAL: solo un ADMIN actúa como representante; D-052 admite también otra persona autorizada (pendiente).
     */
    RegistroIniciado iniciar(String representanteId, String dni);

    /** Paso 6: el representante confirma que validó la identidad de la persona. */
    void validarIdentidad(String representanteId, String codigoRegistro);

    /**
     * MECANISMO TÉCNICO PROVISIONAL DEL PROTOTIPO, solo perfil "prototipo": crea el registro del primer
     * administrador cuando todavía no existe ninguno. Separado de D-052 y sin cumplirlo: no hay representante
     * que valide la identidad. D-050 sigue pendiente.
     */
    RegistroIniciado iniciarAdministradorInicial(String dni);

    /** Paso 7: datos que la persona ve en su celular para confirmarlos. */
    DatosParaConfirmar datosParaConfirmar(String codigoRegistro);

    /**
     * Pasos 7–10: confirmación, celular (D-043), PIN (D-061, solo hash) y clave del dispositivo (D-048).
     * PROVISIONAL: si la persona no confirma sus datos, el registro se cancela (D-052 no lo establece).
     */
    EstadoRegistro completarDatos(String codigoRegistro, SolicitudDatosRegistro solicitud);

    /** Paso 11: identificador de la futura cuenta de acceso para el enrolamiento de voz (AG-13). */
    String usuarioParaEnrolar(String codigoRegistro);

    /** Paso 11 terminado: el servicio de voz confirmó el enrolamiento. */
    void marcarVozEnrolada(String codigoRegistro);

    /** Paso 13: crea la cuenta de acceso, vincula el dispositivo y la cuenta financiera (HU-04). */
    RegistroFinalizado finalizar(String codigoRegistro);
}
