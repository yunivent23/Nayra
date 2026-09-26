package upc.pe.nayrabackend.securities;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import upc.pe.nayrabackend.exceptions.CodigoError;

import java.io.IOException;

/** Respuesta 401 uniforme cuando falta la sesión o expiró (04_API.md §4). */
@Component
public class SesionEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException e)
            throws IOException {
        CodigoError c = CodigoError.SESION_EXPIRADA;
        response.setStatus(c.getEstado().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"codigo\":\"" + c.name() + "\",\"mensaje\":\"" + c.getMensaje() + "\"}");
    }
}
