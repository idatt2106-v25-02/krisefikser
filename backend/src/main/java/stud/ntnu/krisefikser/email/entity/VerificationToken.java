package stud.ntnu.krisefikser.email.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import stud.ntnu.krisefikser.user.entity.User;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity class representing a verification token used for email verification.
 * This class is used to store and manage tokens that are sent to users for email verification.
 *
 * @author Krisefikser Team
 */
@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "verification_tokens")
public class VerificationToken {
    
    /**
     * Unique identifier for the verification token.
     * Generated automatically using UUID strategy.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    /**
     * The actual token string used for verification.
     * Must be unique and cannot be null.
     */
    @Column(nullable = false, unique = true)
    private String token;
    
    /**
     * The user associated with this verification token.
     * Uses eager fetching to ensure user data is always available.
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    /**
     * The expiration date and time of the token.
     * After this time, the token will be considered expired.
     */
    @Column(nullable = false)
    private LocalDateTime expiryDate;
    
    /**
     * Flag indicating whether the token has been used for verification.
     * Default value is false.
     */
    @Column(nullable = false)
    private boolean used = false;
    
    /**
     * Checks if the token has expired by comparing the current time with the expiry date.
     *
     * @return true if the token has expired, false otherwise
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiryDate);
    }
}