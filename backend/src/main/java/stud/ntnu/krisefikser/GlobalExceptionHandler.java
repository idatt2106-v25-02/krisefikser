package stud.ntnu.krisefikser;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.persistence.EntityNotFoundException;
import java.net.URI;
import java.time.Instant;
import java.util.stream.Collectors;
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
import stud.ntnu.krisefikser.article.exception.ArticleNotFoundException;
import stud.ntnu.krisefikser.auth.exception.InvalidTokenException;
import stud.ntnu.krisefikser.auth.exception.RefreshTokenDoesNotExistException;
import stud.ntnu.krisefikser.household.exception.HouseholdNotFoundException;
import stud.ntnu.krisefikser.user.exception.EmailAlreadyExistsException;
import stud.ntnu.krisefikser.user.exception.UnauthorizedAccessException;
import stud.ntnu.krisefikser.user.exception.UserDoesNotExistException;

@RestControllerAdvice
public class GlobalExceptionHandler {

  // Custom exceptions
  @ExceptionHandler(HouseholdNotFoundException.class)
  public ProblemDetail handleHouseholdNotFoundException(HouseholdNotFoundException exception) {
    return createProblemDetail(HttpStatus.NOT_FOUND, exception.getMessage());
  }

  // Authentication-related exceptions
  @ExceptionHandler(InvalidTokenException.class)
  public ProblemDetail handleInvalidTokenException(InvalidTokenException exception) {
    return createProblemDetail(HttpStatus.UNAUTHORIZED, exception.getMessage());
  }

  @ExceptionHandler(RefreshTokenDoesNotExistException.class)
  public ProblemDetail handleRefreshTokenDoesNotExistException(
      RefreshTokenDoesNotExistException exception) {
    return createProblemDetail(HttpStatus.UNAUTHORIZED, exception.getMessage());
  }

  // User related exceptions
  @ExceptionHandler(EmailAlreadyExistsException.class)
  public ProblemDetail handleEmailAlreadyExistsException(EmailAlreadyExistsException exception) {
    return createProblemDetail(HttpStatus.CONFLICT, exception.getMessage());
  }

  @ExceptionHandler(UserDoesNotExistException.class)
  public ProblemDetail handleUserDoesNotExistException(UserDoesNotExistException exception) {
    return createProblemDetail(HttpStatus.NOT_FOUND, exception.getMessage());
  }

  // JWT specific exceptions
  @ExceptionHandler(ExpiredJwtException.class)
  public ProblemDetail handleExpiredJwtException(ExpiredJwtException exception) {
    return createProblemDetail(HttpStatus.UNAUTHORIZED, "JWT token has expired");
  }

  @ExceptionHandler(MalformedJwtException.class)
  public ProblemDetail handleMalformedJwtException(MalformedJwtException exception) {
    return createProblemDetail(HttpStatus.UNAUTHORIZED, "Invalid JWT token format");
  }

  @ExceptionHandler(SignatureException.class)
  public ProblemDetail handleSignatureException(SignatureException exception) {
    return createProblemDetail(HttpStatus.UNAUTHORIZED, "Invalid JWT signature");
  }

  @ExceptionHandler(UnsupportedJwtException.class)
  public ProblemDetail handleUnsupportedJwtException(UnsupportedJwtException exception) {
    return createProblemDetail(HttpStatus.UNAUTHORIZED, "Unsupported JWT token");
  }

  // Spring Security exceptions
  @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
  public ProblemDetail handleAccessDeniedException(
      org.springframework.security.access.AccessDeniedException exception) {
    return createProblemDetail(HttpStatus.FORBIDDEN, "Access denied");
  }

  @ExceptionHandler(BadCredentialsException.class)
  public ProblemDetail handleBadCredentialsException(BadCredentialsException exception) {
    return createProblemDetail(HttpStatus.UNAUTHORIZED, "Invalid credentials");
  }

  // Bean validation exceptions
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ProblemDetail handleValidationException(MethodArgumentNotValidException exception) {
    String errorMessage = exception.getBindingResult().getFieldErrors()
        .stream()
        .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
        .collect(Collectors.joining(", "));
    return createProblemDetail(HttpStatus.BAD_REQUEST, errorMessage);
  }

  // Request mapping exceptions
  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  public ProblemDetail handleMethodNotSupportedException(
      HttpRequestMethodNotSupportedException exception) {
    return createProblemDetail(
        HttpStatus.METHOD_NOT_ALLOWED,
        "Method " + exception.getMethod() + " is not supported for this request"
    );
  }

  @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
  public ProblemDetail handleMediaTypeNotSupportedException(
      HttpMediaTypeNotSupportedException exception) {
    return createProblemDetail(
        HttpStatus.UNSUPPORTED_MEDIA_TYPE,
        "Media type " + exception.getContentType() + " is not supported"
    );
  }

  @ExceptionHandler(MissingServletRequestParameterException.class)
  public ProblemDetail handleMissingParameterException(
      MissingServletRequestParameterException exception) {
    return createProblemDetail(
        HttpStatus.BAD_REQUEST,
        "Required parameter '" + exception.getParameterName() + "' is missing"
    );
  }

  // Resource not found exceptions
  @ExceptionHandler(EntityNotFoundException.class)
  public ProblemDetail handleEntityNotFoundException(EntityNotFoundException exception) {
    return createProblemDetail(HttpStatus.NOT_FOUND, exception.getMessage());
  }

  @ExceptionHandler(NoHandlerFoundException.class)
  public ProblemDetail handleNoHandlerFoundException(NoHandlerFoundException exception) {
    return createProblemDetail(
        HttpStatus.NOT_FOUND,
        "No handler found for " + exception.getHttpMethod() + " " + exception.getRequestURL()
    );
  }

  // TODO: move this into article domain
  @ExceptionHandler(ArticleNotFoundException.class)
  public ProblemDetail handleArticleNotFoundException(ArticleNotFoundException exception) {
    return createProblemDetail(HttpStatus.NOT_FOUND, exception.getMessage());
  }

  @ExceptionHandler(UnauthorizedAccessException.class)
  public ProblemDetail handleUnauthorizedAccessException(UnauthorizedAccessException exception) {
    return createProblemDetail(HttpStatus.UNAUTHORIZED, exception.getMessage());
  }

  // General exceptions
  @ExceptionHandler(Exception.class)
  public ProblemDetail handleException(Exception exception) {
    ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
        HttpStatus.INTERNAL_SERVER_ERROR,
        "An unexpected error occurred"
    );
    problemDetail.setProperty("timestamp", Instant.now());
    return problemDetail;
  }

  private ProblemDetail createProblemDetail(HttpStatus status, String message) {
    ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, message);
    problemDetail.setType(URI.create("https://krisefikser.ntnu.stud/errors/" + status.value()));
    problemDetail.setTitle(status.getReasonPhrase());
    problemDetail.setProperty("timestamp", Instant.now());
    return problemDetail;
  }
}