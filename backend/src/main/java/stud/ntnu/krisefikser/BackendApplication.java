package stud.ntnu.krisefikser;

import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;
import stud.ntnu.krisefikser.config.FrontendConfig;

/**
 * Main application class for the backend service. This class serves as the entry point for the
 * Spring Boot application.
 *
 * <p>It initializes the application context, loads the configuration, and starts the embedded web
 * server.
 */
@SpringBootApplication
@RequiredArgsConstructor
public class BackendApplication {

  private final FrontendConfig frontendConfig;
  private final Environment environment;

  /**
   * Main method to run the Spring Boot application.
   *
   * @param args command line arguments
   */
  public static void main(String[] args) {
    ConfigurableApplicationContext context = SpringApplication.run(BackendApplication.class, args);
    BackendApplication app = context.getBean(BackendApplication.class);
    System.out.println("Active profiles: " + Arrays.toString(app.environment.getActiveProfiles()));
    System.out.println(
        "Application started with data seeding enabled. Database will be populated with sample "
            + "data if empty.");
    System.out.println("Frontend URL: " + app.frontendConfig.getUrl());
  }

}
