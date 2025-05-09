package stud.ntnu.krisefikser.auth.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import stud.ntnu.krisefikser.auth.service.CustomUserDetailsService;
import stud.ntnu.krisefikser.user.repository.UserRepository;

/**
 * Configuration class for authentication-related beans.
 *
 * <p>This class is responsible for creating and configuring beans related to user authentication,
 * including the user details service, authentication provider, password encoder, and authentication
 * manager.
 */
@org.springframework.context.annotation.Configuration
@EnableConfigurationProperties({JwtProperties.class, TurnstileProperties.class})
@RequiredArgsConstructor
public class Configuration {

  /**
   * The UserRepository instance used for user-related operations.
   *
   * <p>This field is injected via constructor injection and is used to create the custom user
   * details service.
   */
  @Bean
  public UserDetailsService userDetailsService(UserRepository userRepository) {
    return new CustomUserDetailsService(userRepository);
  }

  /**
   * The AuthenticationProvider instance used for authentication.
   *
   * <p>This method creates and configures a DaoAuthenticationProvider with the custom user details
   * service and password encoder.
   *
   * @param userDetailsService the custom user details service
   * @return the configured AuthenticationProvider
   */
  @Bean
  public AuthenticationProvider authenticationProvider(UserDetailsService userDetailsService) {
    DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
    provider.setUserDetailsService(userDetailsService);
    provider.setPasswordEncoder(encoder());

    return provider;
  }

  /**
   * The PasswordEncoder instance used for encoding passwords.
   *
   * <p>This method creates and returns a new instance of BCryptPasswordEncoder, which is used to
   * securely hash and verify passwords.
   *
   * @return the configured PasswordEncoder
   */
  @Bean
  public PasswordEncoder encoder() {
    return new BCryptPasswordEncoder();
  }

  /**
   * The AuthenticationManager instance used for managing authentication.
   *
   * <p>This method creates and returns a new instance of AuthenticationManager, which is used to
   * handle authentication requests.
   *
   * @param configuration the authentication configuration
   * @return the configured AuthenticationManager
   * @throws Exception if an error occurs while creating the AuthenticationManager
   */
  @Bean
  public AuthenticationManager authenticationManager(
      AuthenticationConfiguration configuration) throws Exception {
    return configuration.getAuthenticationManager();
  }
}
