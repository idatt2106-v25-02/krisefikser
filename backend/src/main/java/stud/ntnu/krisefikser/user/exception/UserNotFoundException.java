package stud.ntnu.krisefikser.user.exception;

import java.util.UUID;

/**
 * Exception thrown when a user is not found in the system.
 *
 * <p>This exception is typically thrown when a user tries to access a resource or perform an
 * action that requires a valid user account, but the specified user does not exist in the
 * database.
 */
public class UserNotFoundException extends RuntimeException {

  /**
   * Constructs a new UserDoesNotExistException with the specified detail message.
   *
   * @param message the detail message
   */
  public UserNotFoundException(String email) {
    super("User with email " + email + " does not exist");
  }

  /**
   * Constructs a new UserDoesNotExistException with the specified user ID.
   *
   * @param userId the ID of the user that was not found
   */
  public UserNotFoundException(UUID userId) {
    super("User with ID " + userId + " does not exist");
  }
}
