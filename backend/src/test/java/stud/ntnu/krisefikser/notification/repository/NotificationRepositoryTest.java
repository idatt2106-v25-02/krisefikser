package stud.ntnu.krisefikser.notification.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import stud.ntnu.krisefikser.common.RepositoryTestConfig;
import stud.ntnu.krisefikser.notification.entity.Notification;
import stud.ntnu.krisefikser.notification.entity.NotificationType;
import stud.ntnu.krisefikser.user.entity.User;
import stud.ntnu.krisefikser.user.repository.UserRepository;

@DataJpaTest
@Import(RepositoryTestConfig.class)
class NotificationRepositoryTest {

  @Autowired
  private NotificationRepository notificationRepository;

  @Autowired
  private UserRepository userRepository;

  private User user1;
  private User emptyUser;

  @BeforeEach
  void setUp() {
    // Clear previous data
    notificationRepository.deleteAll();
    userRepository.deleteAll();

    // Create test users
    user1 = User.builder()
        .email("user1@example.com")
        .password("password")
        .build();
    user1 = userRepository.save(user1);

    User user2 = User.builder()
        .email("user2@example.com")
        .password("password")
        .build();
    user2 = userRepository.save(user2);

    emptyUser = User.builder()
        .email("empty@example.com")
        .password("password")
        .build();
    emptyUser = userRepository.save(emptyUser);

    // Create notifications
    Notification notification1 = Notification.builder()
        .user(user1)
        .title("Test Notification 1")
        .message("Message 1")
        .type(NotificationType.INFO)
        .isRead(false)
        .createdAt(LocalDateTime.now().minusDays(1))
        .build();

    Notification notification2 = Notification.builder()
        .user(user1)
        .title("Test Notification 2")
        .message("Message 2")
        .type(NotificationType.EVENT)
        .isRead(true)
        .createdAt(LocalDateTime.now())
        .build();

    Notification notification3 = Notification.builder()
        .user(user2)
        .title("Test Notification 3")
        .message("Message 3")
        .type(NotificationType.INVITE)
        .isRead(false)
        .createdAt(LocalDateTime.now())
        .build();

    notificationRepository.saveAll(List.of(notification1, notification2, notification3));
  }

  @Test
  void findByUser_WithPagination_ShouldReturnUserNotifications() {
    // Arrange
    Pageable pageable = PageRequest.of(0, 10);

    // Act
    Page<Notification> result = notificationRepository.findByUser(user1, pageable);

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.getContent()).hasSize(2);
    assertThat(result.getContent()).extracting("title")
        .containsExactlyInAnyOrder("Test Notification 1", "Test Notification 2");
  }

  @Test
  void findByUser_WithPagination_ShouldReturnEmptyPageForUserWithNoNotifications() {
    // Arrange
    Pageable pageable = PageRequest.of(0, 10);

    // Act
    Page<Notification> result = notificationRepository.findByUser(emptyUser, pageable);

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.getContent()).isEmpty();
  }

  @Test
  void findByUser_WithoutPagination_ShouldReturnAllUserNotifications() {
    // Act
    List<Notification> result = notificationRepository.findByUser(user1);

    // Assert
    assertThat(result).hasSize(2);
    assertThat(result).extracting("title")
        .containsExactlyInAnyOrder("Test Notification 1", "Test Notification 2");
  }

  @Test
  void findByUser_WithoutPagination_ShouldReturnEmptyListForUserWithNoNotifications() {
    // Act
    List<Notification> result = notificationRepository.findByUser(emptyUser);

    // Assert
    assertThat(result).isEmpty();
  }

  @Test
  void countByIsReadAndUser_ShouldReturnCorrectCountForUnreadNotifications() {
    // Act
    Long result = notificationRepository.countByIsReadAndUser(false, user1);

    // Assert
    assertThat(result).isEqualTo(1);
  }

  @Test
  void countByIsReadAndUser_ShouldReturnCorrectCountForReadNotifications() {
    // Act
    Long result = notificationRepository.countByIsReadAndUser(true, user1);

    // Assert
    assertThat(result).isEqualTo(1);
  }

  @Test
  void countByIsReadAndUser_ShouldReturnZeroForUserWithNoNotifications() {
    // Act
    Long resultUnread = notificationRepository.countByIsReadAndUser(false, emptyUser);
    Long resultRead = notificationRepository.countByIsReadAndUser(true, emptyUser);

    // Assert
    assertThat(resultUnread).isZero();
    assertThat(resultRead).isZero();
  }

  @Test
  void standardCrudMethods_ShouldWorkCorrectly() {
    // Test save
    Notification newNotification = Notification.builder()
        .user(user1)
        .title("New Notification")
        .message("New Message")
        .type(NotificationType.INFO)
        .isRead(false)
        .build();

    Notification saved = notificationRepository.save(newNotification);
    assertThat(saved.getId()).isNotNull();

    // Test findById
    Optional<Notification> found = notificationRepository.findById(saved.getId());
    assertThat(found).isPresent();
    assertThat(found.get().getTitle()).isEqualTo("New Notification");

    // Test findById with non-existent ID
    Optional<Notification> notFound = notificationRepository.findById(UUID.randomUUID());
    assertThat(notFound).isEmpty();

    // Test delete
    notificationRepository.deleteById(saved.getId());
    assertThat(notificationRepository.findById(saved.getId())).isEmpty();

    // Test findAll
    List<Notification> all = notificationRepository.findAll();
    assertThat(all).hasSize(3); // Original 3 notifications
  }
}