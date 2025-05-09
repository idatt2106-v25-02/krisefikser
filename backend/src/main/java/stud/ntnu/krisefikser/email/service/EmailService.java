package stud.ntnu.krisefikser.email.service;

import java.util.Collections;
import java.util.List;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
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

    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(mailProperties.getApiKey());
    headers.setContentType(MediaType.APPLICATION_JSON);

    MailtrapRequest payload = new MailtrapRequest();
    payload.setFrom(new MailtrapAddress(mailProperties.getFrom()));
    payload.setTo(Collections.singletonList(new MailtrapAddress(toEmail)));
    payload.setSubject(subject);
    payload.setHtml(htmlContent);

    HttpEntity<MailtrapRequest> requestEntity = new HttpEntity<>(payload, headers);

    try {
      ResponseEntity<String> response = restTemplate.postForEntity(url, requestEntity,
          String.class);
      log.info("Email sent successfully to {}: Status {}", toEmail, response.getStatusCode());

      if (!response.getStatusCode().is2xxSuccessful()) {
        throw new EmailSendingException("Failed to send email: " + response.getBody());
      }

      return response;
    } catch (Exception e) {
      log.error("Unexpected error sending email to {}: {}", toEmail, e.getMessage(), e);
      throw new EmailSendingException("Failed to send email due to an unexpected error");
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