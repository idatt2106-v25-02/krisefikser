package stud.ntnu.krisefikser.auth.repository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import stud.ntnu.krisefikser.auth.entity.RefreshToken;

/**
 * Repository interface for managing {@link RefreshToken} entities. This interface extends
 * JpaRepository to provide CRUD operations and custom query methods.
 */
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

  /**
   * Finds a RefreshToken by its token string.
   *
   * @param token the token string
   * @return an Optional containing the RefreshToken if found, or empty if not found
   */
  Optional<RefreshToken> findByToken(String token);
}