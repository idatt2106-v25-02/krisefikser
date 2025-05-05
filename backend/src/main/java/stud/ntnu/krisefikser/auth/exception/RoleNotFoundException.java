package stud.ntnu.krisefikser.auth.exception;

/**
 * Exception thrown when a user tries to access a resource or perform an action they are not
 * authorized to.
 */
public class RoleNotFoundException extends RuntimeException {

  /**
   * Constructs a new RoleNotFoundException with the specified detail message.
   */
  public RoleNotFoundException() {
    super("Role not found");
  }
}
