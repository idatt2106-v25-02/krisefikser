package stud.ntnu.krisefikser.map.service;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import stud.ntnu.krisefikser.map.dto.EventResponse;

/**
 * Service responsible for sending real-time WebSocket notifications related to events. This class
 * handles broadcasting updates, creations, and deletions of events to subscribed clients using
 * Spring's WebSocket messaging capabilities.
 *
 * <p>The service uses predefined topic destinations that clients can subscribe to in order
 * to receive notifications about specific event actions:</p>
 * <ul>
 *   <li>{@code /topic/events} - For general event updates</li>
 *   <li>{@code /topic/events/new} - For new event creations</li>
 *   <li>{@code /topic/events/delete} - For event deletions</li>
 * </ul>
 *
 * <p>This service uses {@link SimpMessagingTemplate} to send messages to these WebSocket
 * destinations.</p>
 *
 * @author Jakob Huuse
 */
@Service
@RequiredArgsConstructor
public class EventWebSocketService {

  /**
   * Spring's messaging template used to send messages to WebSocket destinations. Automatically
   * injected through constructor by Lombok's {@code @RequiredArgsConstructor}.
   */
  private final SimpMessagingTemplate messagingTemplate;

  /**
   * Broadcasts an update notification for an existing event. Sends the updated event object to
   * clients subscribed to the "/topic/events" destination.
   *
   * @param event The updated event object to broadcast to clients
   */
  public void notifyEventUpdate(EventResponse event) {
    messagingTemplate.convertAndSend("/topic/events", event);
  }

  /**
   * Broadcasts a creation notification for a new event. Sends the newly created event object to
   * clients subscribed to the "/topic/events/new" destination.
   *
   * @param event The newly created event object to broadcast to clients
   */
  public void notifyEventCreation(EventResponse event) {
    messagingTemplate.convertAndSend("/topic/events/new", event);
  }

  /**
   * Broadcasts a deletion notification for an event. Sends the ID of the deleted event to clients
   * subscribed to the "/topic/events/delete" destination.
   *
   * @param eventId The ID of the deleted event to broadcast to clients
   */
  public void notifyEventDeletion(Long eventId) {
    messagingTemplate.convertAndSend("/topic/events/delete", eventId);
  }
}