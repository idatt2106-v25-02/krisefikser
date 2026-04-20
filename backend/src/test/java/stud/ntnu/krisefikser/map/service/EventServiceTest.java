package stud.ntnu.krisefikser.map.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.persistence.EntityNotFoundException;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import stud.ntnu.krisefikser.map.dto.EventRequest;
import stud.ntnu.krisefikser.map.dto.UpdateEventRequest;
import stud.ntnu.krisefikser.map.entity.Event;
import stud.ntnu.krisefikser.map.entity.EventLevel;
import stud.ntnu.krisefikser.map.entity.EventStatus;
import stud.ntnu.krisefikser.map.repository.EventRepository;
import stud.ntnu.krisefikser.notification.service.NotificationService;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

  @Mock
  private EventRepository eventRepository;
  @Mock
  private EventWebSocketService eventWebSocketService;
  @Mock
  private NotificationService notificationService;

  @InjectMocks
  private EventService eventService;

  private Event event;

  @BeforeEach
  void setUp() {
    event = Event.builder()
        .id(1L)
        .title("Storm")
        .description("Strong wind")
        .radius(500.0)
        .latitude(63.43)
        .longitude(10.39)
        .level(EventLevel.YELLOW)
        .status(EventStatus.ONGOING)
        .startTime(ZonedDateTime.now())
        .build();
  }

  @Test
  void getAllEvents_shouldReturnMappedResponses() {
    when(eventRepository.findAll()).thenReturn(List.of(event));

    var result = eventService.getAllEvents();

    assertThat(result).hasSize(1);
    assertThat(result.getFirst().getTitle()).isEqualTo("Storm");
  }

  @Test
  void createEvent_shouldSaveAndNotify() {
    EventRequest request = EventRequest.builder()
        .title("Storm")
        .description("Strong wind")
        .radius(500.0)
        .latitude(63.43)
        .longitude(10.39)
        .level(EventLevel.YELLOW)
        .status(EventStatus.ONGOING)
        .startTime(ZonedDateTime.now())
        .build();
    when(eventRepository.save(any(Event.class))).thenReturn(event);

    var result = eventService.createEvent(request);

    assertThat(result.getTitle()).isEqualTo("Storm");
    verify(eventWebSocketService).notifyEventCreation(any());
    verify(notificationService).createNotificationsForAll(any());
  }

  @Test
  void updateEvent_shouldThrowWhenMissing() {
    when(eventRepository.findById(1L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> eventService.updateEvent(1L, UpdateEventRequest.builder().build()))
        .isInstanceOf(EntityNotFoundException.class)
        .hasMessageContaining("Event not found");
  }
}
