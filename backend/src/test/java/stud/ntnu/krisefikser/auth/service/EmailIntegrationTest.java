package stud.ntnu.krisefikser.auth.service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import stud.ntnu.krisefikser.auth.dto.RegisterRequest;
import stud.ntnu.krisefikser.auth.dto.RegisterResponse;

@SpringBootTest
@ActiveProfiles("test") // Use test profile if you have one
public class EmailIntegrationTest {

    @Autowired
    private AuthService authService;

    @Test
    void testSendRealEmail() {
        // Create a unique email to avoid duplicate registration errors
        String uniqueEmail = "janjaboy14+" + System.currentTimeMillis() + "@gmail.com";

        RegisterRequest request = new RegisterRequest(
            uniqueEmail,
            "securePassword123",
            "Test",
            "User"
        );

        // This will actually trigger the email sending process
        RegisterResponse response = authService.register(request);

        // If no exception is thrown, the email was sent successfully
        System.out.println("Email sent successfully! Check " + request.getEmail());
        System.out.println("Access token: " + response.getAccessToken());
    }
}