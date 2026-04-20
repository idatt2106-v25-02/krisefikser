package stud.ntnu.krisefikser.e2e;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * HTTP hook for Cypress to read captured emails. Only registered when profile {@code e2e} is
 * active. Requires {@code X-E2E-Mail-Hook} matching {@code e2e.mail.hook-secret}.
 */
@RestController
@RequestMapping("/api/e2e/mail")
@Profile("e2e")
@RequiredArgsConstructor
public class E2eMailController {

  static final String HOOK_HEADER = "X-E2E-Mail-Hook";

  private final E2eMailOutbox outbox;

  @Value("${e2e.mail.hook-secret:}")
  private String hookSecret;

  @GetMapping("/latest")
  public ResponseEntity<E2eMailResponse> latest(
      @RequestParam String email,
      @RequestHeader(value = HOOK_HEADER, required = false) String providedSecret
  ) {
    if (!StringUtils.hasText(hookSecret)) {
      return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
    }
    if (!StringUtils.hasText(providedSecret) || !constantTimeEquals(hookSecret, providedSecret)) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
    E2eMailOutbox.CapturedMail mail = outbox.getLatest(email);
    if (mail == null) {
      return ResponseEntity.notFound().build();
    }
    return ResponseEntity.ok(
        new E2eMailResponse(mail.to(), mail.subject(), mail.html(), mail.capturedAtMillis()));
  }

  private static boolean constantTimeEquals(String expected, String actual) {
    if (expected == null || actual == null || expected.length() != actual.length()) {
      return false;
    }
    int result = 0;
    for (int i = 0; i < expected.length(); i++) {
      result |= expected.charAt(i) ^ actual.charAt(i);
    }
    return result == 0;
  }

  public record E2eMailResponse(String to, String subject, String html, long capturedAtMillis) {
  }
}
