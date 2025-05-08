package stud.ntnu.krisefikser.email.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EmailAdminService {
    private final EmailService emailService;
    private final EmailTemplateService emailTemplateService;

    /**
     * Sends an admin invitation email to the specified email address
     * @param email The email address to send the invitation to
     * @param inviteLink The invitation link containing the verification token
     * @return ResponseEntity containing the response from the email service
     */
    public ResponseEntity<String> sendAdminInvitation(String email, String inviteLink) {
        if (email == null || inviteLink == null) {
            return ResponseEntity.badRequest()
                .body("Email and invite link cannot be null");
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
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body("Failed to send admin invitation: " + e.getMessage());
        }
    }
} 