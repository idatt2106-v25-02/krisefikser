package stud.ntnu.krisefikser.common;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import stud.ntnu.krisefikser.config.FrontendConfig;

@TestConfiguration
public class RepositoryTestConfig {

  @Bean
  public FrontendConfig frontendConfig() {
    // Create with default/test values
    return new FrontendConfig();
  }
}