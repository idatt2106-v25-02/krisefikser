package stud.ntnu.krisefikser;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import stud.ntnu.krisefikser.article.exception.ArticleNotFoundException;
import stud.ntnu.krisefikser.auth.exception.InvalidCredentialsException;
import stud.ntnu.krisefikser.auth.exception.InvalidTokenException;
import stud.ntnu.krisefikser.auth.exception.RefreshTokenDoesNotExistException;
import stud.ntnu.krisefikser.auth.exception.TwoFactorAuthRequiredException;
import stud.ntnu.krisefikser.email.exception.EmailSendingException;
import stud.ntnu.krisefikser.email.exception.EmailTemplateException;
import stud.ntnu.krisefikser.household.exception.HouseholdNotFoundException;
import stud.ntnu.krisefikser.user.exception.UnauthorizedAccessException;
import stud.ntnu.krisefikser.user.exception.UserNotFoundException;

class GlobalExceptionHandlerTest {

  private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

  @Test
  void handleInvalidTokenException_shouldReturnUnauthorizedProblem() {
    var problem = handler.handleInvalidTokenException(new InvalidTokenException("bad token"));

    assertThat(problem.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
    assertThat(problem.getDetail()).isEqualTo("bad token");
  }

  @Test
  void handleUserDoesNotExistException_shouldReturnNotFoundProblem() {
    var problem = handler.handleUserDoesNotExistException(new UserNotFoundException(UUID.randomUUID()));

    assertThat(problem.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
    assertThat(problem.getDetail()).contains("does not exist");
  }

  @Test
  void handleTwoFactorAuthRequiredException_shouldReturnPreconditionRequired() {
    var problem = handler.handleTwoFactorAuthRequiredException(
        new TwoFactorAuthRequiredException("2FA required"));

    assertThat(problem.getStatus()).isEqualTo(HttpStatus.PRECONDITION_REQUIRED.value());
    assertThat(problem.getDetail()).isEqualTo("2FA required");
  }

  @Test
  void handleBadCredentialsException_shouldReturnUnauthorizedProblem() {
    var problem = handler.handleBadCredentialsException(new BadCredentialsException("invalid"));

    assertThat(problem.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
    assertThat(problem.getDetail()).isEqualTo("Invalid credentials");
  }

  @Test
  void handleAuthenticationException_shouldReturnUnauthorizedProblem() {
    AuthenticationException exception = new BadCredentialsException("auth required");

    var problem = handler.handleAuthenticationException(exception);

    assertThat(problem.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
    assertThat(problem.getDetail()).isEqualTo("Authentication required");
  }

  @Test
  void handleMethodNotSupportedException_shouldReturnMethodNotAllowed() {
    var exception = new HttpRequestMethodNotSupportedException("PATCH");

    var problem = handler.handleMethodNotSupportedException(exception);

    assertThat(problem.getStatus()).isEqualTo(HttpStatus.METHOD_NOT_ALLOWED.value());
    assertThat(problem.getDetail()).contains("Method PATCH is not supported");
  }

  @Test
  void handleMediaTypeNotSupportedException_shouldReturnUnsupportedMediaType() {
    var exception = new HttpMediaTypeNotSupportedException(MediaType.TEXT_PLAIN_VALUE);

    var problem = handler.handleMediaTypeNotSupportedException(exception);

    assertThat(problem.getStatus()).isEqualTo(HttpStatus.UNSUPPORTED_MEDIA_TYPE.value());
    assertThat(problem.getDetail()).contains("not supported");
  }

  @Test
  void handleMissingParameterException_shouldReturnBadRequest() {
    var exception = new MissingServletRequestParameterException("page", "Integer");

    var problem = handler.handleMissingParameterException(exception);

    assertThat(problem.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    assertThat(problem.getDetail()).contains("Required parameter 'page' is missing");
  }

  @Test
  void handleEmailSendingException_shouldHideInternalMessage() {
    var problem = handler.handleEmailSendingException(new EmailSendingException("smtp timeout"));

    assertThat(problem.getStatus()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE.value());
    assertThat(problem.getDetail()).isEqualTo("Failed to send email");
  }

  @Test
  void handleEmailTemplateException_shouldHideInternalMessage() {
    var problem = handler.handleEmailTemplateException(new EmailTemplateException("bad template"));

    assertThat(problem.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
    assertThat(problem.getDetail()).isEqualTo("Failed to process email template");
  }

  @Test
  void handleHouseholdNotFoundException_shouldReturnNotFoundProblem() {
    var problem = handler.handleHouseholdNotFoundException(new HouseholdNotFoundException());

    assertThat(problem.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
    assertThat(problem.getDetail()).isEqualTo("Household not found");
  }

  @Test
  void handleArticleNotFoundException_shouldReturnNotFoundProblem() {
    var problem = handler.handleArticleNotFoundException(new ArticleNotFoundException("No article"));

    assertThat(problem.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
    assertThat(problem.getDetail()).isEqualTo("No article");
  }

  @Test
  void handleInvalidCredentialsException_shouldReturnUnauthorizedProblem() {
    var problem = handler.handleInvalidCredentialsException(new InvalidCredentialsException());

    assertThat(problem.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
    assertThat(problem.getDetail()).isEqualTo("Invalid email or password");
  }

  @Test
  void handleRefreshTokenDoesNotExistException_shouldReturnUnauthorizedProblem() {
    var problem = handler.handleRefreshTokenDoesNotExistException(
        new RefreshTokenDoesNotExistException());

    assertThat(problem.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
    assertThat(problem.getDetail()).isEqualTo("Refresh token does not exist");
  }

  @Test
  void handleUnauthorizedAccessException_shouldReturnUnauthorizedProblem() {
    var problem = handler.handleUnauthorizedAccessException(
        new UnauthorizedAccessException("no access"));

    assertThat(problem.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
    assertThat(problem.getDetail()).isEqualTo("no access");
  }

  @Test
  void handleException_shouldReturnGenericInternalServerError() {
    var problem = handler.handleException(new RuntimeException("hidden"));

    assertThat(problem.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
    assertThat(problem.getDetail()).isEqualTo("An unexpected error occurred");
  }
}
