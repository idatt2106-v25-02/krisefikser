package stud.ntnu.krisefikser.email.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import stud.ntnu.krisefikser.email.dto.EmailRequestDto;
import stud.ntnu.krisefikser.email.service.EmailService;

@RestController
@RequestMapping("/api/email")
public class EmailController {

    private final EmailService emailService;

    public EmailController(EmailService emailService) {
        this.emailService = emailService;
    }

    @PostMapping("/send")
    public ResponseEntity<String> sendEmail(@RequestBody EmailRequestDto requestDto) {
        try {
            return emailService.sendEmail(
                requestDto.toEmail(),
                "Please verify your email",
                requestDto.verificationLink()
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Email sending failed: " + e.getMessage());
        }
    }

    // metoden skal kobles opp til authService, men da trener man en verifyEmial metode, men først må man fikse
    //det med verification tokens.
    /*
    @PostMapping("/verify")
    public ResponseEntity<String> verifyEmail(@RequestParam String token) {
        boolean isVerified = authService.verifyEmail(token);
        if (isVerified) {
            return ResponseEntity.ok("Email verified successfully.");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid verification token.");
        }
    }
    */

}
