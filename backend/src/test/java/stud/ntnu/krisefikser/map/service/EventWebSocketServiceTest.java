package stud.ntnu.krisefikser.map.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import java.time.ZonedDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import stud.ntnu.krisefikser.map.dto.EventResponse;
import stud.ntnu.krisefikser.map.entity.EventLevel;
import stud.ntnu.krisefikser.map.entity.EventStatus;

@ExtendWith(MockitoExtension.class)
class EventWebSocketServiceTest {

  @Mock
  private SimpMessagingTemplate messagingTemplate;

  @InjectMocks
  private EventWebSocketService webSocketService;

  private EventResponse testEventResponse;
  private ZonedDateTime now;

  @BeforeEach
  void setUp() {
    now = ZonedDateTime.now();

    testEventResponse = EventResponse.builder()
        .id(1L)
        .title("Test Event")
        .description("Test Description")
        .radius(1000.0)
        .latitude(63.4305)
        .longitude(10.3951)
        .level(EventLevel.YELLOW)
        .startTime(now)
        .endTime(now.plusHours(2))
        .status(EventStatus.ONGOING)
        .build();
  }

  @Test
  void notifyEventUpdate_ShouldSendEventToTopic() {
    // Act
    webSocketService.notifyEventUpdate(testEventResponse);

    // Assert
    verify(messagingTemplate).convertAndSend(eq("/topic/events"), any(EventResponse.class));
  }

  @Test
  void notifyEventCreation_ShouldSendEventToNewTopic() {
    // Act
    webSocketService.notifyEventCreation(testEventResponse);

    // Assert
    verify(messagingTemplate).convertAndSend(eq("/topic/events/new"), any(EventResponse.class));
  }

  @Test
  void notifyEventDeletion_ShouldSendEventIdToDeleteTopic() {
    // Act
    webSocketService.notifyEventDeletion(1L);

    // Assert
    verify(messagingTemplate).convertAndSend(eq("/topic/events/delete"), eq(1L));
  }
}