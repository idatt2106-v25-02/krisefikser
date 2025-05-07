package stud.ntnu.krisefikser.auth.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import stud.ntnu.krisefikser.auth.entity.PasswordResetToken;
import stud.ntnu.krisefikser.common.RepositoryTestConfig;
import stud.ntnu.krisefikser.user.entity.User;

@DataJpaTest
@Import(RepositoryTestConfig.class)
class PasswordResetTokenRepositoryTest {

  @Autowired
  private TestEntityManager entityManager;

  @Autowired
  private PasswordResetTokenRepository passwordResetTokenRepository;

  @Test
  void save_ShouldPersistPasswordResetToken() {
    // Arrange - Create a user
    User user = User.builder()
        .email("reset-token-test@example.com")
        .firstName("Test")
        .lastName("User")
        .password("password")
        .build();

    entityManager.persist(user);

    // Create a token
    PasswordResetToken token = PasswordResetToken.builder()
        .token("test-reset-token")
        .user(user)
        .expiryDate(Instant.now().plusSeconds(3600)) // 1 hour in the future
        .build();

    // Act
    PasswordResetToken savedToken = passwordResetTokenRepository.save(token);
    entityManager.flush();

    // Assert
    assertThat(savedToken.getId()).isNotNull();
    assertThat(savedToken.getToken()).isEqualTo("test-reset-token");
    assertThat(savedToken.getUser().getEmail()).isEqualTo("reset-token-test@example.com");
    assertThat(savedToken.getExpiryDate()).isNotNull();
  }

  @Test
  void findByToken_ShouldReturnToken() {
    // Arrange - Create a user
    User user = User.builder()
        .email("token-lookup-test@example.com")
        .firstName("Test")
        .lastName("User")
        .password("password")
        .build();

    entityManager.persist(user);

    // Create and persist a token
    String tokenString = "find-by-token-test";
    PasswordResetToken token = PasswordResetToken.builder()
        .token(tokenString)
        .user(user)
        .expiryDate(Instant.now().plusSeconds(3600))
        .build();

    entityManager.persist(token);
    entityManager.flush();

    // Act
    Optional<PasswordResetToken> foundToken = passwordResetTokenRepository.findByToken(tokenString);

    // Assert
    assertThat(foundToken).isPresent();
    assertThat(foundToken.get().getToken()).isEqualTo(tokenString);
    assertThat(foundToken.get().getUser().getEmail()).isEqualTo("token-lookup-test@example.com");
  }

  @Test
  void findByToken_WhenTokenDoesNotExist_ShouldReturnEmpty() {
    // Act
    Optional<PasswordResetToken> foundToken = passwordResetTokenRepository.findByToken(
        "non-existent-token");

    // Assert
    assertThat(foundToken).isEmpty();
  }

  @Test
  void findByUser_ShouldReturnAllUserTokens() {
    // Arrange - Create two different users
    User user1 = User.builder()
        .email("multi-token-user1@example.com")
        .firstName("Test")
        .lastName("User")
        .password("password")
        .build();

    User user2 = User.builder()
        .email("multi-token-user2@example.com")
        .firstName("Test")
        .lastName("User")
        .password("password")
        .build();

    entityManager.persist(user1);
    entityManager.persist(user2);

    // Create one token per user
    PasswordResetToken token1 = PasswordResetToken.builder()
        .token("token-1-for-user")
        .user(user1)
        .expiryDate(Instant.now().plusSeconds(3600))
        .build();

    PasswordResetToken token2 = PasswordResetToken.builder()
        .token("token-2-for-user")
        .user(user2)
        .expiryDate(Instant.now().plusSeconds(7200))
        .build();

    entityManager.persist(token1);
    entityManager.persist(token2);
    entityManager.flush();

    // Test with just one user
    List<PasswordResetToken> userTokens = passwordResetTokenRepository.findByUser(user1);

    // Assert
    assertThat(userTokens).hasSize(1);
    assertThat(userTokens).extracting(PasswordResetToken::getToken)
        .containsExactly("token-1-for-user");
  }

  @Test
  void findByUser_WhenNoTokensExist_ShouldReturnEmptyList() {
    // Arrange - Create a user without tokens
    User user = User.builder()
        .email("no-token-user@example.com")
        .firstName("Test")
        .lastName("User")
        .password("password")
        .build();

    entityManager.persist(user);
    entityManager.flush();

    // Act
    List<PasswordResetToken> userTokens = passwordResetTokenRepository.findByUser(user);

    // Assert
    assertThat(userTokens).isEmpty();
  }

  @Test
  void isExpired_WhenTokenIsExpired_ShouldReturnTrue() {
    // Arrange - Create a user
    User user = User.builder()
        .email("expired-token-test@example.com")
        .firstName("Test")
        .lastName("User")
        .password("password")
        .build();

    entityManager.persist(user);

    // Create an expired token (expiry in the past)
    PasswordResetToken expiredToken = PasswordResetToken.builder()
        .token("expired-token")
        .user(user)
        .expiryDate(Instant.now().minusSeconds(3600)) // 1 hour in the past
        .build();

    entityManager.persist(expiredToken);
    entityManager.flush();

    // Act & Assert
    assertThat(expiredToken.isExpired()).isTrue();
  }

  @Test
  void isExpired_WhenTokenIsValid_ShouldReturnFalse() {
    // Arrange - Create a user
    User user = User.builder()
        .email("valid-token-test@example.com")
        .firstName("Test")
        .lastName("User")
        .password("password")
        .build();

    entityManager.persist(user);

    // Create a valid token (expiry in the future)
    PasswordResetToken validToken = PasswordResetToken.builder()
        .token("valid-token")
        .user(user)
        .expiryDate(Instant.now().plusSeconds(3600)) // 1 hour in the future
        .build();

    entityManager.persist(validToken);
    entityManager.flush();

    // Act & Assert
    assertThat(validToken.isExpired()).isFalse();
  }

  @Test
  void deleteToken_ShouldRemoveToken() {
    // Arrange - Create a user
    User user = User.builder()
        .email("delete-token-test@example.com")
        .firstName("Test")
        .lastName("User")
        .password("password")
        .build();

    entityManager.persist(user);

    // Create a token
    PasswordResetToken token = PasswordResetToken.builder()
        .token("token-to-delete")
        .user(user)
        .expiryDate(Instant.now().plusSeconds(3600))
        .build();

    PasswordResetToken persistedToken = entityManager.persist(token);
    entityManager.flush();

    // Act
    passwordResetTokenRepository.delete(persistedToken);
    entityManager.flush();

    // Assert
    Optional<PasswordResetToken> deletedToken = passwordResetTokenRepository.findByToken(
        "token-to-delete");
    assertThat(deletedToken).isEmpty();
  }
}