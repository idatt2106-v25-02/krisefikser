package stud.ntnu.krisefikser.email.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import stud.ntnu.krisefikser.email.entity.VerificationToken;
import stud.ntnu.krisefikser.email.repository.VerificationTokenRepository;
import stud.ntnu.krisefikser.user.entity.User;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    private final VerificationTokenRepository tokenRepository;
    private final EmailService emailService;
    
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
        String verificationLink = frontendUrl + "/verify?token=" + token.getToken();
        String htmlContent = createVerificationEmailHtml(user.getFirstName(), verificationLink);
        
        return emailService.sendEmail(
            user.getEmail(),
            "Please verify your email address",
            htmlContent
        );
    }
    
    private String createVerificationEmailHtml(String firstName, String verificationLink) {
        return "<html><body>" +
               "<h2>Welcome to Krisefikser!</h2>" +
               "<p>Hello " + firstName + ",</p>" +
               "<p>Thank you for registering. Please click the link below to verify your email address:</p>" +
               "<p><a href='" + verificationLink + "'>Verify Email Address</a></p>" +
               "<p>This link will expire in " + tokenValidityHours + " hours.</p>" +
               "<p>If you did not create this account, you can safely ignore this email.</p>" +
               "</body></html>";
    }
}