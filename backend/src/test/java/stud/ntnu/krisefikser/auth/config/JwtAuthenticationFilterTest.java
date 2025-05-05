package stud.ntnu.krisefikser.auth.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import stud.ntnu.krisefikser.auth.service.CustomUserDetailsService;
import stud.ntnu.krisefikser.auth.service.TokenService;

@ExtendWith(MockitoExtension.class)
public class JwtAuthenticationFilterTest {

  @Mock
  private TokenService tokenService;

  @Mock
  private CustomUserDetailsService userDetailsService;

  @Mock
  private HttpServletRequest request;

  @Mock
  private HttpServletResponse response;

  @Mock
  private FilterChain filterChain;

  @InjectMocks
  private JwtAuthenticationFilter jwtAuthenticationFilter;

  private UserDetails userDetails;

  @BeforeEach
  void setUp() {
    userDetails = User.builder()
        .username("test@example.com")
        .password("password")
        .authorities(Collections.emptyList())
        .build();

    SecurityContextHolder.clearContext();
  }

  @Test
  void doFilterInternal_WithValidToken_ShouldAuthenticate() throws ServletException, IOException {
    // Arrange
    when(request.getHeader("Authorization")).thenReturn("Bearer valid-token");
    when(tokenService.extractEmail("valid-token")).thenReturn("test@example.com");
    when(userDetailsService.loadUserByUsername("test@example.com")).thenReturn(userDetails);
    when(tokenService.isValid("valid-token", userDetails)).thenReturn(true);
    when(tokenService.isAccessToken("valid-token")).thenReturn(true); // Add this line

    // Act
    jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

    // Assert
    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
    assertThat(SecurityContextHolder.getContext().getAuthentication().getName()).isEqualTo(
        "test@example.com");

    verify(filterChain).doFilter(request, response);
  }

  @Test
  void doFilterInternal_WithoutAuthHeader_ShouldNotAuthenticate()
      throws ServletException, IOException {
    // Arrange
    when(request.getHeader("Authorization")).thenReturn(null);

    // Act
    jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

    // Assert
    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();

    verify(filterChain).doFilter(request, response);
    verify(tokenService, never()).extractEmail(anyString());
  }

  @Test
  void doFilterInternal_WithInvalidTokenFormat_ShouldNotAuthenticate()
      throws ServletException, IOException {
    // Arrange
    when(request.getHeader("Authorization")).thenReturn("InvalidFormat");

    // Act
    jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

    // Assert
    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();

    verify(filterChain).doFilter(request, response);
    verify(tokenService, never()).extractEmail(anyString());
  }

  @Test
  void doFilterInternal_WithInvalidToken_ShouldNotAuthenticate()
      throws ServletException, IOException {
    // Arrange
    when(request.getHeader("Authorization")).thenReturn("Bearer invalid-token");
    when(tokenService.extractEmail("invalid-token")).thenReturn("test@example.com");
    when(userDetailsService.loadUserByUsername("test@example.com")).thenReturn(userDetails);
    when(tokenService.isAccessToken("invalid-token")).thenReturn(true); // Add this line
    when(tokenService.isValid("invalid-token", userDetails)).thenReturn(false);

    // Act
    jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

    // Assert
    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();

    verify(filterChain).doFilter(request, response);
  }

  @Test
  void doFilterInternal_WithExistingAuthentication_ShouldSkipProcessing()
      throws ServletException, IOException {
    // Arrange
    when(request.getHeader("Authorization")).thenReturn("Bearer valid-token");

    // Set an existing authentication
    SecurityContextHolder.getContext().setAuthentication(
        new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
            "existing-user", null, Collections.emptyList()));

    // Act
    jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

    // Assert
    assertThat(SecurityContextHolder.getContext().getAuthentication().getName()).isEqualTo(
        "existing-user");

    verify(filterChain).doFilter(request, response);
    verify(tokenService, never()).extractEmail(anyString());
  }
}