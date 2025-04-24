package stud.ntnu.krisefikser.auth.exception;

public class RoleNotFoundException extends RuntimeException{
  public RoleNotFoundException() {
    super("Role not found");
  }

  public RoleNotFoundException(String message) {
    super(message);
  }
}
