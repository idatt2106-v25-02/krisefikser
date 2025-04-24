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

@org.springframework.context.annotation.Configuration
@EnableConfigurationProperties({JwtProperties.class})
@RequiredArgsConstructor
public class Configuration {

  @Bean
  public UserDetailsService userDetailsService(UserRepository userRepository) {
    return new CustomUserDetailsService(userRepository);
  }

  @Bean
  public AuthenticationProvider authenticationProvider(UserDetailsService userDetailsService) {
    DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
    provider.setUserDetailsService(userDetailsService);
    provider.setPasswordEncoder(encoder());

    return provider;
  }

  @Bean
  public AuthenticationManager authenticationManager(
      AuthenticationConfiguration configuration) throws Exception {
    return configuration.getAuthenticationManager();
  }

  @Bean
  public PasswordEncoder encoder() {
    return new BCryptPasswordEncoder();
  }
}
