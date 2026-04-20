package stud.ntnu.krisefikser.e2e;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * In-memory store of the last outbound email per recipient, used only when profile {@code e2e} is
 * active so Cypress can assert verification links without Mailtrap.
 */
@Component
@Profile("e2e")
public class E2eMailOutbox {

  private final Map<String, CapturedMail> latestByRecipient = new ConcurrentHashMap<>();

  /**
   * Records (overwrites) the latest message for the given recipient.
   */
  public void record(String toEmail, String subject, String htmlContent) {
    if (toEmail == null) {
      return;
    }
    String key = toEmail.trim().toLowerCase(Locale.ROOT);
    latestByRecipient.put(key, new CapturedMail(toEmail.trim(), subject, htmlContent, System.currentTimeMillis()));
  }

  /**
   * Returns the latest captured mail for the recipient, or empty if none.
   */
  public CapturedMail getLatest(String email) {
    if (email == null) {
      return null;
    }
    return latestByRecipient.get(email.trim().toLowerCase(Locale.ROOT));
  }

  public record CapturedMail(String to, String subject, String html, long capturedAtMillis) {
  }
}
