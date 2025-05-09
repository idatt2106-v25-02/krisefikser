package stud.ntnu.krisefikser.auth.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import stud.ntnu.krisefikser.auth.entity.RefreshToken;
import stud.ntnu.krisefikser.common.RepositoryTestConfig;
import stud.ntnu.krisefikser.user.entity.User;

@DataJpaTest
@Import(RepositoryTestConfig.class)
class RefreshTokenRepositoryTest {

  @Autowired
  private TestEntityManager entityManager;

  @Autowired
  private RefreshTokenRepository refreshTokenRepository;

  @Test
  void findByToken_ExistingToken_ShouldReturnToken() {
    // Arrange
    User user = createAndPersistUser("refresh-user@example.com");
    String tokenValue = "valid-refresh-token-123";
    RefreshToken token = createAndPersistRefreshToken(tokenValue, user);

    // Act
    Optional<RefreshToken> found = refreshTokenRepository.findByToken(tokenValue);

    // Assert
    assertThat(found).isPresent();
    assertThat(found.get().getToken()).isEqualTo(tokenValue);
    assertThat(found.get().getUser().getId()).isEqualTo(user.getId());
  }

  private User createAndPersistUser(String email) {
    return entityManager.persist(createUser(email));
  }

  private RefreshToken createAndPersistRefreshToken(String token, User user) {
    RefreshToken refreshToken = new RefreshToken();
    refreshToken.setToken(token);
    refreshToken.setUser(user);
    return entityManager.persist(refreshToken);
  }

  private User createUser(String email) {
    User user = new User();
    user.setEmail(email);
    user.setPassword("password");
    user.setFirstName("Test");
    user.setLastName("User");
    return user;
  }

  @Test
  void findByToken_NonExistingToken_ShouldReturnEmpty() {
    // Act
    Optional<RefreshToken> found = refreshTokenRepository.findByToken("nonexistent-token");

    // Assert
    assertThat(found).isEmpty();
  }

  @Test
  void findByToken_NullToken_ShouldReturnEmpty() {
    // Act
    Optional<RefreshToken> found = refreshTokenRepository.findByToken(null);

    // Assert
    assertThat(found).isEmpty();
  }

  @Test
  void findByToken_MultipleTokensExist_ShouldReturnMatchingToken() {
    // Arrange
    User user1 = createAndPersistUser("user1@example.com");
    User user2 = createAndPersistUser("user2@example.com");

    String tokenValue1 = "token-for-user1";
    String tokenValue2 = "token-for-user2";

    RefreshToken token1 = createAndPersistRefreshToken(tokenValue1, user1);
    RefreshToken token2 = createAndPersistRefreshToken(tokenValue2, user2);

    // Act
    Optional<RefreshToken> found = refreshTokenRepository.findByToken(tokenValue2);

    // Assert
    assertThat(found).isPresent();
    assertThat(found.get().getToken()).isEqualTo(tokenValue2);
    assertThat(found.get().getUser().getId()).isEqualTo(user2.getId());
  }
}