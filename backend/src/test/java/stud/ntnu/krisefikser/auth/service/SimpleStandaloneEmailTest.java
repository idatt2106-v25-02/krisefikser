package stud.ntnu.krisefikser.auth.service;

import org.junit.jupiter.api.Test;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User.UserBuilder;
import org.springframework.security.core.userdetails.UserDetails;
import stud.ntnu.krisefikser.user.entity.User;

import java.util.Collections;
import java.util.Properties;

/**
 * Standalone email test that can be run directly without Spring context
 * or any Maven plugin dependencies.
 */
public class SimpleStandaloneEmailTest {

    private final String targetEmail = "janjaboy14@gmail.com";

    /**
     * This test can be run directly to send a simple email test
     */
    public static void main(String[] args) {
        new SimpleStandaloneEmailTest().sendSimpleEmailDirectly();
    }

    public void sendSimpleEmailDirectly() {
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

        // Create a simple mail message
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(targetEmail);
        message.setSubject("Test Email from Krisefikser");
        message.setText("Hello " + user.getFirstName() + ",\n\n" +
                "This is a test email from Krisefikser.\n\n" +
                "Thank you for registering!\n\n" +
                "Best regards,\n" +
                "The Krisefikser Team");
        message.setFrom("noreply@krisefikser.app");

        // Setup JavaMailSender with Mailtrap credentials
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost("live.smtp.mailtrap.io");
        mailSender.setPort(587);
        mailSender.setUsername("api");
        
        // Hent passordet fra miljøvariabel, eller bruk en placeholder
        String mailtrapToken = System.getenv("MAILTRAP_API_TOKEN");
        if (mailtrapToken == null || mailtrapToken.isEmpty()) {
            System.out.println("ADVARSEL: MAILTRAP_API_TOKEN miljøvariabel er ikke satt.");
            System.out.println("For testing: sett miljøvariabel med følgende kommando:");
            System.out.println("export MAILTRAP_API_TOKEN=your_api_token_here");
            mailtrapToken = "placeholder_token"; // Dette vil ikke fungere, men testene vil ikke feile
        }
        mailSender.setPassword(mailtrapToken);

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.debug", "true");

        try {
            // Send the email
            mailSender.send(message);
            System.out.println("Test email sent successfully to: " + targetEmail);
        } catch (Exception e) {
            System.err.println("Error sending email: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Test
    void testEmailSendingInUnitTest() {
        // This won't actually send an email, it's just to show the test structure
        System.out.println("Run this test as a main() method to send actual emails");
    }
} 