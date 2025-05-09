package stud.ntnu.krisefikser.auth.exception;

import org.springframework.security.core.AuthenticationException;

/**
 * Exception thrown when a user's email is not verified during authentication.
 *
 * <p>This exception indicates that the user has not verified their email address, which is
 * required for authentication to proceed.
 */
public class EmailNotVerifiedException extends AuthenticationException {

  /**
   * Constructs a new EmailNotVerifiedException with the specified detail message.
   *
   * @param msg the detail message
   */
  public EmailNotVerifiedException(String msg, Throwable cause) {
    super(msg, cause);
  }

  /**
   * Constructs a new EmailNotVerifiedException with the specified detail message.
   *
   * @param msg the detail message
   */
  public EmailNotVerifiedException(String msg) {
    super(msg);
  }
} 