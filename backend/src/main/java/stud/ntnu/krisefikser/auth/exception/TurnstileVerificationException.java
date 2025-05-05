package stud.ntnu.krisefikser.auth.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when Turnstile verification fails.
 *
 * @since 1.0
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class TurnstileVerificationException extends RuntimeException {

  /**
   * Constructs a new TurnstileVerificationException with the default detail message.
   */
  public TurnstileVerificationException() {
    super("CAPTCHA verification failed. Please try again.");
  }

  /**
   * Constructs a new TurnstileVerificationException with the specified detail message.
   *
   * @param message the detail message
   */
  public TurnstileVerificationException(String message) {
    super(message);
  }
}
