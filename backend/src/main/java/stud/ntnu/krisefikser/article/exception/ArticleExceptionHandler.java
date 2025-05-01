package stud.ntnu.krisefikser.article.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import stud.ntnu.krisefikser.common.ProblemDetailUtils;

/**
 * Exception handler for the article domain.
 * 
 * <p>This class provides centralized exception handling for article-related exceptions.
 * It translates exceptions into RFC 7807 Problem Details.
 * </p>
 */
@Slf4j
@RestControllerAdvice(basePackages = "stud.ntnu.krisefikser.article")
public class ArticleExceptionHandler {

  /**
   * Handles exceptions thrown when an article is not found.
   *
   * @param exception the article not found exception
   * @return a problem detail with NOT_FOUND status and the exception message
   */
  @ExceptionHandler(ArticleNotFoundException.class)
  public ProblemDetail handleArticleNotFoundException(ArticleNotFoundException exception) {
    log.warn("Article not found: {}", exception.getMessage());
    return ProblemDetailUtils.createDomainProblemDetail(HttpStatus.NOT_FOUND, exception.getMessage(), "article");
  }
} 