package stud.ntnu.krisefikser.auth.config;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import stud.ntnu.krisefikser.config.FrontendConfig;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {

  private final FrontendConfig frontendConfig;

  /**
   * Configures the security filter chain for the application.
   *
   * @param http The HttpSecurity object to configure
   * @param jwtAuthenticationEntryPoint The JwtAuthenticationEntryPoint to use
   * @param jwtAuthFilter The JwtAuthenticationFilter to use
   * @param authenticationProvider The AuthenticationProvider to use
   * @return The security filter chain
   * @throws Exception if an error occurs
   */
  @Bean
  public DefaultSecurityFilterChain securityFilterChain(
      HttpSecurity http,
      JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint,
      JwtAuthenticationFilter jwtAuthFilter,
      AuthenticationProvider authenticationProvider
  ) throws Exception {
    http.csrf(AbstractHttpConfigurer::disable)
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .authorizeHttpRequests(auth -> auth
            .requestMatchers(HttpMethod.GET, "/api/articles", "/api/articles/**").permitAll()
            .requestMatchers(HttpMethod.GET, "/api/map-points", "/api/map-points/**")
            .permitAll()
            .requestMatchers("/ws/**")
            .permitAll()
            .requestMatchers(HttpMethod.GET, "/api/map-point-types", "/api/map-point-types/**")
            .permitAll()
            .requestMatchers(HttpMethod.GET, "/api/events", "/api/events/**").permitAll()
            .requestMatchers("/api/auth/login", "/api/auth/register", "/api/auth/refresh", "api/auth/verify-email/**")
            .permitAll()
            .requestMatchers(HttpMethod.POST, "/api/email/**").permitAll()
            .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
            .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
            .anyRequest().authenticated())
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authenticationProvider(authenticationProvider)
        .exceptionHandling(
            handler -> handler.authenticationEntryPoint(jwtAuthenticationEntryPoint))
        .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(List.of(frontendConfig.getUrl()));
    configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    configuration.setAllowedHeaders(List.of("*"));
    configuration.setAllowCredentials(true);
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }
}
