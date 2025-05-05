package stud.ntnu.krisefikser.notification.service;

import jakarta.persistence.EntityNotFoundException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import stud.ntnu.krisefikser.notification.dto.NotificationResponse;
import stud.ntnu.krisefikser.notification.entity.Notification;
import stud.ntnu.krisefikser.notification.repository.NotificationRepository;
import stud.ntnu.krisefikser.user.entity.User;
import stud.ntnu.krisefikser.user.service.UserService;

/**
 * Service class for managing notifications. Handles creation, retrieval,
 * and real-time distribution of notifications to users.
 *
 * @since 1.0
 */
@Service
@RequiredArgsConstructor
public class NotificationService {
  private final NotificationRepository notificationRepository;
  private final NotificationWebSocketService notificationWebSocketService;
  private final UserService userService;

  /**
   * Creates and saves a new notification, then sends it to the recipient via WebSocket.
   *
   * @param notification the notification entity to be created
   * @return NotificationResponse containing the saved notification data
   */
  public NotificationResponse createNotification(Notification notification) {
    NotificationResponse savedNotification = notificationRepository.save(notification).toResponse();
    notificationWebSocketService.sendNotification(notification.getUser(), savedNotification);
    return savedNotification;
  }

  /**
   * Deletes a notification.
   *
   * @param notificationId the notification ID of the entity to be deleted
   */
  public void deleteNotification(UUID notificationId) {
    notificationRepository.deleteById(notificationId);
  }

  /**
   * Retrieves a paginated list of notifications for the currently authenticated user.
   *
   * @param pageable pagination information (page number, size, and sorting)
   * @return Page of NotificationResponse objects for the current user
   */
  public Page<NotificationResponse> getNotifications(Pageable pageable) {
    User currentUser = userService.getCurrentUser();
    return notificationRepository.findByUser(currentUser, pageable).map(Notification::toResponse);
  }


  /**
   * Marks a specific notification as read for the currently authenticated user.
   * Throws an EntityNotFoundException if the notification does not exist.
   * Throws an AccessDeniedException if the current user does not own the notification.
   *
   * @param notificationId the unique identifier of the notification to be marked as read
   */
  public void markNotificationAsRead(UUID notificationId) {
    Notification notification = notificationRepository.findById(notificationId).orElseThrow(
        () -> new EntityNotFoundException(
            "Notification with id " + notificationId + " not found"));
    if (!notification.getUser().getId().equals(userService.getCurrentUser().getId())) {
      throw new AccessDeniedException("You do not have permission to read this notification");
    }
    notification.setRead(true);
    notificationRepository.save(notification);
  }

  /**
   * Marks all notifications for the current user as read.
   */
  public void markAllNotificationsAsRead() {
    notificationRepository.findByUser(userService.getCurrentUser()).forEach(notification -> {
      notification.setRead(true);
    });
  }

  /**
   * Counts the number of unread notifications for the current user.
   *
   * @return the number of unread notifications for the current user.
   */
  public Long countUnreadNotifications() {
    return notificationRepository.countByReadAndUser(false, userService.getCurrentUser());
  }
}