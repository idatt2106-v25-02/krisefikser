package stud.ntnu.krisefikser.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuration class for OpenAPI documentation.
 * Provides configuration for Swagger UI and OpenAPI documentation.
 * This includes server information, contact details, and license information.
 *
 * @since 1.0
 */
@Configuration
public class OpenApiConfig {

        /**
         * Configures the OpenAPI documentation for the application.
         * Sets up the server information, contact details, license, and basic API
         * information.
         *
         * @return The OpenAPI configuration object.
         * @since 1.0
         */
        @Bean
        public OpenAPI myOpenAPI() {
                Server devServer = new Server()
                                .url("http://localhost:8080")
                                .description("Development server");

                Contact contact = new Contact()
                                .name("Krisefikser Team")
                                .email("embret.roas@outlook.com");

                License mitLicense = new License()
                                .name("MIT License")
                                .url("https://choosealicense.com/licenses/mit/");

                Info info = new Info()
                                .title("Krisefikser API")
                                .version("1.0")
                                .contact(contact)
                                .description("This API exposes endpoints for the Krisefikser application.")
                                .license(mitLicense);

                return new OpenAPI()
                                .info(info)
                                .servers(List.of(devServer));
        }
}