package stud.ntnu.krisefikser.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for OpenAPI documentation. Provides configuration for Swagger UI and OpenAPI
 * documentation. This includes server information, contact details, and license information.
 *
 * @since 1.0
 */
@Configuration
@RequiredArgsConstructor
public class OpenApiConfig {

  private final FrontendConfig frontendConfig;

  /**
   * Configures the OpenAPI documentation for the application. Sets up the server information,
   * contact details, license, and basic API information.
   *
   * @return The OpenAPI configuration object.
   * @since 1.0
   */
  @Bean
  public OpenAPI openAPI() {
    return new OpenAPI()
        .info(new Info()
            .title("Krisefikser API")
            .description("API for the Krisefikser application")
            .version("1.0")
            .contact(new Contact()
                .name("Krisefikser Team")
                .email("krisefikser@example.com"))
            .license(new License()
                .name("MIT License")
                .url("https://opensource.org/licenses/MIT")))
        .servers(List.of(
            new Server()
                .url(frontendConfig.getUrl())
                .description("Development server")));
  }
}