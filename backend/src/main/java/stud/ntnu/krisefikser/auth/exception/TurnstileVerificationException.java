package stud.ntnu.krisefikser.auth.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class TurnstileVerificationException extends RuntimeException {

  public TurnstileVerificationException() {
    super("CAPTCHA verification failed. Please try again.");
  }

  public TurnstileVerificationException(String message) {
    super(message);
  }
}
