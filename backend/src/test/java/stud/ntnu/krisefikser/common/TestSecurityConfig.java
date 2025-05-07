package stud.ntnu.krisefikser.common;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.config.http.SessionCreationPolicy;
import stud.ntnu.krisefikser.auth.config.JwtAuthenticationFilter;
import stud.ntnu.krisefikser.auth.entity.Role;
import stud.ntnu.krisefikser.auth.entity.Role.RoleType;
import stud.ntnu.krisefikser.auth.repository.RoleRepository;
import stud.ntnu.krisefikser.auth.service.CustomUserDetailsService;
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
  public SecurityFilterChain securityFilterChain(
      HttpSecurity http,
      JwtAuthenticationFilter jwtAuthFilter,
      AuthenticationProvider authenticationProvider
  ) throws Exception {
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
            .requestMatchers("/api/auth/login", "/api/auth/register", "/api/auth/refresh",
                "/api/auth/request-password-reset", "/api/auth/complete-password-reset")
            .permitAll()
            .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
            .anyRequest().authenticated())
        .authenticationProvider(authenticationProvider)
        .addFilterBefore(jwtAuthFilter, org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class)
        .exceptionHandling(
            exception -> exception.authenticationEntryPoint(
                new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
    return http.build();
  }

  @Bean
  public PasswordEncoder testPasswordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public UserDetailsService inMemoryUserDetailsService() {
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
  public AuthenticationProvider authenticationProvider(
      UserDetailsService userDetailsService,
      PasswordEncoder passwordEncoder
  ) {
    DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
    authProvider.setUserDetailsService(userDetailsService);
    authProvider.setPasswordEncoder(passwordEncoder);
    return authProvider;
  }

  @Bean
  @Primary
  public UserRepository mockUserRepository() {
    UserRepository mockRepo = Mockito.mock(UserRepository.class);

    Role userRole = new Role();
    userRole.setId(1L);
    userRole.setName(RoleType.USER);

    Set<Role> userRoles = new HashSet<>();
    userRoles.add(userRole);

    // Mock the test user
    User testUser = User.builder()
        .id(UUID.randomUUID())
        .email("newuser@example.com")
        .password("$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG") // encoded Password123!
        .firstName("New")
        .lastName("User")
        .roles(userRoles)
        .emailVerified(true)
        .build();

    Mockito.when(mockRepo.findByEmail("newuser@example.com"))
        .thenReturn(Optional.of(testUser));

    Mockito.when(mockRepo.save(any(User.class))).thenAnswer(invocation -> {
      User user = invocation.getArgument(0);
      if (user.getId() == null) {
        user = User.builder()
            .id(UUID.randomUUID())
            .email(user.getEmail())
            .password(user.getPassword())
            .firstName(user.getFirstName())
            .lastName(user.getLastName())
            .roles(userRoles)
            .emailVerified(true)
            .build();
      }
      return user;
    });

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
    TokenService mockService = Mockito.mock(TokenService.class);
    
    // Mock token validation to always return true
    Mockito.when(mockService.isValid(anyString(), any())).thenReturn(true);
    Mockito.when(mockService.isAccessToken(anyString())).thenReturn(true);
    
    // Mock token generation to return predictable tokens
    Mockito.when(mockService.generateAccessToken(any())).thenReturn("test-access-token");
    Mockito.when(mockService.generateRefreshToken(any())).thenReturn("test-refresh-token");
    
    // Mock email extraction to return the correct email
    Mockito.when(mockService.extractEmail(anyString())).thenAnswer(invocation -> {
      String token = invocation.getArgument(0);
      if (token.startsWith("Bearer ")) {
        token = token.substring(7);
      }
      return "newuser@example.com";
    });
    
    return mockService;
  }

  @Bean
  public JwtAuthenticationFilter jwtAuthenticationFilter(
      TokenService tokenService,
      CustomUserDetailsService userDetailsService
  ) {
    return new JwtAuthenticationFilter(tokenService, userDetailsService);
  }

  @Bean
  @Primary
  public CustomUserDetailsService testUserDetailsService(UserRepository userRepository) {
    return new CustomUserDetailsService(userRepository);
  }
}