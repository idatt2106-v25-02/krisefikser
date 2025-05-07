package stud.ntnu.krisefikser.notification.service;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import stud.ntnu.krisefikser.household.service.HouseholdService;
import stud.ntnu.krisefikser.notification.dto.NotificationResponse;
import stud.ntnu.krisefikser.notification.entity.Notification;
import stud.ntnu.krisefikser.notification.repository.NotificationRepository;
import stud.ntnu.krisefikser.user.entity.User;
import stud.ntnu.krisefikser.user.service.UserService;

/**
 * Service class that handles business logic related to Notification entities.
 * Provides comprehensive
 * notification management capabilities including creation, retrieval, distribution,
 * and status tracking of notifications.
 *
 * <p>This service acts as an intermediary between the controllers and the data access layer,
 * adding business validation and integrating with the WebSocket service for real-time notification
 * delivery to users. It supports targeted notifications for individual users as well as bulk
 * notification creation for user groups like administrators and household owners.</p>
 *
 * @author NTNU Krisefikser Team
 * @see Notification
 * @see NotificationRepository
 * @see NotificationWebSocketService
 */
@Service
@RequiredArgsConstructor
public class NotificationService {
  /**
   * Repository for Notification entity operations.
   * Automatically injected through constructor by Lombok's
   * {@code @RequiredArgsConstructor}.
   */
  private final NotificationRepository notificationRepository;

  /**
   * WebSocket service for distributing notifications to users in real-time. Automatically injected
   * through constructor by Lombok's {@code @RequiredArgsConstructor}.
   */
  private final NotificationWebSocketService notificationWebSocketService;

  /**
   * Service for retrieving user information and authentication details. Automatically injected
   * through constructor by Lombok's {@code @RequiredArgsConstructor}.
   */
  private final UserService userService;

  /**
   * Service for retrieving household information and ownership details. Automatically injected
   * through constructor by Lombok's {@code @RequiredArgsConstructor}.
   */
  private final HouseholdService householdService;

  /**
   * Creates and saves a new notification, then sends it to the recipient via WebSocket.
   * This method persists the notification to the database and immediately delivers it
   * to the intended user through the WebSocket service.
   *
   * @param notification The notification entity to be created and delivered
   * @return NotificationResponse containing the saved notification data
   */
  public NotificationResponse createNotification(Notification notification) {
    NotificationResponse savedNotification = notificationRepository.save(notification).toResponse();
    notificationWebSocketService.sendNotification(notification.getUser(), savedNotification);
    return savedNotification;
  }

  /**
   * Creates and distributes a copy of the provided notification to all users in the system.
   * This method iterates through all registered users and creates individual notifications
   * for each one.
   *
   * @param notification The template notification to be duplicated for all users
   */
  public void createNotificationsForAll(Notification notification) {
    for (User user : userService.getAllUsers()) {
      createNotification(new Notification(notification, user));
    }
  }

  /**
   * Creates and distributes a copy of the provided notification to all administrator users.
   * This method specifically targets users with administrative privileges.
   *
   * @param notification The template notification to be duplicated for all admin users
   */
  public void createNotificationsForAdmin(Notification notification) {
    for (User user : userService.getAllAdmins()) {
      createNotification(new Notification(notification, user));
    }
  }

  /**
   * Creates and distributes a copy of the provided notification to all users who own households.
   * This method specifically targets users based on their household ownership status.
   *
   * @param notification The template notification to be duplicated for all household owners
   */
  public void createNotificationsForHouseholdOwners(Notification notification) {
    for (User user : householdService.getHouseholdOwners()) {
      createNotification(new Notification(notification, user));
    }
  }

  /**
   * Deletes a notification from the system by its unique identifier.
   *
   * @param notificationId The unique identifier of the notification to be deleted
   */
  public void deleteNotification(UUID notificationId) {
    notificationRepository.deleteById(notificationId);
  }

  /**
   * Retrieves a paginated list of notifications for the currently authenticated user.
   * This method supports pagination for efficient retrieval of large notification sets.
   *
   * @param pageable Pagination information (page number, size, and sorting)
   * @return Page of NotificationResponse objects for the current user
   */
  public Page<NotificationResponse> getNotifications(Pageable pageable) {
    User currentUser = userService.getCurrentUser();
    return notificationRepository.findByUser(currentUser, pageable).map(Notification::toResponse);
  }

  /**
   * Retrieves all notifications for the currently authenticated user.
   * This method returns the complete set of notifications without pagination.
   *
   * @return A List of NotificationResponse objects, representing all the user's notifications
   */
  public List<NotificationResponse> getNotifications() {
    User currentUser = userService.getCurrentUser();
    return notificationRepository.findByUser(currentUser).stream()
        .map(Notification::toResponse)
        .toList();
  }

  /**
   * Marks a specific notification as read for the currently authenticated user.
   * This method enforces access control by verifying the current user owns the notification.
   *
   * @param notificationId The unique identifier of the notification to be marked as read
   * @throws EntityNotFoundException If no notification with the specified ID exists
   * @throws AccessDeniedException   If the current user does not own the notification
   */
  public void markNotificationAsRead(UUID notificationId) {
    Notification notification = notificationRepository.findById(notificationId).orElseThrow(
        () -> new EntityNotFoundException(
            "Notification with id " + notificationId + " not found"));
    if (!notification.getUser().getId().equals(userService.getCurrentUser().getId())) {
      throw new AccessDeniedException("You do not have permission to read this notification");
    }
    notification.setIsRead(true);
    notificationRepository.save(notification);
  }

  /**
   * Marks all notifications for the current user as read.
   * This method performs a bulk update of read status for all notifications belonging
   * to the currently authenticated user.
   */
  public void markAllNotificationsAsRead() {
    List<Notification> notificationsToUpdate =
        notificationRepository.findByUser(userService.getCurrentUser());
    notificationsToUpdate.forEach(notification -> notification.setIsRead(true));
    notificationRepository.saveAll(notificationsToUpdate);
  }

  /**
   * Counts the number of unread notifications for the current user.
   * This method provides an efficient way to determine notification badge counts.
   *
   * @return The number of unread notifications for the current user
   */
  public Long countUnreadNotifications() {
    return notificationRepository.countByIsReadAndUser(false, userService.getCurrentUser());
  }
}