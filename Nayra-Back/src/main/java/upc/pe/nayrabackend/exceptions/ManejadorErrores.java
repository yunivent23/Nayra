package upc.pe.nayrabackend.exceptions;

import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import upc.pe.nayrabackend.dtos.ErrorDTO;

/** Respuesta uniforme {codigo, mensaje} (04_API.md §1). Nunca expone detalles internos. */
@RestControllerAdvice
public class ManejadorErrores {


    @ExceptionHandler(NayraException.class)
    public ResponseEntity<ErrorDTO> nayra(NayraException e) {
        return ResponseEntity.status(e.getEstado()).body(new ErrorDTO(e.getCodigo().name(), e.getMensaje()));
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, MissingServletRequestParameterException.class,
            MissingServletRequestPartException.class, IllegalArgumentException.class,
            MethodArgumentTypeMismatchException.class, HttpMessageNotReadableException.class,
            MaxUploadSizeExceededException.class})
    public ResponseEntity<ErrorDTO> invalida(Exception e) {
        CodigoError c = CodigoError.SOLICITUD_INVALIDA;
        return ResponseEntity.status(c.getEstado()).body(new ErrorDTO(c.name(), c.getMensaje()));
    }
}
