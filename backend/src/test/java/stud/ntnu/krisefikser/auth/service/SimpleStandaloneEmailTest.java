package stud.ntnu.krisefikser.auth.service;

import io.github.cdimascio.dotenv.Dotenv;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User.UserBuilder;
import org.springframework.security.core.userdetails.UserDetails;
import stud.ntnu.krisefikser.user.entity.User;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Properties;

/**
 * Standalone email test that can be run directly without Spring context
 * or any Maven plugin dependencies.
 */
public class SimpleStandaloneEmailTest {

    private final String targetEmail = "janjaboy14@gmail.com";
    private final Dotenv dotenv;

    public SimpleStandaloneEmailTest() {
        // Finn path til backend-mappen
        Path currentPath = Paths.get("").toAbsolutePath();
        String backendPath = currentPath.toString();

        // Hvis vi er i prosjektets rotmappe, legg til "backend"
        if (backendPath.endsWith("Smidig oppgave")) {
            backendPath = Paths.get(backendPath, "backend").toString();
        }

        // Konfigurer dotenv til å lete i backend-mappen
        dotenv = Dotenv.configure()
            .directory(backendPath)
            .load();
    }

    /**
     * This test can be run directly to send a simple email test
     */
    public static void main(String[] args) {
        new SimpleStandaloneEmailTest().sendSimpleEmailDirectly();
    }

    public void sendSimpleEmailDirectly() {
        try {
            // Last inn properties fra application-test.properties
            Properties properties = PropertiesLoaderUtils.loadProperties(
                new ClassPathResource("application-test.properties")
            );

            // Opprett mail sender med konfigurasjon fra properties
            JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
            mailSender.setHost(properties.getProperty("spring.mail.host"));
            mailSender.setPort(Integer.parseInt(properties.getProperty("spring.mail.port")));
            mailSender.setUsername(properties.getProperty("spring.mail.username"));

            // Hent API token fra .env fil, deretter miljøvariabel, til slutt properties
            String mailtrapToken = dotenv.get("MAILTRAP_API_TOKEN");
            if (mailtrapToken == null || mailtrapToken.isEmpty()) {
                mailtrapToken = System.getenv("MAILTRAP_API_TOKEN");
                if (mailtrapToken == null || mailtrapToken.isEmpty()) {
                    mailtrapToken = properties.getProperty("spring.mail.password")
                        .replace("${MAILTRAP_API_TOKEN:", "")
                        .replace("}", "");
                    System.out.println("Bruker API token fra application-test.properties");
                } else {
                    System.out.println("Bruker API token fra miljøvariabel");
                }
            } else {
                System.out.println("Bruker API token fra .env fil");
            }
            mailSender.setPassword(mailtrapToken);

            // Konfigurer mail properties
            Properties mailProperties = new Properties();
            mailProperties.put("mail.transport.protocol", "smtp");
            mailProperties.put("mail.smtp.auth", properties.getProperty("spring.mail.properties.mail.smtp.auth"));
            mailProperties.put("mail.smtp.starttls.enable", properties.getProperty("spring.mail.properties.mail.smtp.starttls.enable"));
            mailProperties.put("mail.smtp.connectiontimeout", properties.getProperty("spring.mail.properties.mail.smtp.connectiontimeout"));
            mailProperties.put("mail.smtp.timeout", properties.getProperty("spring.mail.properties.mail.smtp.timeout"));
            mailProperties.put("mail.smtp.writetimeout", properties.getProperty("spring.mail.properties.mail.smtp.writetimeout"));
            mailProperties.put("mail.debug", "true");
            mailSender.setJavaMailProperties(mailProperties);

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

            // Send the email
            mailSender.send(message);
            System.out.println("Test email sent successfully to: " + targetEmail);

        } catch (IOException e) {
            System.err.println("Error loading properties: " + e.getMessage());
            e.printStackTrace();
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