package stud.ntnu.krisefikser.map.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import stud.ntnu.krisefikser.map.entity.Event;
import stud.ntnu.krisefikser.map.entity.EventLevel;
import stud.ntnu.krisefikser.map.entity.EventStatus;

@ExtendWith(MockitoExtension.class)
public class EventWebSocketServiceTest {

  @Mock
  private SimpMessagingTemplate messagingTemplate;

  @InjectMocks
  private EventWebSocketService webSocketService;

  private Event testEvent;

  @BeforeEach
  public void setup() {
    testEvent =
        Event.builder().id(1L).title("Test Event").description("Test Description").radius(1000.0)
            .latitude(63.4305).longitude(10.3951).level(EventLevel.YELLOW)
            .startTime(LocalDateTime.now()).endTime(LocalDateTime.now().plusHours(2))
            .status(EventStatus.ONGOING).build();
  }

  @Test
  public void testNotifyEventUpdate() {
    webSocketService.notifyEventUpdate(testEvent);
    verify(messagingTemplate).convertAndSend(eq("/topic/events"), any(Event.class));
  }

  @Test
  public void testNotifyEventCreation() {
    webSocketService.notifyEventCreation(testEvent);
    verify(messagingTemplate).convertAndSend(eq("/topic/events/new"), any(Event.class));
  }

  @Test


  public void testNotifyEventDeletion() {
    webSocketService.notifyEventDeletion(1L);
    verify(messagingTemplate).convertAndSend(eq("/topic/events/delete"), eq(1L));
  }
}