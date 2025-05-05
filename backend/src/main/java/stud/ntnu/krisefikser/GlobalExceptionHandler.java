package stud.ntnu.krisefikser;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.persistence.EntityNotFoundException;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;
import stud.ntnu.krisefikser.article.exception.ArticleNotFoundException;
import stud.ntnu.krisefikser.auth.exception.InvalidTokenException;
import stud.ntnu.krisefikser.auth.exception.RefreshTokenDoesNotExistException;
import stud.ntnu.krisefikser.auth.exception.TurnstileVerificationException;
import stud.ntnu.krisefikser.common.ProblemDetailUtils;
import stud.ntnu.krisefikser.household.exception.HouseholdNotFoundException;
import stud.ntnu.krisefikser.user.exception.EmailAlreadyExistsException;
import stud.ntnu.krisefikser.user.exception.UnauthorizedAccessException;
import stud.ntnu.krisefikser.user.exception.UserDoesNotExistException;

/**
 * Global exception handler for the Krisefikser application.
 *
 * <p>This class provides centralized exception handling across all REST controllers
 * in the application. It translates exceptions into RFC 7807 Problem Details (implemented as
 * {@link ProblemDetail} in Spring) to provide consistent, machine-readable error responses to API
 * clients.</p>
 *
 * <p>Error responses include:
 * <ul>
 *   <li>HTTP status code appropriate to the exception</li>
 *   <li>A type URI that identifies the error category</li>
 *   <li>A title that corresponds to the HTTP status phrase</li>
 *   <li>A detailed message explaining the specific error</li>
 *   <li>A timestamp indicating when the error occurred</li>
 * </ul>
 * </p>
 *
 * <p>The handler categorizes exceptions into the following groups:
 * <ul>
 *   <li>Domain-specific custom exceptions (e.g., resource not found)</li>
 *   <li>Authentication and authorization exceptions</li>
 *   <li>JWT processing exceptions</li>
 *   <li>Bean validation exceptions</li>
 *   <li>HTTP request mapping exceptions</li>
 *   <li>General fallback exceptions</li>
 * </ul>
 * </p>
 *
 * @author Krisefikser Development Team
 * @see org.springframework.http.ProblemDetail
 * @see org.springframework.web.bind.annotation.RestControllerAdvice
 * @since 1.0
 */
@Slf4j
@RestControllerAdvice(basePackages = {"stud.ntnu.krisefikser"})
public class GlobalExceptionHandler {

  // ===== Domain-specific custom exceptions =====

  /**
   * Handles exceptions thrown when a household entity cannot be found.
   *
   * @param exception the household not found exception
   * @return a problem detail with NOT_FOUND status and the exception message
   */
  @ExceptionHandler(HouseholdNotFoundException.class)
  public ProblemDetail handleHouseholdNotFoundException(HouseholdNotFoundException exception) {
    log.warn("Household not found: {}", exception.getMessage());
    return ProblemDetailUtils.createDomainProblemDetail(HttpStatus.NOT_FOUND,
        exception.getMessage(), "household");
  }

  /**
   * Handles exceptions thrown when an article is not found.
   *
   * @param exception the article not found exception
   * @return a problem detail with NOT_FOUND status and the exception message
   */
  @ExceptionHandler(ArticleNotFoundException.class)
  public ProblemDetail handleArticleNotFoundException(ArticleNotFoundException exception) {
    log.warn("Article not found: {}", exception.getMessage());
    return ProblemDetailUtils.createDomainProblemDetail(HttpStatus.NOT_FOUND,
        exception.getMessage(), "article");
  }

  // ===== Authentication-related exceptions =====

  /**
   * Handles exceptions thrown when a JWT token is invalid.
   *
   * @param exception the invalid token exception
   * @return a problem detail with UNAUTHORIZED status and the exception message
   */
  @ExceptionHandler(InvalidTokenException.class)
  public ProblemDetail handleInvalidTokenException(InvalidTokenException exception) {
    log.error("Invalid token: {}", exception.getMessage());
    return ProblemDetailUtils.createDomainProblemDetail(HttpStatus.UNAUTHORIZED,
        exception.getMessage(), "auth");
  }

  /**
   * Handles exceptions thrown when a refresh token does not exist in the system.
   *
   * @param exception the refresh token does not exist exception
   * @return a problem detail with UNAUTHORIZED status and the exception message
   */
  @ExceptionHandler(RefreshTokenDoesNotExistException.class)
  public ProblemDetail handleRefreshTokenDoesNotExistException(
      RefreshTokenDoesNotExistException exception) {
    log.error("Refresh token does not exist: {}", exception.getMessage());
    return ProblemDetailUtils.createDomainProblemDetail(HttpStatus.UNAUTHORIZED,
        exception.getMessage(), "auth");
  }

  @ExceptionHandler(TurnstileVerificationException.class)
  public ProblemDetail handleTurnstileVerificationException(
      TurnstileVerificationException exception) {
    return ProblemDetailUtils.createProblemDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
  }

  // ===== User related exceptions =====

