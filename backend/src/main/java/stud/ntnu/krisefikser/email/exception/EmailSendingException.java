package stud.ntnu.krisefikser.email.exception;

/**
 * Custom exception class for handling email sending errors.
 *
 * <p>This exception is thrown when there is an issue with sending an email, such as a network
 * error, authentication failure, or any other unexpected error that occurs during the email
 * sending
 * </p>
 */
public class EmailSendingException extends RuntimeException {

  /**
   * Constructs a new EmailSendingException with the specified detail message.
   *
   * @param message the detail message
   */
  public EmailSendingException(String message) {
    super(message);
  }

  /**
   * Constructs a new EmailSendingException with the specified detail message and cause.
   *
   * @param message the detail message
   * @param cause   the cause of the exception
   */
  public EmailSendingException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Constructs a new EmailSendingException with the specified cause.
   *
   * @param cause the cause of the exception
   */
  public EmailSendingException(Throwable cause) {
    super(cause);
  }
}
