package stud.ntnu.krisefikser.email.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import stud.ntnu.krisefikser.common.RepositoryTestConfig;
import stud.ntnu.krisefikser.email.entity.VerificationToken;
import stud.ntnu.krisefikser.user.entity.User;

@DataJpaTest
@Import(RepositoryTestConfig.class)
class VerificationTokenRepositoryTest {

  @Autowired
  private TestEntityManager entityManager;

  @Autowired
  private VerificationTokenRepository verificationTokenRepository;

  @Test
  void findByToken_ExistingToken_ShouldReturnToken() {
    // Arrange
    User user = createAndPersistUser("test@example.com");
    String tokenValue = "abc123";
    VerificationToken token = createAndPersistToken(tokenValue, user, false);

    // Act
    Optional<VerificationToken> found = verificationTokenRepository.findByToken(tokenValue);

    // Assert
    assertThat(found).isPresent();
    assertThat(found.get().getToken()).isEqualTo(tokenValue);
    assertThat(found.get().getUser().getId()).isEqualTo(user.getId());
  }

  private User createAndPersistUser(String email) {
    return entityManager.persist(createUser(email));
  }

  private VerificationToken createAndPersistToken(String token, User user, boolean used) {
    VerificationToken verificationToken = new VerificationToken();
    verificationToken.setToken(token);
    verificationToken.setUser(user);
    verificationToken.setUsed(used);
    verificationToken.setExpiryDate(LocalDateTime.now().plusDays(1));
    return entityManager.persist(verificationToken);
  }

  // Helper methods
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
    Optional<VerificationToken> found = verificationTokenRepository.findByToken("nonexistent");

    // Assert
    assertThat(found).isEmpty();
  }

  @Test
  void findByUserAndUsed_ExistingUnusedToken_ShouldReturnToken() {
    // Arrange
    User user = createAndPersistUser("test@example.com");
    VerificationToken token = createAndPersistToken("abc123", user, false);

    // Act
    Optional<VerificationToken> found = verificationTokenRepository.findByUserAndUsed(user, false);

    // Assert
    assertThat(found).isPresent();
    assertThat(found.get().getId()).isEqualTo(token.getId());
  }

  @Test
  void findByUserAndUsed_ExistingUsedToken_ShouldReturnToken() {
    // Arrange
    User user = createAndPersistUser("test@example.com");
    VerificationToken token = createAndPersistToken("abc123", user, true);

    // Act
    Optional<VerificationToken> found = verificationTokenRepository.findByUserAndUsed(user, true);

    // Assert
    assertThat(found).isPresent();
    assertThat(found.get().getId()).isEqualTo(token.getId());
  }

  @Test
  void findByUserAndUsed_NonMatchingUsedStatus_ShouldReturnEmpty() {
    // Arrange
    User user = createAndPersistUser("test@example.com");
    createAndPersistToken("abc123", user, true); // Create a used token

    // Act - Look for unused token
    Optional<VerificationToken> found = verificationTokenRepository.findByUserAndUsed(user, false);

    // Assert
    assertThat(found).isEmpty();
  }

  @Test
  void findByUserAndUsed_NonExistingUser_ShouldReturnEmpty() {
    // Arrange
    User existingUser = createAndPersistUser("existing@example.com");
    createAndPersistToken("abc123", existingUser, false);

    User nonExistingUser = createUser("nonexisting@example.com");
    nonExistingUser.setId(UUID.randomUUID()); // Ensure unique ID

    // Act
    Optional<VerificationToken> found = verificationTokenRepository.findByUserAndUsed(
        nonExistingUser, false);

    // Assert
    assertThat(found).isEmpty();
  }

  @Test
  void findByUserAndUsed_MultipleTokensForSameUser_ShouldReturnMatchingToken() {
    // Arrange
    User user = createAndPersistUser("test@example.com");
    VerificationToken unusedToken = createAndPersistToken("unused123", user, false);
    createAndPersistToken("used456", user, true);

    // Act
    Optional<VerificationToken> found = verificationTokenRepository.findByUserAndUsed(user, false);

    // Assert
    assertThat(found).isPresent();
    assertThat(found.get().getId()).isEqualTo(unusedToken.getId());
    assertThat(found.get().isUsed()).isFalse();
  }
}