package stud.ntnu.krisefikser.article.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Exception handler for the article domain.
 *
 * <p>This class provides centralized exception handling for article-related exceptions.
 * It translates exceptions into RFC 7807 Problem Details.
 * </p>
 */
@Slf4j
@RestControllerAdvice(basePackages = {"stud.ntnu.krisefikser.article"})
public class ArticleExceptionHandler {

}