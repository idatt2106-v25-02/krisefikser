package stud.ntnu.krisefikser.auth.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import stud.ntnu.krisefikser.auth.entity.PasswordResetToken;
import stud.ntnu.krisefikser.user.entity.User;

/**
 * Repository interface for managing PasswordResetToken entities. This interface extends
 * JpaRepository to provide CRUD operations and custom query methods.
 */
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

  /**
   * Finds a PasswordResetToken by its token string.
   *
   * @param token the token string to search for
   * @return an Optional containing the found PasswordResetToken, or empty if not found
   */
  Optional<PasswordResetToken> findByToken(String token);

  /**
   * Finds all PasswordResetTokens associated with a specific User.
   *
   * @param user the User whose tokens are to be found
   * @return a List of PasswordResetTokens associated with the User
   */
  List<PasswordResetToken> findByUser(User user);
}
