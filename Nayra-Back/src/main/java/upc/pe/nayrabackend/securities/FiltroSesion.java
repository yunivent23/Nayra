package upc.pe.nayrabackend.securities;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import upc.pe.nayrabackend.serviceinterfaces.ISesionesService;

import java.io.IOException;
import java.util.List;

/**
 * Resuelve "Authorization: Bearer &lt;JWT&gt;" a una sesión vigente (v4 §4.1). Es el lado de Negocio: pregunta a
 * Autenticación ({@link ISesionesService}), única autoridad sobre la sesión, que aplica la regla aprobada de 5 minutos
 * de inactividad. El token nunca se registra en logs.
 * No es un bean para que Spring Boot no lo registre dos veces como filtro del servlet.
 */
public class FiltroSesion extends OncePerRequestFilter {

    private static final String PREFIJO = "Bearer ";

    private final ISesionesService sesiones;

    public FiltroSesion(ISesionesService sesiones) {
        this.sesiones = sesiones;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String cabecera = request.getHeader("Authorization");
        if (cabecera != null && cabecera.startsWith(PREFIJO)) {
            sesiones.validar(cabecera.substring(PREFIJO.length()).trim()).ifPresent(s -> {
                UsuarioAutenticado principal = new UsuarioAutenticado(s.usuarioId(), s.sesionId(), s.dispositivoId(), s.rol());
                var autenticacion = new UsernamePasswordAuthenticationToken(principal, null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + s.rol().name())));
                SecurityContextHolder.getContext().setAuthentication(autenticacion);
            });
        }
        chain.doFilter(request, response);
    }
}
