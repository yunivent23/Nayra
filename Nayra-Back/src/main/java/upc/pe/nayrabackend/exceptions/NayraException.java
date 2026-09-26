package upc.pe.nayrabackend.exceptions;

import org.springframework.http.HttpStatus;

/** Error de negocio con código de 04_API.md §4 y mensaje apto para ser leído por voz. */
public class NayraException extends RuntimeException {

    private final CodigoError codigo;
    private final String mensaje;

    public NayraException(CodigoError codigo) {
        this(codigo, codigo.getMensaje());
    }

    public NayraException(CodigoError codigo, String mensaje) {
        super(codigo.name());
        this.codigo = codigo;
        this.mensaje = mensaje;
    }

    public CodigoError getCodigo() {
        return codigo;
    }

    public String getMensaje() {
        return mensaje;
    }

    public HttpStatus getEstado() {
        return codigo.getEstado();
    }
}
