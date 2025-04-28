package stud.ntnu.krisefikser.auth.service;

import io.github.cdimascio.dotenv.Dotenv;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User.UserBuilder;
import org.springframework.security.core.userdetails.UserDetails;
import stud.ntnu.krisefikser.user.entity.User;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Year;
import java.util.Collections;
import java.util.Properties;

/**
 * E-post test som bruker MJML for formattering
 */
public class MjmlEmailTest {

    private final String targetEmail = "janjaboy14@gmail.com";
    private final Dotenv dotenv;

    public MjmlEmailTest() {
        // Finn path til backend-mappen
        Path currentPath = Paths.get("").toAbsolutePath();
        String backendPath = currentPath.toString();

        // Hvis vi er i prosjektets rotmappe, legg til "backend"
        // Juster "Smidig oppgave" hvis prosjektmappen heter noe annet
        if (backendPath.endsWith("Smidig oppgave")) {
            backendPath = Paths.get(backendPath, "backend").toString();
        } else if (!backendPath.endsWith("backend")) {
            // Anta at vi er i en undermappe av backend, gå opp et nivå
            Path parentPath = currentPath.getParent();
            if (parentPath != null && parentPath.toString().endsWith("Smidig oppgave")) {
                backendPath = Paths.get(parentPath.toString(), "backend").toString();
            }
            // Hvis vi fortsatt ikke finner den, logg en advarsel
            else {
                System.err.println("ADVARSEL: Kunne ikke bestemme backend-mappen sikkert. Prøver standard.");
            }
        }


        // Konfigurer dotenv til å lete i backend-mappen
        dotenv = Dotenv.configure()
            .directory(backendPath)
            .ignoreIfMissing() // Ikke kast feil hvis .env mangler
            .load();
    }

    /**
     * Denne testen kan kjøres direkte for å sende en e-post med MJML-formattering
     */
    public static void main(String[] args) {
        new MjmlEmailTest().sendMjmlFormattedEmail();
    }

    public void sendMjmlFormattedEmail() {
        JavaMailSenderImpl mailSender = null;
        try {
            // Last inn properties fra application-test.properties
            Properties properties = PropertiesLoaderUtils.loadProperties(
                new ClassPathResource("application-test.properties")
            );

            // Opprett og konfigurer mailSender
            mailSender = configureMailSender(properties);

            // Opprett bruker og UserDetails
            User user = createTestUser();
            UserDetails userDetails = createUserDetails(user);

            // Kompiler MJML til HTML (forenklet)
            String htmlContent = compileToHtml(user);

            // Send e-post med HTML-innhold
            sendEmail(mailSender, user.getEmail(), "Velkommen til Krisefikser!", htmlContent);

            System.out.println("E-post med MJML-formattering sendt til: " + targetEmail);

        } catch (IOException e) {
            System.err.println("Feil ved lasting av properties: " + e.getMessage());
            e.printStackTrace();
        } catch (MessagingException e) {
            System.err.println("Feil ved sending av e-post: " + e.getMessage());
            if (mailSender != null) {
                System.err.println("Brukte host: " + mailSender.getHost() + ", port: " + mailSender.getPort() + ", brukernavn: " + mailSender.getUsername());
            }
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("En uventet feil oppstod: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private JavaMailSenderImpl configureMailSender(Properties properties) {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(properties.getProperty("spring.mail.host"));
        mailSender.setPort(Integer.parseInt(properties.getProperty("spring.mail.port")));
        mailSender.setUsername(properties.getProperty("spring.mail.username"));

        // Hent API token fra .env fil, deretter miljøvariabel, til slutt properties
        String mailtrapToken = dotenv.get("MAILTRAP_API_TOKEN");
        if (mailtrapToken == null || mailtrapToken.isEmpty()) {
            mailtrapToken = System.getenv("MAILTRAP_API_TOKEN");
            if (mailtrapToken == null || mailtrapToken.isEmpty()) {
                String passwordProperty = properties.getProperty("spring.mail.password");
                if (passwordProperty != null && passwordProperty.contains("${MAILTRAP_API_TOKEN:")) {
                    mailtrapToken = passwordProperty.replace("${MAILTRAP_API_TOKEN:", "").replace("}", "");
                } else {
                    mailtrapToken = passwordProperty; // Bruk verdien direkte hvis den ikke er en placeholder
                }
                if (mailtrapToken == null || mailtrapToken.isEmpty() || mailtrapToken.equals("api_token_here")) {
                    System.out.println("ADVARSEL: MAILTRAP_API_TOKEN er ikke satt i .env, miljøvariabler eller properties. Bruker placeholder.");
                    mailtrapToken = "placeholder_token"; // Dette vil ikke fungere
                } else {
                    System.out.println("Bruker API token fra application-test.properties");
                }
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
        mailProperties.put("mail.debug", "true"); // Sett til false for mindre output
        mailSender.setJavaMailProperties(mailProperties);

        return mailSender;
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
            .password("password") // Passordet brukes ikke i denne testen
            .authorities(Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_USER")))
            .build();
    }

    // Denne metoden er uendret, da den bare simulerer MJML-kompilering
    private String compileToHtml(User user) {
        // I en faktisk implementasjon ville du brukt MJML Java API eller NPM for å kompilere
        // Nå genererer vi enkel HTML direkte for testing
        return convertSimpleMjmlToHtml(user);
    }

    // Denne metoden er uendret
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

    // Modifisert for å ta imot en ferdigkonfigurert mailSender
    private void sendEmail(JavaMailSenderImpl mailSender, String toEmail, String subject, String htmlContent) throws MessagingException {
        // Opprett e-post
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        // Bruk true for multipart message for robust HTML/alternativ tekst-håndtering
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

        helper.setTo(toEmail);
        helper.setFrom("noreply@krisefikser.app"); // Eller hent fra properties hvis ønskelig
        helper.setSubject(subject);
        helper.setText(htmlContent, true); // true indikerer at innholdet er HTML

        // Send e-post
        mailSender.send(mimeMessage);
    }

    @Test
    void testMjmlEmailSending() {
        // Denne testen kan nå kjøres direkte fra IDE, den vil prøve å sende e-post
        // hvis konfigurasjonen er riktig.
        System.out.println("Starter MjmlEmailTest...");
        sendMjmlFormattedEmail();
        System.out.println("MjmlEmailTest fullført.");
    }
}