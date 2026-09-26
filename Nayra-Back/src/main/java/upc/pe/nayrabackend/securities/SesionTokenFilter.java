package upc.pe.nayrabackend.securities;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import upc.pe.nayrabackend.serviceinterfaces.ISesionesService;

import java.io.IOException;
import java.util.List;

/** Autenticación por token opaco (D-042). Reemplaza al filtro JWT del código inicial. */
@Component
public class SesionTokenFilter extends OncePerRequestFilter {

    private static final String PREFIJO = "Bearer ";

    private final ISesionesService sesiones;

    public SesionTokenFilter(ISesionesService sesiones) {
        this.sesiones = sesiones;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String cabecera = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (cabecera != null && cabecera.startsWith(PREFIJO)) {
            sesiones.validar(cabecera.substring(PREFIJO.length()).strip()).ifPresent(s -> {
                List<SimpleGrantedAuthority> roles = s.getUsuario().getRoles() == null ? List.of()
                        : s.getUsuario().getRoles().stream().map(r -> new SimpleGrantedAuthority(r.getRol())).toList();
                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                        new UsuarioAutenticado(s.getUsuario().getId(), s.getId()), null, roles);
                SecurityContextHolder.getContext().setAuthentication(auth);
            });
        }
        chain.doFilter(request, response);
    }
}
