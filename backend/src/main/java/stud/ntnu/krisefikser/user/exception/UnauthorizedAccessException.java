package stud.ntnu.krisefikser.user.exception;

/**
 * Exception thrown when a user tries to access a resource or perform an action they are not
 * authorized to.
 */
public class UnauthorizedAccessException extends RuntimeException {

  /**
   * Constructs a new UnauthorizedAccessException with the specified detail message.
   *
   * @param message the detail message
   */
  public UnauthorizedAccessException(String message) {
    super(message);
  }
}