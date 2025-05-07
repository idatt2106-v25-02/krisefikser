package stud.ntnu.krisefikser.common;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import stud.ntnu.krisefikser.auth.entity.Role;
import stud.ntnu.krisefikser.auth.entity.Role.RoleType;
import stud.ntnu.krisefikser.auth.repository.RoleRepository;
import stud.ntnu.krisefikser.auth.service.TokenService;
import stud.ntnu.krisefikser.config.FrontendConfig;
import stud.ntnu.krisefikser.user.entity.User;
import stud.ntnu.krisefikser.user.repository.UserRepository;

@TestConfiguration
@EnableMethodSecurity
public class TestSecurityConfig {

  @Bean
  public FrontendConfig frontendConfig() {
    FrontendConfig config = new FrontendConfig();
    config.setUrl("http://localhost:5173");
    return config;
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.csrf(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(auth -> auth
            .requestMatchers(HttpMethod.GET, "/api/articles", "/api/articles/**").permitAll()
            .requestMatchers(HttpMethod.GET, "/api/map-points", "/api/map-points/**")
            .permitAll()
            .requestMatchers(HttpMethod.GET, "/api/map-point-types", "/api/map-point-types/**")
            .permitAll()
            .requestMatchers(HttpMethod.GET, "/api/events", "/api/events/**").permitAll()
            .requestMatchers("/api/auth/login", "/api/auth/register", "/api/auth/refresh",
                "/api/auth/request-password-reset", "/api/auth/complete-password-reset")
            .permitAll()
            .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
            .anyRequest().authenticated())
        .exceptionHandling(
            exception -> exception.authenticationEntryPoint(
                new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)));
    return http.build();
  }

  @Bean
  public PasswordEncoder testPasswordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  @Primary
  public UserDetailsService testUserDetailsService() {
    return new org.springframework.security.provisioning.InMemoryUserDetailsManager(
        org.springframework.security.core.userdetails.User.withUsername("admin@example.com")
            .password("{noop}password")
            .roles("ADMIN", "USER")
            .build(),
        org.springframework.security.core.userdetails.User.withUsername("user@example.com")
            .password("{noop}password")
            .roles("USER")
            .build());
  }

  @Bean
  public UserRepository mockUserRepository() {
    UserRepository mockRepo = Mockito.mock(UserRepository.class);

    Role userRole = new Role();
    userRole.setId(1L);
    userRole.setName(RoleType.USER);

    Role adminRole = new Role();
    adminRole.setId(2L);
    adminRole.setName(RoleType.ADMIN);

    Set<Role> adminRoles = new HashSet<>();
    adminRoles.add(userRole);
    adminRoles.add(adminRole);

    User adminUser = User.builder()
        .id(UUID.randomUUID())
        .email("admin@example.com")
        .password("$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG")
        .firstName("Admin")
        .lastName("User")
        .roles(adminRoles)
        .build();

    Mockito.when(mockRepo.findByEmail("admin@example.com"))
        .thenReturn(Optional.of(adminUser));

    return mockRepo;
  }

  @Bean
  public RoleRepository mockRoleRepository() {
    RoleRepository mockRepo = Mockito.mock(RoleRepository.class);

    Role userRole = new Role();
    userRole.setId(1L);
    userRole.setName(RoleType.USER);

    Role adminRole = new Role();
    adminRole.setId(2L);
    adminRole.setName(RoleType.ADMIN);

    Mockito.when(mockRepo.findByName(RoleType.USER))
        .thenReturn(Optional.of(userRole));
    Mockito.when(mockRepo.findByName(RoleType.ADMIN))
        .thenReturn(Optional.of(adminRole));

    return mockRepo;
  }

  @Bean
  public TokenService mockTokenService() {
    return Mockito.mock(TokenService.class);
  }
}