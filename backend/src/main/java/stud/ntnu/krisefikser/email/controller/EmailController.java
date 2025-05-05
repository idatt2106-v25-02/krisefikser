package stud.ntnu.krisefikser.email.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import stud.ntnu.krisefikser.email.dto.EmailRequestDto;
import stud.ntnu.krisefikser.email.service.EmailService;

/**
 * Controller for handling email-related requests.
 *
 * <p>This controller provides endpoints for sending emails and verifying email addresses.
 */
@RestController
@RequestMapping("/api/email")
public class EmailController {

  private final EmailService emailService;

  /**
   * Constructs a new EmailController with the specified EmailService.
   *
   * @param emailService the EmailService to be used for sending emails
   */
  public EmailController(EmailService emailService) {
    this.emailService = emailService;
  }

  /**
   * Endpoint for sending an email.
   *
   * <p>This method handles POST requests to send an email. It takes an EmailRequestDto object as
   * input, which contains the recipient's email address and the verification link. The method
   * returns a ResponseEntity indicating the success or failure of the email sending operation.
   *
   * @param requestDto the EmailRequestDto containing the recipient's email address and verification
   *                   link
   * @return a ResponseEntity indicating the result of the email sending operation
   */
  @PostMapping("/send")
  public ResponseEntity<String> sendEmail(@RequestBody EmailRequestDto requestDto) {
    try {
      return emailService.sendEmail(
          requestDto.toEmail(),
          "Please verify your email",
          requestDto.verificationLink()
      );
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("Email sending failed: " + e.getMessage());
    }
  }
}
