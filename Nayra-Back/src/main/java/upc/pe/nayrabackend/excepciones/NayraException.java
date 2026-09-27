package upc.pe.nayrabackend.excepciones;

import org.springframework.http.HttpStatus;

/**
 * Error de negocio con un código estable y genérico ("DNI_REGISTRADO", "NO_ENCONTRADO"...).
 * El código es lo único que llega al cliente: nunca datos personales, PIN ni detalles internos (06 §21).
 */
public class NayraException extends RuntimeException {

    private final String codigo;
    private final HttpStatus estado;

    public NayraException(HttpStatus estado, String codigo) {
        super(codigo);
        this.estado = estado;
        this.codigo = codigo;
    }

    public String getCodigo() { return codigo; }
    public HttpStatus getEstado() { return estado; }

    public static NayraException noEncontrado(String codigo) { return new NayraException(HttpStatus.NOT_FOUND, codigo); }
    public static NayraException conflicto(String codigo) { return new NayraException(HttpStatus.CONFLICT, codigo); }
    public static NayraException solicitudInvalida(String codigo) { return new NayraException(HttpStatus.BAD_REQUEST, codigo); }
    public static NayraException noAutorizado(String codigo) { return new NayraException(HttpStatus.UNAUTHORIZED, codigo); }
}
