package stud.ntnu.krisefikser.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@TestConfiguration
@EnableWebSecurity
public class TestSecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        // GET requests - publicly accessible
                        .requestMatchers(HttpMethod.GET, "/api/articles", "/api/articles/**")
                        .permitAll()
                        // POST, PUT, DELETE requests - only accessible by ADMIN
                        .requestMatchers(HttpMethod.POST, "/api/articles")
                        .hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/articles/**")
                        .hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/articles/**")
                        .hasRole("ADMIN")
                        .anyRequest()
                        .authenticated());

        return http.build();
    }
}