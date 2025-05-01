package stud.ntnu.krisefikser.auth.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

  @Override
  public void commence(
      HttpServletRequest request,
      HttpServletResponse response,
      AuthenticationException authException
  ) throws IOException {
    ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
        HttpStatus.UNAUTHORIZED,
        "Unauthorized"
    );
    problemDetail.setType(
        URI.create("https://krisefikser.ntnu.stud/errors/" + HttpStatus.UNAUTHORIZED.value()));
    problemDetail.setTitle(HttpStatus.UNAUTHORIZED.getReasonPhrase());

    response.setContentType("application/problem+json");
    response.setStatus(HttpStatus.UNAUTHORIZED.value());
    response.getWriter().write(new ObjectMapper().writeValueAsString(problemDetail));
  }
}
