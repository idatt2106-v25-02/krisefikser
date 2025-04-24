package stud.ntnu.krisefikser;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;
import stud.ntnu.krisefikser.auth.exception.InvalidTokenException;
import stud.ntnu.krisefikser.auth.exception.RefreshTokenDoesNotExistException;
import stud.ntnu.krisefikser.user.exception.EmailAlreadyExistsException;
import stud.ntnu.krisefikser.user.exception.UserDoesNotExistException;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

  // Authentication related exceptions
  @ExceptionHandler(InvalidTokenException.class)
  public ResponseEntity<ErrorResponse> handleInvalidTokenException(InvalidTokenException exception) {
    return createErrorResponse(HttpStatus.UNAUTHORIZED, exception.getMessage());
  }

  @ExceptionHandler(RefreshTokenDoesNotExistException.class)
  public ResponseEntity<ErrorResponse> handleRefreshTokenDoesNotExistException(RefreshTokenDoesNotExistException exception) {
    return createErrorResponse(HttpStatus.UNAUTHORIZED, exception.getMessage());
  }

  // User related exceptions
  @ExceptionHandler(EmailAlreadyExistsException.class)
  public ResponseEntity<ErrorResponse> handleEmailAlreadyExistsException(EmailAlreadyExistsException exception) {
    return createErrorResponse(HttpStatus.CONFLICT, exception.getMessage());
  }

  @ExceptionHandler(UserDoesNotExistException.class)
  public ResponseEntity<ErrorResponse> handleUserDoesNotExistException(UserDoesNotExistException exception) {
    return createErrorResponse(HttpStatus.NOT_FOUND, exception.getMessage());
  }

  // JWT specific exceptions
  @ExceptionHandler(ExpiredJwtException.class)
  public ResponseEntity<ErrorResponse> handleExpiredJwtException(ExpiredJwtException exception) {
    return createErrorResponse(HttpStatus.UNAUTHORIZED, "JWT token has expired");
  }

  @ExceptionHandler(MalformedJwtException.class)
  public ResponseEntity<ErrorResponse> handleMalformedJwtException(MalformedJwtException exception) {
    return createErrorResponse(HttpStatus.UNAUTHORIZED, "Invalid JWT token format");
  }

  @ExceptionHandler(SignatureException.class)
  public ResponseEntity<ErrorResponse> handleSignatureException(SignatureException exception) {
    return createErrorResponse(HttpStatus.UNAUTHORIZED, "Invalid JWT signature");
  }

  @ExceptionHandler(UnsupportedJwtException.class)
  public ResponseEntity<ErrorResponse> handleUnsupportedJwtException(UnsupportedJwtException exception) {
    return createErrorResponse(HttpStatus.UNAUTHORIZED, "Unsupported JWT token");
  }

  // Spring Security exceptions
  @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
  public ResponseEntity<ErrorResponse> handleAccessDeniedException(org.springframework.security.access.AccessDeniedException exception) {
    return createErrorResponse(HttpStatus.FORBIDDEN, "Access denied");
  }

  @ExceptionHandler(BadCredentialsException.class)
  public ResponseEntity<ErrorResponse> handleBadCredentialsException(BadCredentialsException exception) {
    return createErrorResponse(HttpStatus.UNAUTHORIZED, "Invalid credentials");
  }

  // Bean validation exceptions
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException exception) {
    String errorMessage = exception.getBindingResult().getFieldErrors()
        .stream()
        .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
        .collect(Collectors.joining(", "));
    return createErrorResponse(HttpStatus.BAD_REQUEST, errorMessage);
  }

  // Request mapping exceptions
  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  public ResponseEntity<ErrorResponse> handleMethodNotSupportedException(HttpRequestMethodNotSupportedException exception) {
    return createErrorResponse(
        HttpStatus.METHOD_NOT_ALLOWED,
        "Method " + exception.getMethod() + " is not supported for this request"
    );
  }

  @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
  public ResponseEntity<ErrorResponse> handleMediaTypeNotSupportedException(HttpMediaTypeNotSupportedException exception) {
    return createErrorResponse(
        HttpStatus.UNSUPPORTED_MEDIA_TYPE,
        "Media type " + exception.getContentType() + " is not supported"
    );
  }

  @ExceptionHandler(MissingServletRequestParameterException.class)
  public ResponseEntity<ErrorResponse> handleMissingParameterException(MissingServletRequestParameterException exception) {
    return createErrorResponse(
        HttpStatus.BAD_REQUEST,
        "Required parameter '" + exception.getParameterName() + "' is missing"
    );
  }

  // Resource not found exceptions
  @ExceptionHandler(EntityNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleEntityNotFoundException(EntityNotFoundException exception) {
    return createErrorResponse(HttpStatus.NOT_FOUND, exception.getMessage());
  }

  @ExceptionHandler(NoHandlerFoundException.class)
  public ResponseEntity<ErrorResponse> handleNoHandlerFoundException(NoHandlerFoundException exception) {
    return createErrorResponse(
        HttpStatus.NOT_FOUND,
        "No handler found for " + exception.getHttpMethod() + " " + exception.getRequestURL()
    );
  }

  // General exceptions
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleException(Exception exception) {
    ErrorResponse errorResponse = new ErrorResponse(
        500,
        "An unexpected error occurred",
        System.currentTimeMillis()
    );

    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
  }

  private ResponseEntity<ErrorResponse> createErrorResponse(HttpStatus status, String message) {
    ErrorResponse errorResponse = new ErrorResponse(
        status.value(), message, System.currentTimeMillis()
    );
    return ResponseEntity.status(status).body(errorResponse);
  }
}