package stud.ntnu.krisefikser.email.exception;

/**
 * Custom exception class for handling email template errors.
 *
 * <p>This exception is thrown when there is an issue with loading or processing an email template,
 * such as a missing file, invalid syntax, or any other unexpected error that occurs during the
 * template processing.
 * </p>
 */
public class EmailTemplateException extends RuntimeException {

  /**
   * Constructs a new EmailTemplateException with the specified detail message.
   *
   * @param message the detail message
   */
  public EmailTemplateException(String message) {
    super(message);
  }

  /**
   * Constructs a new EmailTemplateException with the specified detail message and cause.
   *
   * @param message the detail message
   * @param cause   the cause of the exception
   */
  public EmailTemplateException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Constructs a new EmailTemplateException with the specified cause.
   *
   * @param cause the cause of the exception
   */
  public EmailTemplateException(Throwable cause) {
    super(cause);
  }

}
