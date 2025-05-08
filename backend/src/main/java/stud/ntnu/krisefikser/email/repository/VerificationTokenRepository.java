package stud.ntnu.krisefikser.email.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import stud.ntnu.krisefikser.email.entity.VerificationToken;
import stud.ntnu.krisefikser.user.entity.User;

import java.util.Optional;
import java.util.UUID;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, UUID> {
    Optional<VerificationToken> findByToken(String token);
    Optional<VerificationToken> findByUserAndUsed(User user, boolean used);
}