package stud.ntnu.krisefikser.email.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

/**
 * Mock implementation of the EmailService for testing and development purposes.
 * This service logs email sending attempts instead of actually sending emails,
 * making it suitable for testing environments where real email sending is not desired.
 *
 * @author Krisefikser Team
 */
@Service
public class MockEmailService extends EmailService {

    private static final Logger logger = LoggerFactory.getLogger(MockEmailService.class);

    /**
     * Constructs a new MockEmailService.
     * Initializes without a RestTemplate since no actual HTTP requests are made.
     */
    public MockEmailService() {
        super(null); // No RestTemplate needed for the mock
    }

    /**
     * Simulates sending an email by logging the email details.
     * This method overrides the parent class's implementation to provide mock functionality.
     *
     * @param to the recipient's email address
     * @param subject the subject of the email
     * @param body the content of the email
     * @return a ResponseEntity with a success message and OK status
     */
    @Override
    public ResponseEntity<String> sendEmail(String to, String subject, String body) {
        logger.info("Mock email sent to: {}\nSubject: {}\nBody: {}", to, subject, body);
        // Return a success response similar to what the real service would return
        return new ResponseEntity<>("Mock email sent successfully", HttpStatus.OK);
    }
}