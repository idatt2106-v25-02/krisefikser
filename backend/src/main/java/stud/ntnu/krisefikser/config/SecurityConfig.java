package stud.ntnu.krisefikser.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(auth -> auth
                        // GET requests - publicly accessible
                        .requestMatchers(HttpMethod.GET, "/api/articles", "/api/articles/**")
                        .permitAll()
                        // Map points and map point types GET endpoints - publicly accessible
                        .requestMatchers(HttpMethod.GET, "/api/map-points", "/api/map-points/**")
                        .permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/map-point-types", "/api/map-point-types/**")
                        .permitAll()
                        // Events GET endpoints - publicly accessible
                        .requestMatchers(HttpMethod.GET, "/api/events", "/api/events/**")
                        .permitAll()
                        // Swagger UI and OpenAPI endpoints - publicly accessible
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html")
                        .permitAll()
                        // POST, PUT, DELETE requests - only accessible by ADMIN
                        .requestMatchers(HttpMethod.POST, "/api/articles")
                        .hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/articles/**")
                        .hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/articles/**")
                        .hasRole("ADMIN")
                        // Events POST, PUT, DELETE requests - only accessible by ADMIN
                        .requestMatchers(HttpMethod.POST, "/api/events")
                        .hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/events/**")
                        .hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/events/**")
                        .hasRole("ADMIN")
                        .anyRequest()
                        .authenticated());

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:5173"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
