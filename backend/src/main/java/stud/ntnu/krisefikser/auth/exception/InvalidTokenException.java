package stud.ntnu.krisefikser.auth.exception;

/**
 * Exception thrown when a user tries to access a resource or perform an action they are not
 * authorized to.
 */
public class InvalidTokenException extends RuntimeException {

  /**
   * Constructs a new InvalidTokenException with the specified detail message.
   */
  public InvalidTokenException() {
    super("Invalid token");
  }
}
