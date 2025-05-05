package stud.ntnu.krisefikser.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for frontend-related properties.
 *
 * <p>This class is used to bind properties prefixed with "frontend" from the application
 * properties file. It contains a single property, "url", which represents the URL of the frontend
 * application.
 * </p>
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "frontend")
public class FrontendConfig {

  private String url;
}