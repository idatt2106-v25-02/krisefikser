package stud.ntnu.krisefikser.user.exception;

/**
 * Exception thrown when a user tries to access a resource or perform an action they are not
 * authorized to.
 */
public class EmailAlreadyExistsException extends RuntimeException {

  /**
   * Constructs a new EmailAlreadyExistsException with the specified detail message.
   *
   * @param message the detail message
   */
  public EmailAlreadyExistsException(String message) {
    super(message);
  }
}
