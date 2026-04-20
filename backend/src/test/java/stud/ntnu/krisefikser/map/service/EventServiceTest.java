package stud.ntnu.krisefikser.map.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
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
@SuppressWarnings("null")
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
    when(eventRepository.save(org.mockito.ArgumentMatchers.<Event>any())).thenReturn(event);

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

  @Test
  void getEventById_shouldReturnMappedEvent() {
    when(eventRepository.findById(1L)).thenReturn(Optional.of(event));

    var result = eventService.getEventById(1L);

    assertThat(result.getId()).isEqualTo(1L);
    assertThat(result.getTitle()).isEqualTo("Storm");
  }

  @Test
  void getEventEntityById_shouldThrowWhenMissing() {
    when(eventRepository.findById(999L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> eventService.getEventEntityById(999L))
        .isInstanceOf(EntityNotFoundException.class)
        .hasMessageContaining("Event not found with id: 999");
  }

  @Test
  void updateEvent_shouldUpdateFieldsAndNotify() {
    UpdateEventRequest request = UpdateEventRequest.builder()
        .title("Updated storm")
        .description("Updated description")
        .status(EventStatus.FINISHED)
        .build();

    when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
    when(eventRepository.save(org.mockito.ArgumentMatchers.<Event>any()))
        .thenAnswer(invocation -> invocation.getArgument(0));

    var result = eventService.updateEvent(1L, request);

    assertThat(result.getTitle()).isEqualTo("Updated storm");
    assertThat(result.getDescription()).isEqualTo("Updated description");
    assertThat(result.getStatus()).isEqualTo(EventStatus.FINISHED);
    verify(eventWebSocketService).notifyEventUpdate(any());
    verify(notificationService).createNotificationsForAll(any());
  }

  @Test
  void deleteEvent_shouldDeleteAndNotifyWhenExists() {
    when(eventRepository.existsById(1L)).thenReturn(true);

    eventService.deleteEvent(1L);

    verify(eventWebSocketService).notifyEventDeletion(1L);
    verify(eventRepository).deleteById(1L);
  }

  @Test
  void deleteEvent_shouldThrowAndNotNotifyWhenMissing() {
    when(eventRepository.existsById(123L)).thenReturn(false);

    assertThatThrownBy(() -> eventService.deleteEvent(123L))
        .isInstanceOf(EntityNotFoundException.class)
        .hasMessageContaining("Event not found with id: 123");
    verify(eventWebSocketService, never()).notifyEventDeletion(anyLong());
    verify(eventRepository, never()).deleteById(anyLong());
  }
}
