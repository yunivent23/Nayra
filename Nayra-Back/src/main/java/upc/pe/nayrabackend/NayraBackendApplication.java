package upc.pe.nayrabackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class NayraBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(NayraBackendApplication.class, args);
    }

}
