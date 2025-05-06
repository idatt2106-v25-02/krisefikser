package stud.ntnu.krisefikser.notification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;

import stud.ntnu.krisefikser.notification.dto.NotificationResponse;
import stud.ntnu.krisefikser.notification.entity.Notification;
import stud.ntnu.krisefikser.notification.entity.NotificationType;
import stud.ntnu.krisefikser.notification.repository.NotificationRepository;
import stud.ntnu.krisefikser.user.entity.User;
import stud.ntnu.krisefikser.user.service.UserService;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

  @Mock
  private NotificationRepository notificationRepository;

  @Mock
  private NotificationWebSocketService notificationWebSocketService;

  @Mock
  private UserService userService;

  @InjectMocks
  private NotificationService notificationService;

  private User testUser;
  private Notification testNotification;
  private NotificationResponse testNotificationResponse;
  private UUID testNotificationId;
  private Pageable pageable;

  @BeforeEach
  void setUp() {
    testUser = User.builder()
        .id(UUID.randomUUID())
        .email("test@example.com")
        .build();

    testNotificationId = UUID.fromString("d0e69f2c-6805-471c-adb0-c4f51dc40545");

    testNotification = Notification.builder()
        .id(testNotificationId)
        .user(testUser)
        .title("Test Notification")
        .message("This is a test notification")
        .type(NotificationType.INFO)
        .url("http://example.com/test")
        .isRead(false)
        .createdAt(LocalDateTime.now())
        .build();

    testNotificationResponse = NotificationResponse.builder()
        .id(testNotificationId)
        .title("Test Notification")
        .message("This is a test notification")
        .type(NotificationType.INFO)
        .url("http://example.com/test")
        .read(false)
        .createdAt(testNotification.getCreatedAt())
        .build();

    pageable = PageRequest.of(0, 10);
  }

  @Test
  void createNotification_ShouldReturnNotificationResponseAndSendWebsocketNotification() {
    // Arrange
    when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);
    doNothing().when(notificationWebSocketService)
        .sendNotification(any(User.class), any(NotificationResponse.class));

    // Act
    NotificationResponse result = notificationService.createNotification(testNotification);

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(testNotificationId);
    assertThat(result.getTitle()).isEqualTo(testNotification.getTitle());
    assertThat(result.getMessage()).isEqualTo(testNotification.getMessage());
    assertThat(result.getType()).isEqualTo(testNotification.getType());

    verify(notificationRepository).save(testNotification);
    verify(notificationWebSocketService).sendNotification(eq(testUser),
        any(NotificationResponse.class));
  }

  @Test
  void getNotifications_WithPagination_ShouldReturnPageOfNotificationResponses() {
    // Arrange
    List<Notification> notifications = List.of(testNotification);
    Page<Notification> notificationPage =
        new PageImpl<>(notifications, pageable, notifications.size());

    when(userService.getCurrentUser()).thenReturn(testUser);
    when(notificationRepository.findByUser(testUser, pageable)).thenReturn(notificationPage);

    // Act
    Page<NotificationResponse> result = notificationService.getNotifications(pageable);

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getContent().get(0).getId()).isEqualTo(testNotificationId);
    assertThat(result.getContent().get(0).getTitle()).isEqualTo(testNotification.getTitle());

    verify(userService).getCurrentUser();
    verify(notificationRepository).findByUser(testUser, pageable);
  }

  @Test
  void getNotifications_WithoutPagination_ShouldReturnListOfNotificationResponses() {
    // Arrange
    List<Notification> notifications = List.of(testNotification);

    when(userService.getCurrentUser()).thenReturn(testUser);
    when(notificationRepository.findByUser(testUser)).thenReturn(notifications);

    // Act
    List<NotificationResponse> result = notificationService.getNotifications();

    // Assert
    assertThat(result).isNotNull();
    assertThat(result).hasSize(1);
    assertThat(result.get(0).getId()).isEqualTo(testNotificationId);
    assertThat(result.get(0).getTitle()).isEqualTo(testNotification.getTitle());

    verify(userService).getCurrentUser();
    verify(notificationRepository).findByUser(testUser);
  }

  @Test
  void markNotificationAsRead_WithValidIdAndOwnership_ShouldMarkAsRead() {
    // Arrange
    when(notificationRepository.findById(testNotificationId)).thenReturn(
        Optional.of(testNotification));
    when(userService.getCurrentUser()).thenReturn(testUser);
    when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);

    // Act
    notificationService.markNotificationAsRead(testNotificationId);

    // Assert
    assertThat(testNotification.getIsRead()).isTrue();
    verify(notificationRepository).findById(testNotificationId);
    verify(userService).getCurrentUser();
    verify(notificationRepository).save(testNotification);
  }

  @Test
  void markNotificationAsRead_WithNonExistingId_ShouldThrowEntityNotFoundException() {
    // Arrange
    when(notificationRepository.findById(testNotificationId)).thenReturn(Optional.empty());

    // Act & Assert
    assertThatThrownBy(() -> notificationService.markNotificationAsRead(testNotificationId))
        .isInstanceOf(EntityNotFoundException.class)
        .hasMessageContaining("Notification with id " + testNotificationId + " not found");

    verify(notificationRepository).findById(testNotificationId);
    verify(notificationRepository, never()).save(any(Notification.class));
  }

  @Test
  void markNotificationAsRead_WithDifferentOwner_ShouldThrowAccessDeniedException() {
    // Arrange
    User differentUser = User.builder()
        .id(UUID.randomUUID())
        .email("different@example.com")
        .build();

    when(notificationRepository.findById(testNotificationId)).thenReturn(
        Optional.of(testNotification));
    when(userService.getCurrentUser()).thenReturn(differentUser);

    // Act & Assert
    assertThatThrownBy(() -> notificationService.markNotificationAsRead(testNotificationId))
        .isInstanceOf(AccessDeniedException.class)
        .hasMessageContaining("You do not have permission to read this notification");

    verify(notificationRepository).findById(testNotificationId);
    verify(userService).getCurrentUser();
    verify(notificationRepository, never()).save(any(Notification.class));
  }

  @Test
  void markAllNotificationsAsRead_ShouldMarkAllUserNotificationsAsRead() {
    // Arrange
    List<Notification> notifications = new ArrayList<>();
    notifications.add(testNotification);

    Notification secondNotification = Notification.builder()
        .id(UUID.randomUUID())
        .user(testUser)
        .title("Another Notification")
        .message("This is another test notification")
        .type(NotificationType.EVENT)
        .isRead(false)
        .build();
    notifications.add(secondNotification);

    when(userService.getCurrentUser()).thenReturn(testUser);
    when(notificationRepository.findByUser(testUser)).thenReturn(notifications);
    when(notificationRepository.saveAll(notifications)).thenReturn(notifications);

    // Act
    notificationService.markAllNotificationsAsRead();

    // Assert
    assertThat(testNotification.getIsRead()).isTrue();
    assertThat(secondNotification.getIsRead()).isTrue();

    verify(userService).getCurrentUser();
    verify(notificationRepository).findByUser(testUser);
    verify(notificationRepository).saveAll(notifications);
  }

  @Test
  void countUnreadNotifications_ShouldReturnNumberOfUnreadNotifications() {
    // Arrange
    Long unreadCount = 5L;

    when(userService.getCurrentUser()).thenReturn(testUser);
    when(notificationRepository.countByIsReadAndUser(false, testUser)).thenReturn(unreadCount);

    // Act
    Long result = notificationService.countUnreadNotifications();

    // Assert
    assertThat(result).isEqualTo(unreadCount);

    verify(userService).getCurrentUser();
    verify(notificationRepository).countByIsReadAndUser(false, testUser);
  }

  @Test
  void deleteNotification_ShouldDeleteNotification() {
    // Arrange
    doNothing().when(notificationRepository).deleteById(testNotificationId);

    // Act
    notificationService.deleteNotification(testNotificationId);

    // Assert
    verify(notificationRepository).deleteById(testNotificationId);
  }
}