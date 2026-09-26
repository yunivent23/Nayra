package upc.pe.nayrabackend.exceptions;

import org.springframework.http.HttpStatus;

/** Códigos de error de 04_API.md §4. */
public enum CodigoError {
    DESAFIO_INVALIDO(HttpStatus.UNAUTHORIZED,
            "El desafío ya no es válido. Solicita uno nuevo e inténtalo otra vez."),
    AUTENTICACION_FALLIDA(HttpStatus.UNAUTHORIZED,
            "No se pudo verificar tu identidad. Inténtalo nuevamente."),
    CALIDAD_INSUFICIENTE(HttpStatus.UNPROCESSABLE_CONTENT,
            "No se escuchó bien la grabación. Intenta en un lugar más silencioso y habla cerca del teléfono."),
    CONTENIDO_INCORRECTO(HttpStatus.UNPROCESSABLE_CONTENT,
            "No se reconoció la frase completa. Escucha el desafío de nuevo y repítelo."),
    CALIBRACION(HttpStatus.SERVICE_UNAVAILABLE,
            "La autenticación por voz aún no está habilitada."),
    CUENTA_BLOQUEADA(HttpStatus.LOCKED,
            "Tu cuenta está bloqueada por seguridad. Comunícate con atención para desbloquearla."),
    SESION_EXPIRADA(HttpStatus.UNAUTHORIZED,
            "Tu sesión se cerró. Vuelve a iniciar sesión."),
    SERVICIO_VOZ_NO_DISPONIBLE(HttpStatus.SERVICE_UNAVAILABLE,
            "El servicio de voz no está disponible. Inténtalo de nuevo en unos momentos."),
    SOLICITUD_INVALIDA(HttpStatus.BAD_REQUEST,
            "La solicitud no es válida."),
    NO_ENCONTRADO(HttpStatus.NOT_FOUND,
            "No se encontró el recurso solicitado.");

    private final HttpStatus estado;
    private final String mensaje;

    CodigoError(HttpStatus estado, String mensaje) {
        this.estado = estado;
        this.mensaje = mensaje;
    }

    public HttpStatus getEstado() {
        return estado;
    }

    public String getMensaje() {
        return mensaje;
    }
}
