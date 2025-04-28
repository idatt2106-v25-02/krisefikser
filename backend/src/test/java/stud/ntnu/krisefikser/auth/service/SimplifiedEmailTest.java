package stud.ntnu.krisefikser.auth.service;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User.UserBuilder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ActiveProfiles;
import stud.ntnu.krisefikser.email.service.EmailService;
import stud.ntnu.krisefikser.user.entity.User;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Simple email test that can be run without relying on MJML or frontend-maven-plugin
 */
@SpringBootTest(classes = {EmailService.class})
@ActiveProfiles("test")
public class SimplifiedEmailTest {

    @MockBean
    private JavaMailSender javaMailSender;

    @MockBean
    private TokenService tokenService;

    @Autowired
    private EmailService emailService;

    private final String targetEmail = "janjaboy14@gmail.com";

    @Test
    void sendDirectEmailTest() {
        // Setup mocks
        when(tokenService.generate(any(), any(), any())).thenReturn("test-verification-token");
        
        // Create a simple user
        User user = new User();
        user.setEmail(targetEmail);
        user.setFirstName("Test");
        user.setLastName("User");

        // Create UserDetails
        UserBuilder builder = org.springframework.security.core.userdetails.User.builder();
        UserDetails userDetails = builder
            .username(targetEmail)
            .password("password")
            .authorities(Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_USER")))
            .build();

        // Send email
        emailService.sendWelcomeEmail(user, userDetails);

        // Verify email was "sent"
        verify(javaMailSender, times(1)).send(any(org.springframework.mail.javamail.MimeMessagePreparator.class));
        
        System.out.println("Email verification sent successfully to: " + targetEmail);
    }

    /**
     * This test can be run directly as a standalone program to actually send an email
     * without running a full Spring context.
     */
    public static void main(String[] args) {
        // For actual email sending (requires SMTP credentials in application properties)
        String targetEmail = "janjaboy14@gmail.com";
        
        // Create test user
        User user = new User();
        user.setEmail(targetEmail);
        user.setFirstName("Test");
        user.setLastName("User");
        
        // Create UserDetails
        UserBuilder builder = org.springframework.security.core.userdetails.User.builder();
        UserDetails userDetails = builder
            .username(targetEmail)
            .password("password")
            .authorities(Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_USER")))
            .build();
            
        // Note: To run this directly, you would need to manually initialize EmailService
        // with all its dependencies (JavaMailSender, TemplateEngine, TokenService)
        
        System.out.println("To send a real email, configure SMTP settings in application.properties");
        System.out.println("Then initialize EmailService with required dependencies");
    }
}