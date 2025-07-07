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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;

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
    log.info("API KEY THAT IS READ FROM APPLICATION PROPERTIES: " + mailProperties.getApiKey());
    headers.setBearerAuth(mailProperties.getApiKey());
    headers.setContentType(MediaType.APPLICATION_JSON);

    MailtrapRequest payload = new MailtrapRequest();
    log.info("FROM ADDRESS THAT IS READ FROM APPLICATION PROPERTIES: " + new MailtrapAddress(mailProperties.getFrom()).toString());
    payload.setFrom(new MailtrapAddress(mailProperties.getFrom()));
    payload.setTo(Collections.singletonList(new MailtrapAddress(toEmail)));
    log.info("SUBJECT THAT IS READ FROM APPLICATION PROPERTIES: " + subject);
    payload.setSubject(subject);
    log.info("HTMLCONTENT THAT IS READ FROM APPLICATION PROPERTIES" + htmlContent);
    payload.setHtml(htmlContent);
    payload.setText(stripHtmlTags(htmlContent));

    try {
      ObjectMapper mapper = new ObjectMapper();
      mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
      log.info("Serialized Payload: \n{}", mapper.writeValueAsString(payload));

    } catch (Exception e) {
      log.info("PLEASE SEND HELP - UGHHHHHHHHHH");
    }

    HttpEntity<MailtrapRequest> requestEntity = new HttpEntity<>(payload, headers);

    try {
      log.info("INSIDE THE TRY BLOCK!!!!");
      log.info(url);
      log.info(requestEntity.toString());

      ResponseEntity<String> response = restTemplate.postForEntity(url, requestEntity,
          String.class);
      log.info("Email sent successfully to {}: Status {}", toEmail, response.getStatusCode());

      if (!response.getStatusCode().is2xxSuccessful()) {
        log.info("THIS IS INSIDE THE TRY BLOCK IN EMAILSERVICE");
        throw new EmailSendingException("Failed to send email: " + response.getBody());
      }

      return response;
    } catch (Exception e) {

        log.info("THIS IS INSIDE THE CATCH BLOCK IN EMAILSERVICE");

      log.error("Unexpected error sending email to {}: {}", toEmail, e.getMessage(), e);
      throw new EmailSendingException("Failed to send email due to an unexpected error");
    }
  }

  private String stripHtmlTags(String html) {
  return html.replaceAll("<[^>]*>", ""); // Simple HTML tag remover
}


  // --- Mailtrap API DTOs ---
  // Note: Adjust these based on the actual Mailtrap API documentation if needed.

  @Data
  private static class MailtrapRequest {

    private MailtrapAddress from;
    private List<MailtrapAddress> to;
    private String subject;
    private String html;
    private String text;
  }

  @Data
  @JsonInclude(JsonInclude.Include.NON_NULL) // <-- ADD THIS
  private static class MailtrapAddress {

    private String email;
    private String name; // Optional

    public MailtrapAddress(String email) {
      this.email = email;
    }
  }
} 