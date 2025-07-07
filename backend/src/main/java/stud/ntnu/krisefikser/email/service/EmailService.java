package stud.ntnu.krisefikser.email.service;

import java.util.Collections;
import java.util.List;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestTemplate;
import stud.ntnu.krisefikser.email.config.MailProperties;
import stud.ntnu.krisefikser.email.exception.EmailSendingException;

/**
 * Service class for sending emails using Mailtrap API.
 *
 * <p>This class provides methods to send emails with HTML content using the Mailtrap service.
 * It handles the HTTP requests and responses, including error handling.
 */
@Service
@RequiredArgsConstructor
public class EmailService {

  private static final Logger log = LoggerFactory.getLogger(EmailService.class);

  private final RestTemplate restTemplate;

  private final MailProperties mailProperties;

  /**
   * Sends an email using the Mailtrap API.
   *
   * <p>This method constructs the request payload and headers, sends the email, and handles
   * responses and errors.
   *
   * @param toEmail     the recipient's email address
   * @param subject     the subject of the email
   * @param htmlContent the HTML content of the email
   * @return the response entity containing the status and body of the response
   */
  public ResponseEntity<String> sendEmail(String toEmail, String subject, String htmlContent) {
    if (toEmail == null) {
      log.error("Cannot send email: recipient email is null");
      throw new EmailSendingException("Recipient email cannot be null");
    }
    if (subject == null) {
      log.error("Cannot send email: subject is null");
      throw new EmailSendingException("Email subject cannot be null");
    }
    if (htmlContent == null) {
      log.error("Cannot send email: HTML content is null");
      throw new EmailSendingException("Email content cannot be null");
    }

    String url = "https://" + mailProperties.getHost() + "/api/send";

    try {
      ClassPathResource resource = new ClassPathResource("templates/verification.html");
      String template = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);

      String name = toEmail.split("@")[0];

      String finalHtml = template.replace("{{name}}", name).replace("{{link}}", htmlContent);

      MailtrapRequest requestPayload = new MailtrapRequest();
      MailtrapAddress from = new MailtrapAddress("noreply@krisefikser.app");
      from.setName("Krisefikser");
      requestPayload.setFrom(from);
      requestPayload.setTo(Collections.singletonList(new MailtrapAddress(toEmail)));
      requestPayload.setSubject(subject);
      requestPayload.setHtml(finalHtml);

      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);
      headers.setBearerAuth(mailProperties.getApiKey());

      HttpEntity<MailtrapRequest> requestEntity = new HttpEntity<>(requestPayload, headers);

      ResponseEntity<String> response = restTemplate.postForEntity(url, requestEntity, String.class);
      log.info("Mailtrap API response: {}", response.getBody());
      return response;
    } catch (IOException e) {
      log.error("Could not read email template", e);
      throw new EmailSendingException("Could not read email template", e);
    } catch (Exception e) {
      log.error("Error sending email", e);
      throw new EmailSendingException("Failed to send email", e);
    }
  }

  // --- Mailtrap API DTOs ---
  // Note: Adjust these based on the actual Mailtrap API documentation if needed.

  @Data
  private static class MailtrapRequest {

    private MailtrapAddress from;
    private List<MailtrapAddress> to;
    private String subject;
    private String html;
  }

  @Data
  private static class MailtrapAddress {

    private String email;
    private String name; // Optional

    public MailtrapAddress(String email) {
      this.email = email;
    }
  }
} 