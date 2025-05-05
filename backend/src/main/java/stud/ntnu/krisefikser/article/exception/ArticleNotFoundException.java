package stud.ntnu.krisefikser.article.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when an article is not found in the system.
 *
 * @since 1.0
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ArticleNotFoundException extends RuntimeException {

  /**
   * Constructs a new ArticleNotFoundException with the specified detail message.
   *
   * @param message the detail message
   */
  public ArticleNotFoundException(String message) {
    super(message);
  }
}