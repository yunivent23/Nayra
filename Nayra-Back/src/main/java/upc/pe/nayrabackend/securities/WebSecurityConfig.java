package upc.pe.nayrabackend.securities;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import upc.pe.nayrabackend.serviceinterfaces.ISesionesService;

import jakarta.servlet.http.HttpServletResponse;

/**
 * Roles USER y ADMIN definidos por D-041. El mecanismo de autorización (reglas por ruta con Spring Security y
 * denegación por defecto) es una implementación PROVISIONAL mientras D-009 siga pendiente.
 * La sesión se resuelve con {@link FiltroSesion} (mecanismo PROVISIONAL, D-018).
 * Se deniega todo lo que no esté explícitamente permitido.
 * Organización de rutas PROVISIONAL hasta docs/04_API.md (D-014).
 */
@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    /** PROVISIONAL (D-047): BCrypt sin pepper para el hash del PIN. */
    @Bean
    public static PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * No hay usuarios con contraseña: evita que Spring Boot genere e imprima una contraseña por defecto.
     */
    @Bean
    public UserDetailsService sinUsuariosConContrasena() {
        return new InMemoryUserDetailsManager();
    }

    /** Botón "Authorize" de Swagger con el JWT de sesión (v4 §4.1; algoritmo y clave PROVISIONALES, P-5). */
    @Bean
    public OpenAPI customOpenAPI() {
        final String esquema = "bearerAuth";
        return new OpenAPI()
                .components(new Components().addSecuritySchemes(esquema,
                        new SecurityScheme().name(esquema).type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList(esquema));
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, ISesionesService sesiones) throws Exception {
        http
                // API sin cookies: la sesión viaja en la cabecera Authorization, así que CSRF no aplica.
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(req -> req
                        // EXCEPCIÓN PROVISIONAL solo para el primer entregable (09 §27): rutas /prototipo antes de
                        // D-014. Solo existen con el perfil "prototipo"; se protegen con la firma del dispositivo
                        // (D-048), el PIN y el código de registro, no con una sesión.
                        .requestMatchers("/prototipo/**").permitAll()
                        // Pasos del registro en el celular de la persona: los protege el código de registro de un solo uso.
                        .requestMatchers(HttpMethod.GET, "/api/v1/registros/*").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/registros/*/datos", "/api/v1/registros/*/finalizacion").permitAll()
                        .requestMatchers("/error").permitAll()
                        // Documentación OpenAPI existente (se mantiene tal como estaba permitida).
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/v1/**").authenticated()
                        .anyRequest().denyAll())
                .exceptionHandling(e -> e
                        .authenticationEntryPoint((req, res, ex) -> escribir(res, HttpServletResponse.SC_UNAUTHORIZED, "NO_AUTENTICADO"))
                        .accessDeniedHandler((req, res, ex) -> escribir(res, HttpServletResponse.SC_FORBIDDEN, "ACCESO_DENEGADO")))
                .addFilterBefore(new FiltroSesion(sesiones), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    private static void escribir(HttpServletResponse res, int estado, String codigo) throws java.io.IOException {
        res.setStatus(estado);
        res.setContentType(MediaType.APPLICATION_JSON_VALUE);
        res.getWriter().write("{\"error\":\"" + codigo + "\"}");
    }
}
