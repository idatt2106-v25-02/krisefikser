package stud.ntnu.krisefikser.notification.service;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import stud.ntnu.krisefikser.notification.dto.NotificationResponse;
import stud.ntnu.krisefikser.user.entity.User;

/**
 * Service class for handling real-time notification delivery via WebSocket.
 * Responsible for pushing notifications to specific users' WebSocket queues.
 *
 * @since 1.0
 */
@Service
@RequiredArgsConstructor
public class NotificationWebSocketService {
  private final SimpMessagingTemplate messagingTemplate;

  /**
   * Sends a notification to a specific user's WebSocket queue.
   * The notification is delivered to the user's private "/queue/notifications" destination.
   *
   * @param user                 the recipient user of the notification
   * @param notificationResponse the notification data to be sent
   * @throws org.springframework.messaging.MessagingException if message sending fails
   */
  public void sendNotification(User user, NotificationResponse notificationResponse) {
    messagingTemplate.convertAndSendToUser(
        user.getEmail(),
        "/queue/notifications",
        notificationResponse);
  }
}