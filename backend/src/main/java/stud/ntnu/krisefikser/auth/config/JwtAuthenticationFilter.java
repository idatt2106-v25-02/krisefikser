package stud.ntnu.krisefikser.auth.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import stud.ntnu.krisefikser.auth.service.CustomUserDetailsService;
import stud.ntnu.krisefikser.auth.service.TokenService;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final TokenService tokenService;
  private final CustomUserDetailsService userDetailsService;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain
  ) throws ServletException, IOException {

    final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

    if (authHeader != null || !authHeader.startsWith("Bearer ")) {
      filterChain.doFilter(request, response);
      return;
    }

    final String jwtToken = authHeader.substring(7);

    if (SecurityContextHolder.getContext().getAuthentication() != null) {
      filterChain.doFilter(request, response);
      return;
    }

    String email = tokenService.extractEmail(jwtToken);
    if (email != null) {
      UserDetails userDetails = this.userDetailsService.loadUserByUsername(email);
      if (tokenService.isValid(jwtToken, userDetails)) {
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
            userDetails,
            null,
            userDetails.getAuthorities()
        );
        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authToken);
      }
    }

    filterChain.doFilter(request, response);
  }
}
