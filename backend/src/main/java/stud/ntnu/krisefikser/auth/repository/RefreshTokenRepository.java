package stud.ntnu.krisefikser.auth.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import stud.ntnu.krisefikser.auth.entity.RefreshToken;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
  Optional<RefreshToken> findByToken(String token);
  boolean existsByToken(String token);
}