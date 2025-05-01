package stud.ntnu.krisefikser.map.service;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import stud.ntnu.krisefikser.map.entity.Event;

@Service
@RequiredArgsConstructor
public class EventWebSocketService {

  private final SimpMessagingTemplate messagingTemplate;

  public void notifyEventUpdate(Event event) {
    messagingTemplate.convertAndSend("/topic/events", event);
  }

  public void notifyEventCreation(Event event) {
    messagingTemplate.convertAndSend("/topic/events/new", event);
  }

  public void notifyEventDeletion(Long eventId) {
    messagingTemplate.convertAndSend("/topic/events/delete", eventId);
  }
}