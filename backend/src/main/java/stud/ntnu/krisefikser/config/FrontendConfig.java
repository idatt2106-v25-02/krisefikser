package stud.ntnu.krisefikser.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import lombok.Getter;
import lombok.Setter;

@Configuration
@ConfigurationProperties(prefix = "frontend")
@Getter
@Setter
public class FrontendConfig {
    private String url;
}