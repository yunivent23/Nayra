package upc.pe.nayrabackend.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Profile("prototipo")
@Configuration
@EnableConfigurationProperties(VozProperties.class)
public class VozConfiguracion {

    /** Cliente del servicio Python (D-010): solo red interna, timeouts configurables, sin reintentos. */
    @Bean
    public RestClient servicioVozRestClient(VozProperties props) {
        SimpleClientHttpRequestFactory fabrica = new SimpleClientHttpRequestFactory();
        fabrica.setConnectTimeout(props.servicioTimeoutConexion());
        fabrica.setReadTimeout(props.servicioTimeoutLectura());
        return RestClient.builder()
                .baseUrl(props.servicioUrl())
                .requestFactory(fabrica)
                .defaultHeader("Authorization", "Bearer " + props.servicioToken())
                .build();
    }
}
