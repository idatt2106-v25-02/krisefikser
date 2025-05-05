package stud.ntnu.krisefikser.email.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

public class MockEmailService extends EmailService {

    private static final Logger logger = LoggerFactory.getLogger(MockEmailService.class);

    public MockEmailService() {
        super(null); // No RestTemplate needed for the mock
    }

    @Override
    public ResponseEntity<String> sendEmail(String to, String subject, String body) {
        logger.info("Mock email sent to: {}\nSubject: {}\nBody: {}", to, subject, body);
        // Return a success response similar to what the real service would return
        return new ResponseEntity<>("Mock email sent successfully", HttpStatus.OK);
    }
}