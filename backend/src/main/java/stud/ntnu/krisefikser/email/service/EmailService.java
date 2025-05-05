package stud.ntnu.krisefikser.email.service;

import java.util.Collections;
import java.util.List;
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

  public EmailService(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

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

  private static class MailtrapRequest {

    private MailtrapAddress from;
    private List<MailtrapAddress> to;
    private String subject;
    private String html;
    // Add other fields like 'text', 'cc', 'bcc', 'attachments' if required

    // Getters and Setters
    public MailtrapAddress getFrom() {
      return from;
    }

    public void setFrom(MailtrapAddress from) {
      this.from = from;
    }

    public List<MailtrapAddress> getTo() {
      return to;
    }

    public void setTo(List<MailtrapAddress> to) {
      this.to = to;
    }

    public String getSubject() {
      return subject;
    }

    public void setSubject(String subject) {
      this.subject = subject;
    }

    public String getHtml() {
      return html;
    }

    public void setHtml(String html) {
      this.html = html;
    }
  }

  private static class MailtrapAddress {

    private String email;
    private String name; // Optional

    public MailtrapAddress(String email) {
      this.email = email;
    }

    public MailtrapAddress(String email, String name) {
      this.email = email;
      this.name = name;
    }

    // Getters and Setters
    public String getEmail() {
      return email;
    }

    public void setEmail(String email) {
      this.email = email;
    }

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }
  }
} 