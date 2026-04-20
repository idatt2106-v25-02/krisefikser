package stud.ntnu.krisefikser;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import stud.ntnu.krisefikser.auth.exception.InvalidTokenException;
import stud.ntnu.krisefikser.user.exception.UserNotFoundException;

class GlobalExceptionHandlerTest {

  private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

  @Test
  void handleInvalidTokenException_shouldReturnUnauthorizedProblem() {
    var problem = handler.handleInvalidTokenException(new InvalidTokenException("bad token"));

    assertThat(problem.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
    assertThat(problem.getDetail()).isEqualTo("bad token");
  }

  @Test
  void handleUserDoesNotExistException_shouldReturnNotFoundProblem() {
    var problem = handler.handleUserDoesNotExistException(new UserNotFoundException());

    assertThat(problem.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
    assertThat(problem.getDetail()).contains("User not found");
  }
}
