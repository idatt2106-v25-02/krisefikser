package stud.ntnu.krisefikser.email.service;

import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import stud.ntnu.krisefikser.email.exception.EmailSendingException;

/**
 * Service class for sending admin invitation emails.
 *
 * <p>This class provides a method to send an invitation email to a specified email address. </p>
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class EmailAdminService {

  private final EmailService emailService;
  private final EmailTemplateService emailTemplateService;

  /**
   * Sends an admin invitation email to the specified email address.
   *
   * @param email      The email address to send the invitation to
   * @param inviteLink The invitation link containing the verification token
   * @return ResponseEntity containing the result of the email sending operation
   */
  public ResponseEntity<String> sendAdminInvitation(String email, String inviteLink) {
    if (email == null || inviteLink == null) {
      log.error("Email or invite link is null");
      throw new EmailSendingException("Email or invite link is null");
    }

    try {
      Map<String, String> variables = new HashMap<>();
      variables.put("link", inviteLink);

      String content = emailTemplateService.loadAndReplace("admin-invite.html", variables);

      return emailService.sendEmail(
          email,
          "Admin Invitation - Krisefikser",
          content
      );
    } catch (EmailSendingException e) {
      throw e;
    } catch (Exception e) {
      log.error("Failed to send admin invitation email", e);
      throw new EmailSendingException("Failed to send admin invitation email");
    }
  }
}