  /**
   * Handles exceptions thrown when attempting to create a user with an email that already exists in
   * the system.
   *
   * @param exception the email already exists exception
   * @return a problem detail with CONFLICT status and the exception message
   */

  @ExceptionHandler(EmailAlreadyExistsException.class)
  public ProblemDetail handleEmailAlreadyExistsException(EmailAlreadyExistsException exception) {
    log.warn("Email already exists: {}", exception.getMessage());
    return ProblemDetailUtils.createDomainProblemDetail(HttpStatus.CONFLICT, exception.getMessage(),
        "user");
  }

  /**
   * Handles exceptions thrown when a user cannot be found in the system.
   *
   * @param exception the user does not exist exception
   * @return a problem detail with NOT_FOUND status and the exception message
   */
  @ExceptionHandler(UserDoesNotExistException.class)
  public ProblemDetail handleUserDoesNotExistException(UserDoesNotExistException exception) {
    log.warn("User does not exist: {}", exception.getMessage());
    return ProblemDetailUtils.createDomainProblemDetail(HttpStatus.NOT_FOUND,
        exception.getMessage(), "user");
  }

  // ===== JWT specific exceptions =====

  /**
   * Handles exceptions thrown when a JWT token has expired.
   *
   * @param exception the expired JWT exception
   * @return a problem detail with UNAUTHORIZED status and a message indicating token expiration
   */
  @ExceptionHandler(ExpiredJwtException.class)
  public ProblemDetail handleExpiredJwtException(ExpiredJwtException exception) {
    log.warn("JWT token has expired: {}", exception.getMessage());
    return ProblemDetailUtils.createDomainProblemDetail(HttpStatus.UNAUTHORIZED,
        "JWT token has expired", "auth");
  }

  /**
   * Handles exceptions thrown when a JWT token is malformed.
   *
   * @param exception the malformed JWT exception
   * @return a problem detail with UNAUTHORIZED status and a message indicating invalid token format
   */
  @ExceptionHandler(MalformedJwtException.class)
  public ProblemDetail handleMalformedJwtException(MalformedJwtException exception) {
    log.warn("Invalid JWT token format: {}", exception.getMessage());
    return ProblemDetailUtils.createDomainProblemDetail(HttpStatus.UNAUTHORIZED,
        "Invalid JWT token format", "auth");
  }

  /**
   * Handles exceptions thrown when a JWT token has an invalid signature.
   *
   * @param exception the signature exception
   * @return a problem detail with UNAUTHORIZED status and a message indicating invalid signature
   */
  @ExceptionHandler(SignatureException.class)
  public ProblemDetail handleSignatureException(SignatureException exception) {
    log.error("Invalid JWT signature: {}", exception.getMessage());
    return ProblemDetailUtils.createDomainProblemDetail(HttpStatus.UNAUTHORIZED,
        "Invalid JWT signature", "auth");
  }

  /**
   * Handles exceptions thrown when a JWT token is unsupported.
   *
   * @param exception the unsupported JWT exception
   * @return a problem detail with UNAUTHORIZED status and a message indicating unsupported token
   */
  @ExceptionHandler(UnsupportedJwtException.class)
  public ProblemDetail handleUnsupportedJwtException(UnsupportedJwtException exception) {
    log.warn("Unsupported JWT token: {}", exception.getMessage());
    return ProblemDetailUtils.createDomainProblemDetail(HttpStatus.UNAUTHORIZED,
        "Unsupported JWT token", "auth");
  }

  // ===== Spring Security exceptions =====

  /**
   * Handles access denied exceptions thrown by Spring Security.
   *
   * @param exception the access denied exception
   * @return a problem detail with FORBIDDEN status and an access denied message
   */
  @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
  public ProblemDetail handleAccessDeniedException(
      org.springframework.security.access.AccessDeniedException exception) {
    log.error("Access denied: {}", exception.getMessage());
    return ProblemDetailUtils.createProblemDetail(HttpStatus.FORBIDDEN, "Access denied");
  }

  /**
   * Handles exceptions thrown when credentials are invalid during authentication.
   *
   * @param exception the bad credentials exception
   * @return a problem detail with UNAUTHORIZED status and an invalid credentials message
   */
  @ExceptionHandler(BadCredentialsException.class)
  public ProblemDetail handleBadCredentialsException(BadCredentialsException exception) {
    log.error("Invalid credentials: {}", exception.getMessage());
    return ProblemDetailUtils.createDomainProblemDetail(HttpStatus.UNAUTHORIZED,
        "Invalid credentials", "auth");
  }

  /**
   * Handles authentication exceptions thrown by Spring Security when no valid authentication is
   * present.
   *
   * @param exception the authentication exception
   * @return a problem detail with UNAUTHORIZED status and an authentication required message
   */
  @ExceptionHandler(AuthenticationException.class)
  public ProblemDetail handleAuthenticationException(AuthenticationException exception) {
    log.error("Authentication required: {}", exception.getMessage());
    return ProblemDetailUtils.createDomainProblemDetail(HttpStatus.UNAUTHORIZED,
        "Authentication required", "auth");
  }

  // ===== Bean validation exceptions =====

