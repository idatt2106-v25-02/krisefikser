package stud.ntnu.krisefikser.email.service;

import java.util.Collections;
import java.util.List;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * Service class for sending emails using Mailtrap API.
 *
 * <p>This class provides methods to send emails with HTML content using the Mailtrap service.
 * It handles the HTTP requests and responses, including error handling.
 */
@Service
public class EmailService {

  private static final Logger log = LoggerFactory.getLogger(EmailService.class);

  private final RestTemplate restTemplate;

  @Value("${mail.host}")
  private String mailtrapHost;

  @Value("${mail.api-key}")
  private String mailtrapApiKey;

  @Value("${mail.from}")
  private String defaultFromEmail;

  /**
   * Constructs a new EmailService with the specified RestTemplate.
   *
   * @param restTemplate the RestTemplate to be used for making HTTP requests
   */
  public EmailService(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  /**
   * Sends an email using the Mailtrap API.
   *
   * <p>This method constructs the request payload and headers, sends the email, and handles
   * responses and errors.
   *
   * @param toEmail     the recipient's email address
   * @param subject     the subject of the email
   * @param htmlContent the HTML content of the email
   * @return a ResponseEntity containing the response from the Mailtrap API
   */
  public ResponseEntity<String> sendEmail(String toEmail, String subject, String htmlContent) {
    String url = "https://" + mailtrapHost + "/api/send"; // Assuming /api/send is the correct path

    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(mailtrapApiKey);
    headers.setContentType(MediaType.APPLICATION_JSON);

    MailtrapRequest payload = new MailtrapRequest();
    payload.setFrom(new MailtrapAddress(defaultFromEmail)); // Could add name here if needed
    payload.setTo(Collections.singletonList(new MailtrapAddress(toEmail)));
    payload.setSubject(subject);
    payload.setHtml(htmlContent);

    HttpEntity<MailtrapRequest> requestEntity = new HttpEntity<>(payload, headers);

    try {
      ResponseEntity<String> response = restTemplate.postForEntity(url, requestEntity,
          String.class);
      log.info("Email sent successfully to {}: Status {}", toEmail, response.getStatusCode());
      return response;
    } catch (HttpClientErrorException e) {
      log.error("Client error sending email to {}: {} - {}", toEmail, e.getStatusCode(),
          e.getResponseBodyAsString(), e);
      return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
    } catch (RestClientException e) {
      log.error("Error sending email to {}: {}", toEmail, e.getMessage(), e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("Failed to send email: " + e.getMessage());
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