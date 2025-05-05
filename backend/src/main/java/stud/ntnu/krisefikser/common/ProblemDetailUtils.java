package stud.ntnu.krisefikser.common;

import java.net.URI;
import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

/**
 * Utility class for creating standardized problem details.
 */
public class ProblemDetailUtils {

  private static final String BASE_ERROR_URI = "https://krisefikser.app/errors";

  /**
   * Creates a standardized problem detail response with consistent structure.
   *
   * @param status  the HTTP status to use for the response
   * @param message the detailed error message
   * @return a fully configured problem detail
   */
  public static ProblemDetail createProblemDetail(HttpStatus status, String message) {
    ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, message);
    problemDetail.setType(URI.create(BASE_ERROR_URI + "/" + status.value()));
    problemDetail.setTitle(status.getReasonPhrase());
    problemDetail.setProperty("timestamp", Instant.now());
    return problemDetail;
  }

  /**
   * Creates a domain-specific problem detail response.
   *
   * @param status  the HTTP status to use for the response
   * @param message the detailed error message
   * @param domain  the domain name to include in the error URI
   * @return a fully configured problem detail with domain-specific URI
   */
  public static ProblemDetail createDomainProblemDetail(HttpStatus status, String message,
      String domain) {
    ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, message);
    problemDetail.setType(URI.create(BASE_ERROR_URI + "/" + domain + "/" + status.value()));
    problemDetail.setTitle(status.getReasonPhrase());
    problemDetail.setProperty("timestamp", Instant.now());
    return problemDetail;
  }
} 