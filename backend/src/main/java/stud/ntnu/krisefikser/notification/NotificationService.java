package stud.ntnu.krisefikser.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import stud.ntnu.krisefikser.map.entity.Event;

/**
 * Service responsible for sending real-time WebSocket notifications.
 * This class handles broadcasting updates, creations, and deletions of events to subscribed clients
 * using Spring's WebSocket messaging capabilities.
 *
 * <p>The service uses predefined topic destinations that clients can subscribe to in order
 * to receive notifications about specific event actions:</p>
 * <ul>
 *   <li>{@code /topic/events} - For general event updates</li>
 *   <li>{@code /topic/events/new} - For new event creations</li>
 *   <li>{@code /topic/events/delete} - For event deletions</li>
 * </ul>
 *
 * <p>This service uses {@link SimpMessagingTemplate} to send messages to these WebSocket destinations.</p>
 *
 * @author Jakob Huuse
 */
@Service
@RequiredArgsConstructor
public class NotificationService {

  /**
   * Spring's messaging template used to send messages to WebSocket destinations.
   * Automatically injected through constructor by Lombok's {@code @RequiredArgsConstructor}.
   */
  private final SimpMessagingTemplate messagingTemplate;

  public void broadcastNotification(NotificationResponse notification) {
    messagingTemplate.convertAndSend("/topic/broadcast", notification);
  }
}