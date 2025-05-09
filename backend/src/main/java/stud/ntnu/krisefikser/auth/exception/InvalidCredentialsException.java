package stud.ntnu.krisefikser.auth.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when a user provides invalid credentials during authentication.
 *
 * <p>This exception indicates that the provided email or password is incorrect.
 */
@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class InvalidCredentialsException extends RuntimeException {

  /**
   * Constructs a new InvalidCredentialsException with the default message.
   */
  public InvalidCredentialsException() {
    super("Invalid email or password");
  }

  /**
   * Constructs a new InvalidCredentialsException with the specified detail message.
   *
   * @param message the detail message
   */
  public InvalidCredentialsException(String message) {
    super(message);
  }
}