package stud.ntnu.krisefikser.email.controller;

import stud.ntnu.krisefikser.email.dto.EmailRequestDto;
import stud.ntnu.krisefikser.email.service.EmailService;
import stud.ntnu.krisefikser.email.service.EmailTemplateService;
import java.io.IOException;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/email")
public class EmailController {

    private static final Logger log = LoggerFactory.getLogger(EmailController.class);

    private final EmailService emailService;
    private final EmailTemplateService templateService;

    public EmailController(EmailService emailService, EmailTemplateService templateService) {
        this.emailService = emailService;
        this.templateService = templateService;
    }

    @PostMapping("/test")
    public ResponseEntity<String> sendTestEmail(@RequestBody EmailRequestDto requestDto) {
        log.info("Received test email request for: {}", requestDto.toEmail());

        try {
            // Prepare variables for the template
            Map<String, String> variables = Map.of(
                    "name", requestDto.name(),
                    "link", requestDto.verificationLink()
            );

            // Load and populate the HTML template
            String htmlContent = templateService.loadAndReplace("verification.html", variables);

            // Send the email
            ResponseEntity<String> response = emailService.sendEmail(
                    requestDto.toEmail(),
                    "Please verify your email",
                    htmlContent
            );

            // Return the response from EmailService
            return response;

        } catch (IOException e) {
            log.error("Failed to load or process email template: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Error processing email template: " + e.getMessage());
        } catch (Exception e) {
            log.error("An unexpected error occurred while sending test email: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("An unexpected error occurred: " + e.getMessage());
        }
    }
} 