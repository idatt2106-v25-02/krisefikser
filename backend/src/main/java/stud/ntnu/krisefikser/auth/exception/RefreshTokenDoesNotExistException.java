package stud.ntnu.krisefikser.auth.exception;

public class RefreshTokenDoesNotExistException extends RuntimeException {

  public RefreshTokenDoesNotExistException() {
    super("Refresh token does not exist");
  }

  public RefreshTokenDoesNotExistException(String message) {
    super(message);
  }
}
