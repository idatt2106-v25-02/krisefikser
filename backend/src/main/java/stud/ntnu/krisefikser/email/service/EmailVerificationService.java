package stud.ntnu.krisefikser.email.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import stud.ntnu.krisefikser.email.entity.VerificationToken;
import stud.ntnu.krisefikser.email.repository.VerificationTokenRepository;
import stud.ntnu.krisefikser.user.entity.User;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    private final VerificationTokenRepository tokenRepository;
    private final EmailService emailService;
    private final EmailTemplateService templateService;

    @Value("${verification.token.validity-hours:24}")
    private int tokenValidityHours;

    @Value("${frontend.url}")
    private String frontendUrl;

    public VerificationToken createVerificationToken(User user) {
        // Delete any existing unused tokens for this user
        Optional<VerificationToken> existingToken = tokenRepository.findByUserAndUsed(user, false);
        existingToken.ifPresent(tokenRepository::delete);

        // Create new token
        VerificationToken token = VerificationToken.builder()
            .token(UUID.randomUUID().toString())
            .user(user)
            .expiryDate(LocalDateTime.now().plusHours(tokenValidityHours))
            .used(false)
            .build();

        return tokenRepository.save(token);
    }

    public boolean verifyToken(String token) {
        Optional<VerificationToken> verificationToken = tokenRepository.findByToken(token);

        if (verificationToken.isEmpty() || verificationToken.get().isUsed() || verificationToken.get().isExpired()) {
            return false;
        }

        // Mark token as used
        VerificationToken vt = verificationToken.get();
        vt.setUsed(true);
        tokenRepository.save(vt);

        // Mark user as verified
        User user = vt.getUser();
        user.setEmailVerified(true);

        return true;
    }

    public ResponseEntity<String> sendVerificationEmail(User user, VerificationToken token) {
        try {
            // Create verification link with token
            String verificationLink = frontendUrl + "/api/auth/verify-email?token=" + token.getToken();

            // Prepare template variables
            Map<String, String> templateVariables = new HashMap<>();
            templateVariables.put("name", user.getFirstName());
            templateVariables.put("link", verificationLink);

            // Load and process the verification.html template
            String htmlContent = templateService.loadAndReplace("user-register.html", templateVariables);

            // Send the email
            return emailService.sendEmail(
                user.getEmail(),
                "Verify Your Email Address",
                htmlContent
            );
        } catch (IOException e) {
            throw new RuntimeException("Failed to send verification email: " + e.getMessage(), e);
        }
    }
}