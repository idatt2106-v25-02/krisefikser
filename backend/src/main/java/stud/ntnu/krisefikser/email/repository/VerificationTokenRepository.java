package stud.ntnu.krisefikser.email.repository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import stud.ntnu.krisefikser.email.entity.VerificationToken;
import stud.ntnu.krisefikser.user.entity.User;

/**
 * Repository interface for managing {@link VerificationToken} entities.
 *
 * <p>This interface extends {@link JpaRepository} to provide CRUD operations and custom query
 * methods for {@link VerificationToken} entities.
 * </p>
 */
public interface VerificationTokenRepository extends JpaRepository<VerificationToken, UUID> {

  /**
   * Finds a {@link VerificationToken} by its token string.
   *
   * @param token the token string to search for
   * @return an {@link Optional} containing the found {@link VerificationToken}, or empty if not
   * found
   */
  Optional<VerificationToken> findByToken(String token);

  /**
   * Finds a {@link VerificationToken} by its associated {@link User} and whether it has been used.
   *
   * @param user the {@link User} associated with the token
   * @param used whether the token has been used
   * @return an {@link Optional} containing the found {@link VerificationToken}, or empty if not
   * found
   */
  Optional<VerificationToken> findByUserAndUsed(User user, boolean used);
}