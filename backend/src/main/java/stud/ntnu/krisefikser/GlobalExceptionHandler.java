package stud.ntnu.krisefikser;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.persistence.EntityNotFoundException;
import java.net.URI;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import stud.ntnu.krisefikser.article.exception.ArticleNotFoundException;
import stud.ntnu.krisefikser.auth.exception.InvalidTokenException;
import stud.ntnu.krisefikser.auth.exception.RefreshTokenDoesNotExistException;
import stud.ntnu.krisefikser.auth.exception.TurnstileVerificationException;
import stud.ntnu.krisefikser.user.exception.EmailAlreadyExistsException;
import stud.ntnu.krisefikser.user.exception.UnauthorizedAccessException;
import stud.ntnu.krisefikser.user.exception.UserDoesNotExistException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

  // Authentication-related exceptions
  @ExceptionHandler(InvalidTokenException.class)
  public ProblemDetail handleInvalidTokenException(InvalidTokenException exception) {
    log.warn("Invalid token", exception);
    return createProblemDetail(HttpStatus.UNAUTHORIZED, exception.getMessage());
  }

  @ExceptionHandler(RefreshTokenDoesNotExistException.class)
  public ProblemDetail handleRefreshTokenDoesNotExistException(
      RefreshTokenDoesNotExistException exception) {
    log.warn("Refresh token does not exist", exception);
    return createProblemDetail(HttpStatus.UNAUTHORIZED, exception.getMessage());
  }

  @ExceptionHandler(TurnstileVerificationException.class)
  public ProblemDetail handleTurnstileVerificationException(TurnstileVerificationException exception) {
    return createProblemDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
  }

  // User related exceptions
  @ExceptionHandler(EmailAlreadyExistsException.class)
  public ProblemDetail handleEmailAlreadyExistsException(EmailAlreadyExistsException exception) {
    log.warn("Email already exists", exception);
    return createProblemDetail(HttpStatus.CONFLICT, exception.getMessage());
  }

  @ExceptionHandler(UserDoesNotExistException.class)
  public ProblemDetail handleUserDoesNotExistException(UserDoesNotExistException exception) {
    log.warn("User does not exist", exception);
    return createProblemDetail(HttpStatus.NOT_FOUND, exception.getMessage());
  }

  // JWT specific exceptions
  @ExceptionHandler(ExpiredJwtException.class)
  public ProblemDetail handleExpiredJwtException(ExpiredJwtException exception) {
    log.warn("JWT token has expired", exception);
    return createProblemDetail(HttpStatus.UNAUTHORIZED, "JWT token has expired");
  }

  @ExceptionHandler(MalformedJwtException.class)
  public ProblemDetail handleMalformedJwtException(MalformedJwtException exception) {
    log.warn("Invalid JWT token", exception);
    return createProblemDetail(HttpStatus.UNAUTHORIZED, "Invalid JWT token format");
  }

  @ExceptionHandler(SignatureException.class)
  public ProblemDetail handleSignatureException(SignatureException exception) {
    log.warn("Invalid JWT signature", exception);
    return createProblemDetail(HttpStatus.UNAUTHORIZED, "Invalid JWT signature");
  }

  @ExceptionHandler(UnsupportedJwtException.class)
  public ProblemDetail handleUnsupportedJwtException(UnsupportedJwtException exception) {
    log.warn("Unsupported JWT token", exception);
    return createProblemDetail(HttpStatus.UNAUTHORIZED, "Unsupported JWT token");
  }

  // Spring Security exceptions
  @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
  public ProblemDetail handleAccessDeniedException(
      org.springframework.security.access.AccessDeniedException exception) {
    log.warn("Access denied", exception);
    return createProblemDetail(HttpStatus.FORBIDDEN, "Access denied");
  }

  @ExceptionHandler(BadCredentialsException.class)
  public ProblemDetail handleBadCredentialsException(BadCredentialsException exception) {
    log.warn("Bad credentials", exception);
    return createProblemDetail(HttpStatus.UNAUTHORIZED, "Invalid credentials");
  }

  // Bean validation exceptions
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ProblemDetail handleValidationException(MethodArgumentNotValidException exception) {
    String errorMessage = exception.getBindingResult().getFieldErrors().stream()
        .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
        .collect(Collectors.joining(", "));
    log.warn("Validation error: {}", errorMessage);
    return createProblemDetail(HttpStatus.BAD_REQUEST, errorMessage);
  }

  // Request mapping exceptions
  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  public ProblemDetail handleMethodNotSupportedException(
      HttpRequestMethodNotSupportedException exception) {
    log.warn("Unsupported HTTP method: {}", exception.getMethod());
    return createProblemDetail(HttpStatus.METHOD_NOT_ALLOWED,
        "Method " + exception.getMethod() + " is not supported for this request");
  }

  @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
  public ProblemDetail handleMediaTypeNotSupportedException(
      HttpMediaTypeNotSupportedException exception) {
    log.warn("Unsupported media type: {}", exception.getContentType());
    return createProblemDetail(HttpStatus.UNSUPPORTED_MEDIA_TYPE,
        "Media type " + exception.getContentType() + " is not supported");
  }

  @ExceptionHandler(MissingServletRequestParameterException.class)
  public ProblemDetail handleMissingParameterException(
      MissingServletRequestParameterException exception) {
    log.warn("Missing parameter: {}", exception.getParameterName());
    return createProblemDetail(HttpStatus.BAD_REQUEST,
        "Required parameter '" + exception.getParameterName() + "' is missing");
  }

  // Resource not found exceptions
  @ExceptionHandler(EntityNotFoundException.class)
  public ProblemDetail handleEntityNotFoundException(EntityNotFoundException exception) {
    log.warn("Entity not found", exception);
    return createProblemDetail(HttpStatus.NOT_FOUND, exception.getMessage());
  }

  @ExceptionHandler(NoHandlerFoundException.class)
  public ProblemDetail handleNoHandlerFoundException(NoHandlerFoundException exception) {
    log.warn("No handler found for {} {}", exception.getHttpMethod(), exception.getRequestURL());
    return createProblemDetail(HttpStatus.NOT_FOUND,
        "No handler found for " + exception.getHttpMethod() + " " + exception.getRequestURL());
  }

  // TODO: move this into article domain
  @ExceptionHandler(ArticleNotFoundException.class)
  public ProblemDetail handleArticleNotFoundException(ArticleNotFoundException exception) {
    log.warn("Article not found", exception);
    return createProblemDetail(HttpStatus.NOT_FOUND, exception.getMessage());
  }

  @ExceptionHandler(UnauthorizedAccessException.class)
  public ProblemDetail handleUnauthorizedAccessException(UnauthorizedAccessException exception) {
    log.warn("Unauthorized access attempt", exception);
    return createProblemDetail(HttpStatus.UNAUTHORIZED, exception.getMessage());
  }

  @ExceptionHandler(NoResourceFoundException.class)
  public ProblemDetail handleNoResourceFoundException(NoResourceFoundException exception) {
    log.warn("No resource found", exception);
    return createProblemDetail(HttpStatus.NOT_FOUND, exception.getMessage());
  }

  // General exceptions
  @ExceptionHandler(Exception.class)
  public ProblemDetail handleException(Exception exception) {
    log.error("An unexpected error occurred", exception);
    return createProblemDetail(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred.");
  }

  private ProblemDetail createProblemDetail(HttpStatus status, String message) {
    ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, message);
    problemDetail.setType(URI.create("https://krisefikser.ntnu.stud/errors/" + status.value()));
    problemDetail.setTitle(status.getReasonPhrase());
    return problemDetail;
  }
}