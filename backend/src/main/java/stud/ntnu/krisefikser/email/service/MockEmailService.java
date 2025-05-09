package stud.ntnu.krisefikser.email.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Mock implementation of the EmailService for testing purposes.
 *
 * <p>This class simulates sending emails by logging the email details instead of actually sending
 * them.
 */
public class MockEmailService extends EmailService {

  private static final Logger logger = LoggerFactory.getLogger(MockEmailService.class);

  /**
   * Constructs a new MockEmailService.
   *
   * <p>This constructor does not require a RestTemplate as it does not perform actual HTTP
   * requests.
   */
  public MockEmailService() {
    super(null, null);
  }

  @Override
  public ResponseEntity<String> sendEmail(String to, String subject, String body) {
    logger.info("Mock email sent to: {}\nSubject: {}\nBody: {}", to, subject, body);
    return new ResponseEntity<>("Mock email sent successfully", HttpStatus.OK);
  }
}