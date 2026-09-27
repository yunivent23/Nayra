package upc.pe.nayrabackend.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import upc.pe.nayrabackend.excepciones.NayraException;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.regex.Pattern;

/**
 * Formato único de error: {"error": "CODIGO"}. Nunca devuelve datos personales, PIN, trazas ni
 * mensajes internos (06 §21). Los errores inesperados se registran sin el cuerpo de la petición.
 */
@RestControllerAdvice
public class ManejadorErrores {

    private static final Logger LOG = LoggerFactory.getLogger(ManejadorErrores.class);
    private static final Pattern CODIGO = Pattern.compile("[A-Z][A-Z_]{0,63}");

    @ExceptionHandler(NayraException.class)
    public ResponseEntity<Map<String, String>> negocio(NayraException e) {
        return error(e.getEstado(), e.getCodigo());
    }

    /** Firma o nonce del dispositivo no válidos (D-048). */
    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<Map<String, String>> noAutorizado(SecurityException e) {
        return error(HttpStatus.UNAUTHORIZED, "DISPOSITIVO_NO_VERIFICADO");
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Map<String, String>> noEncontrado(NoSuchElementException e) {
        return error(HttpStatus.NOT_FOUND, esCodigo(e.getMessage()) ? e.getMessage() : "NO_ENCONTRADO");
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> conflicto(IllegalStateException e) {
        if (!esCodigo(e.getMessage())) {
            LOG.error("Estado inesperado ({})", e.getClass().getSimpleName(), e);
            return error(HttpStatus.INTERNAL_SERVER_ERROR, "ERROR_INTERNO");
        }
        return error(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler({IllegalArgumentException.class, HttpMessageNotReadableException.class,
            MissingServletRequestParameterException.class, MissingServletRequestPartException.class,
            MissingRequestHeaderException.class})
    public ResponseEntity<Map<String, String>> invalido(Exception e) {
        return error(HttpStatus.BAD_REQUEST, "SOLICITUD_INVALIDA");
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String, String>> demasiadoGrande(MaxUploadSizeExceededException e) {
        return error(HttpStatus.CONTENT_TOO_LARGE, "AUDIO_DEMASIADO_GRANDE");
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<Map<String, String>> tipoNoSoportado(HttpMediaTypeNotSupportedException e) {
        return error(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "TIPO_DE_CONTENIDO_NO_SOPORTADO");
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Map<String, String>> metodo(HttpRequestMethodNotSupportedException e) {
        return error(HttpStatus.METHOD_NOT_ALLOWED, "METODO_NO_PERMITIDO");
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Map<String, String>> rutaInexistente(NoResourceFoundException e) {
        return error(HttpStatus.NOT_FOUND, "NO_ENCONTRADO");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> inesperado(Exception e) {
        LOG.error("Error inesperado ({})", e.getClass().getSimpleName(), e);
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "ERROR_INTERNO");
    }

    private static boolean esCodigo(String s) {
        return s != null && CODIGO.matcher(s).matches();
    }

    private static ResponseEntity<Map<String, String>> error(HttpStatus estado, String codigo) {
        return ResponseEntity.status(estado).body(Map.of("error", codigo));
    }
}
