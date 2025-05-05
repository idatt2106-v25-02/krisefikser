package stud.ntnu.krisefikser.auth.exception;

/**
 * Exception thrown when a user tries to access a resource or perform an action they are not
 * authorized to.
 */
public class RefreshTokenDoesNotExistException extends RuntimeException {

  /**
   * Constructs a new RefreshTokenDoesNotExistException with the specified detail message.
   */
  public RefreshTokenDoesNotExistException() {
    super("Refresh token does not exist");
  }
}
