package stud.ntnu.krisefikser.auth.service;

import org.junit.jupiter.api.Test;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User.UserBuilder;
import org.springframework.security.core.userdetails.UserDetails;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.templateresolver.StringTemplateResolver;
import stud.ntnu.krisefikser.user.entity.User;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.Year;
import java.util.Collections;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * E-post test som bruker MJML for formattering
 */
public class MjmlEmailTest {

    private final String targetEmail = "janjaboy14@gmail.com";
    
    /**
     * Denne testen kan kjøres direkte for å sende en e-post med MJML-formattering
     */
    public static void main(String[] args) {
        new MjmlEmailTest().sendMjmlFormattedEmail();
    }
    
    public void sendMjmlFormattedEmail() {
        try {
            // Opprett bruker
            User user = createTestUser();
            
            // Opprett UserDetails
            UserDetails userDetails = createUserDetails(user);
            
            // Kompiler MJML til HTML
            String mjmlContent = getMjmlTemplate();
            String htmlContent = compileToHtml(mjmlContent, user);
            
            // Send e-post med HTML-innhold
            sendEmail(user.getEmail(), "Velkommen til Krisefikser!", htmlContent);
            
            System.out.println("E-post med MJML-formattering sendt til: " + targetEmail);
        } catch (Exception e) {
            System.err.println("Feil ved sending av e-post: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private User createTestUser() {
        User user = new User();
        user.setEmail(targetEmail);
        user.setFirstName("Test");
        user.setLastName("User");
        return user;
    }
    
    private UserDetails createUserDetails(User user) {
        UserBuilder builder = org.springframework.security.core.userdetails.User.builder();
        return builder
            .username(user.getEmail())
            .password("password")
            .authorities(Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_USER")))
            .build();
    }
    
    private String getMjmlTemplate() throws IOException {
        // Her bruker vi en enkel MJML-mal for testing
        return "<mjml>\n" +
               "  <mj-body>\n" +
               "    <mj-section>\n" +
               "      <mj-column>\n" +
               "        <mj-image width=\"100px\" src=\"https://krisefikser.no/logo.png\"></mj-image>\n" +
               "        <mj-divider border-color=\"#F45E43\"></mj-divider>\n" +
               "        <mj-text font-size=\"20px\" color=\"#F45E43\" font-family=\"helvetica\">Velkommen til Krisefikser!</mj-text>\n" +
               "        <mj-text font-size=\"16px\">Hei ${name},</mj-text>\n" +
               "        <mj-text font-size=\"16px\">Takk for at du registrerte deg hos oss. Klikk på knappen nedenfor for å verifisere e-postadressen din.</mj-text>\n" +
               "        <mj-button background-color=\"#F45E43\" href=\"${verificationLink}\">Verifiser min e-post</mj-button>\n" +
               "        <mj-text font-size=\"14px\">Denne lenken utløper om ${expiryHours} timer.</mj-text>\n" +
               "        <mj-divider border-color=\"#F45E43\"></mj-divider>\n" +
               "        <mj-text font-size=\"12px\" align=\"center\">© ${currentYear} Krisefikser. Alle rettigheter reservert.</mj-text>\n" +
               "      </mj-column>\n" +
               "    </mj-section>\n" +
               "  </mj-body>\n" +
               "</mjml>";
    }
    
    private String compileToHtml(String mjmlContent, User user) throws IOException {
        // I en faktisk implementasjon ville du brukt MJML Java API eller NPM for å kompilere
        // For enkelhets skyld erstatter vi bare variabler manuelt
        String html = mjmlContent
            .replace("${name}", user.getFirstName())
            .replace("${verificationLink}", "https://krisefikser.no/verify?token=test-token")
            .replace("${expiryHours}", "24")
            .replace("${currentYear}", String.valueOf(Year.now().getValue()));
            
        // Dette er en forenklet HTML-output siden vi ikke har tilgang til MJML compiler i denne konteksten
        // I en faktisk implementasjon ville denne HTML-en vært generert av MJML compiler
        return convertSimpleMjmlToHtml(user);
    }
    
    private String convertSimpleMjmlToHtml(User user) {
        // Istedenfor å forsøke å parse MJML, genererer vi HTML direkte
        return "<!DOCTYPE html>\n" +
               "<html>\n" +
               "<head>\n" +
               "  <title>Velkommen til Krisefikser</title>\n" +
               "  <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">\n" +
               "  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
               "  <style>\n" +
               "    body { font-family: Helvetica, Arial, sans-serif; margin: 0; padding: 0; }\n" +
               "    .container { max-width: 600px; margin: 0 auto; padding: 20px; }\n" +
               "    .header { text-align: center; padding: 20px; }\n" +
               "    .content { padding: 20px; background-color: #f9f9f9; }\n" +
               "    .button { display: inline-block; padding: 10px 20px; background-color: #F45E43; color: white; text-decoration: none; border-radius: 4px; }\n" +
               "    .footer { text-align: center; padding: 20px; font-size: 12px; color: #666; }\n" +
               "  </style>\n" +
               "</head>\n" +
               "<body>\n" +
               "  <div class=\"container\">\n" +
               "    <div class=\"header\">\n" +
               "      <img src=\"https://krisefikser.no/logo.png\" alt=\"Krisefikser Logo\" width=\"100\">\n" +
               "    </div>\n" +
               "    <div class=\"content\">\n" +
               "      <h1 style=\"color: #F45E43;\">Velkommen til Krisefikser!</h1>\n" +
               "      <p>Hei " + user.getFirstName() + ",</p>\n" +
               "      <p>Takk for at du registrerte deg hos oss. Klikk på knappen nedenfor for å verifisere e-postadressen din.</p>\n" +
               "      <p><a class=\"button\" href=\"https://krisefikser.no/verify?token=test-token\">Verifiser min e-post</a></p>\n" +
               "      <p>Denne lenken utløper om 24 timer.</p>\n" +
               "    </div>\n" +
               "    <div class=\"footer\">\n" +
               "      <p>© " + Year.now().getValue() + " Krisefikser. Alle rettigheter reservert.</p>\n" +
               "    </div>\n" +
               "  </div>\n" +
               "</body>\n" +
               "</html>";
    }
    
    private void sendEmail(String toEmail, String subject, String htmlContent) throws MessagingException {
        // Sett opp JavaMailSender med Mailtrap-innstillinger
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost("live.smtp.mailtrap.io");
        mailSender.setPort(587);
        mailSender.setUsername("api");
        
        // Hent passordet fra miljøvariabel
        String mailtrapToken = System.getenv("MAILTRAP_API_TOKEN");
        if (mailtrapToken == null || mailtrapToken.isEmpty()) {
            System.out.println("ADVARSEL: MAILTRAP_API_TOKEN miljøvariabel er ikke satt.");
            System.out.println("For testing: sett miljøvariabel med følgende kommando:");
            System.out.println("export MAILTRAP_API_TOKEN=your_api_token_here");
            mailtrapToken = "placeholder_token"; // Dette vil ikke fungere for faktisk sending
        }
        mailSender.setPassword(mailtrapToken);
        
        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.debug", "true");
        
        // Opprett e-post
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
        
        helper.setTo(toEmail);
        helper.setFrom("noreply@krisefikser.app");
        helper.setSubject(subject);
        helper.setText(htmlContent, true); // true indikerer at innholdet er HTML
        
        // Send e-post
        mailSender.send(mimeMessage);
    }
    
    @Test
    void testMjmlEmailSending() {
        // Dette vil ikke faktisk sende en e-post, det er bare for å vise teststruktur
        System.out.println("Kjør denne testen som main() for å sende faktiske e-poster");
    }
} 