  /**
   * Handles exceptions thrown when method arguments fail validation.
   *
   * <p>This handler collects all field errors and concatenates them into a single
   * error message with the format "field1: message1, field2: message2".</p>
   *
   * @param exception the method argument not valid exception
   * @return a problem detail with BAD_REQUEST status and concatenated validation errors
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ProblemDetail handleValidationException(MethodArgumentNotValidException exception) {
    String errorMessage = exception.getBindingResult().getFieldErrors()
        .stream()
        .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
        .collect(Collectors.joining(", "));
    log.warn("Validation error: {}", errorMessage);
    return ProblemDetailUtils.createProblemDetail(HttpStatus.BAD_REQUEST, errorMessage);
  }

  // ===== Request mapping exceptions =====

  /**
   * Handles exceptions thrown when a request method is not supported.
   *
   * @param exception the HTTP request method not supported exception
   * @return a problem detail with METHOD_NOT_ALLOWED status and a descriptive message
   */
  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  public ProblemDetail handleMethodNotSupportedException(
      HttpRequestMethodNotSupportedException exception) {
    log.warn("Method not supported: {} for path {}", exception.getMethod(), exception.getMessage());
    return ProblemDetailUtils.createProblemDetail(
        HttpStatus.METHOD_NOT_ALLOWED,
        "Method " + exception.getMethod() + " is not supported for this request"
    );
  }

  /**
   * Handles exceptions thrown when a media type is not supported.
   *
   * @param exception the HTTP media type not supported exception
   * @return a problem detail with UNSUPPORTED_MEDIA_TYPE status and a descriptive message
   */
  @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
  public ProblemDetail handleMediaTypeNotSupportedException(
      HttpMediaTypeNotSupportedException exception) {
    log.warn("Media type not supported: {}", exception.getContentType());
    return ProblemDetailUtils.createProblemDetail(
        HttpStatus.UNSUPPORTED_MEDIA_TYPE,
        "Media type " + exception.getContentType() + " is not supported"
    );
  }

  /**
   * Handles exceptions thrown when a required request parameter is missing.
   *
   * @param exception the missing servlet request parameter exception
   * @return a problem detail with BAD_REQUEST status and a message identifying the missing
   * parameter
   */
  @ExceptionHandler(MissingServletRequestParameterException.class)
  public ProblemDetail handleMissingParameterException(
      MissingServletRequestParameterException exception) {
    log.warn("Missing required parameter: {}", exception.getParameterName());
    return ProblemDetailUtils.createProblemDetail(
        HttpStatus.BAD_REQUEST,
        "Required parameter '" + exception.getParameterName() + "' is missing"
    );
  }

  // ===== Resource not found exceptions =====

  /**
   * Handles JPA entity not found exceptions.
   *
   * @param exception the entity not found exception
   * @return a problem detail with NOT_FOUND status and the exception message
   */
  @ExceptionHandler(EntityNotFoundException.class)
  public ProblemDetail handleEntityNotFoundException(EntityNotFoundException exception) {
    log.warn("Entity not found: {}", exception.getMessage());
    return ProblemDetailUtils.createProblemDetail(HttpStatus.NOT_FOUND, exception.getMessage());
  }

  /**
   * Handles exceptions thrown when no handler is found for a request.
   *
   * @param exception the no handler found exception
   * @return a problem detail with NOT_FOUND status and a descriptive message
   */
  @ExceptionHandler(NoHandlerFoundException.class)
  public ProblemDetail handleNoHandlerFoundException(NoHandlerFoundException exception) {
    log.warn("No handler found: {} {}", exception.getHttpMethod(), exception.getRequestURL());
    return ProblemDetailUtils.createProblemDetail(
        HttpStatus.NOT_FOUND,
        "No handler found for " + exception.getHttpMethod() + " " + exception.getRequestURL()
    );
  }

  /**
   * Handles exceptions thrown when a user attempts to access a resource they are not authorized
   * for.
   *
   * @param exception the unauthorized access exception
   * @return a problem detail with UNAUTHORIZED status and the exception message
   */
  @ExceptionHandler(UnauthorizedAccessException.class)
  public ProblemDetail handleUnauthorizedAccessException(UnauthorizedAccessException exception) {
    log.error("Unauthorized access: {}", exception.getMessage());
    return ProblemDetailUtils.createDomainProblemDetail(HttpStatus.UNAUTHORIZED,
        exception.getMessage(), "user");
  }

  // ===== General exceptions =====

  /**
   * Fallback handler for all unhandled exceptions.
   *
   * <p>This handler catches any exception not specifically handled by other handlers
   * and returns a generic error response to avoid exposing sensitive details.</p>
   *
   * @param exception the unhandled exception
   * @return a problem detail with INTERNAL_SERVER_ERROR status and a generic error message
   */
  @ExceptionHandler(Exception.class)
  public ProblemDetail handleException(Exception exception) {
    log.error("Unhandled exception occurred", exception);
    return ProblemDetailUtils.createProblemDetail(
        HttpStatus.INTERNAL_SERVER_ERROR,
        "An unexpected error occurred"
    );
  }
}