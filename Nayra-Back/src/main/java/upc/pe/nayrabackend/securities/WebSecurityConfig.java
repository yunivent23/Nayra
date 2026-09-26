package upc.pe.nayrabackend.securities;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;

/**
 * D-042: sesiones con token opaco validadas en el servidor (sin JWT, sin sesión HTTP).
 * Autorización detallada (D-009) pendiente: por defecto todo requiere sesión y /usuarios solo ADMINISTRADOR.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class WebSecurityConfig {

    public static final String ROL_ADMINISTRADOR = "ADMINISTRADOR";

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, SesionTokenFilter sesionTokenFilter,
                                           SesionEntryPoint entryPoint) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(req -> req
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/desafios", "/api/v1/auth/sesiones").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .requestMatchers("/error").permitAll()
                        .requestMatchers("/usuarios/**").hasAuthority(ROL_ADMINISTRADOR)
                        .anyRequest().authenticated()
                )
                .exceptionHandling(e -> e.authenticationEntryPoint(entryPoint))
                .addFilterBefore(sesionTokenFilter, AnonymousAuthenticationFilter.class);
        return http.build();
    }

    /** El filtro solo corre dentro de la cadena de Spring Security, no como filtro de servlet adicional. */
    @Bean
    public FilterRegistrationBean<SesionTokenFilter> sesionTokenFilterRegistro(SesionTokenFilter filtro) {
        FilterRegistrationBean<SesionTokenFilter> registro = new FilterRegistrationBean<>(filtro);
        registro.setEnabled(false);
        return registro;
    }

    /** Sin usuarios en memoria ni contraseña generada: la autenticación es solo por sesión (P-A01 pendiente). */
    @Bean
    public UserDetailsService userDetailsService() {
        return new InMemoryUserDetailsManager();
    }

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";
        return new OpenAPI()
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .description("Token opaco de sesión (D-042)")))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName));
    }
}
