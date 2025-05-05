package stud.ntnu.krisefikser.email.AppConfig;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class EmailConfig {

    @Bean
    public RestTemplate emailRestTemplate() {
        return new RestTemplate();
    }
} 