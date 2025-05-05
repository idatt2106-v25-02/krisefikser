package stud.ntnu.krisefikser.notification.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
   * Retrieves a paginated list of notifications for the currently authenticated user.
   *
   * @param pageable pagination information (page number, size, and sorting)
   * @return Page of NotificationResponse objects for the current user
   */
  public Page<NotificationResponse> getNotifications(Pageable pageable) {
    User currentUser = userService.getCurrentUser();
    return notificationRepository.findByUser(currentUser, pageable);
  }
}