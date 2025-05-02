package stud.ntnu.krisefikser.map.service;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import stud.ntnu.krisefikser.map.dto.EventRequest;
import stud.ntnu.krisefikser.map.dto.EventResponse;
import stud.ntnu.krisefikser.map.dto.UpdateEventRequest;
import stud.ntnu.krisefikser.map.entity.Event;
import stud.ntnu.krisefikser.map.entity.EventStatus;
import stud.ntnu.krisefikser.map.repository.EventRepository;

/**
 * Service class that handles business logic related to Event entities.
 * Provides CRUD operations for events and integrates with WebSocket
 * notifications
 * to broadcast changes to connected clients in real-time.
 *
 * <p>
 * This service acts as an intermediary between the controllers and the data
 * access layer,
 * adding business validation and integrating with the WebSocket service for
 * real-time updates.
 * </p>
 *
 * @author NTNU Krisefikser Team
 * @see Event
 * @see EventRepository
 * @see EventWebSocketService
 */
@Service
@RequiredArgsConstructor
public class EventService {

  /**
   * Repository for Event entity operations.
   * Automatically injected through constructor by Lombok's
   * {@code @RequiredArgsConstructor}.
   */
  private final EventRepository eventRepository;

  /**
   * WebSocket service for broadcasting event changes to connected clients.
   * Automatically injected through constructor by Lombok's
   * {@code @RequiredArgsConstructor}.
   */
  private final EventWebSocketService eventWebSocketService;

  /**
   * Retrieves all events from the database.
   *
   * @return A list containing all events in the database
   */
  public List<EventResponse> getAllEvents() {
    return eventRepository.findAll().stream().map(Event::toResponse).toList();
  }

  public EventResponse getEventById(Long id) {
    Event event = eventRepository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException("Event not found with id: " + id));

    return event.toResponse();
  }

  /**
   * Creates a new event in the database and notifies connected clients.
   *
   * <p>
   * This method broadcasts a WebSocket notification about the new event
   * before saving it to the database.
   * </p>
   *
   * @param eventRequest The event entity to be created
   * @return The created event with assigned ID
   */
  public EventResponse createEvent(EventRequest eventRequest) {
    Event event = Event.builder()
        .title(eventRequest.getTitle())
        .description(eventRequest.getDescription())
        .radius(eventRequest.getRadius())
        .latitude(eventRequest.getLatitude())
        .longitude(eventRequest.getLongitude())
        .level(eventRequest.getLevel())
        .startTime(eventRequest.getStartTime())
        .endTime(eventRequest.getEndTime())
        .status(eventRequest.getStatus())
        .build();

    return eventRepository.save(event).toResponse();
  }

  /**
   * Updates an existing event in the database and notifies connected clients.
   *
   * <p>
   * This method checks if the event exists before updating, broadcasts
   * a WebSocket notification about the update, and then saves the changes.
   * </p>
   *
   * @param id    The ID of the event to update
   * @param eventRequest The updated event data
   * @return The updated event entity
   * @throws EntityNotFoundException If no event with the specified ID exists
   */
  public EventResponse updateEvent(Long id, UpdateEventRequest eventRequest) {
    Event existingEvent = eventRepository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException("Event not found with id: " + id));

    if (eventRequest.getTitle() != null) {
      existingEvent.setTitle(eventRequest.getTitle());
    }

    if (eventRequest.getDescription() != null) {
      existingEvent.setDescription(eventRequest.getDescription());
    }

    if (eventRequest.getRadius() != null) {
      existingEvent.setRadius(eventRequest.getRadius());
    }

    if (eventRequest.getLatitude() != null) {
      existingEvent.setLatitude(eventRequest.getLatitude());
    }

    if (eventRequest.getLongitude() != null) {
      existingEvent.setLongitude(eventRequest.getLongitude());
    }

    if (eventRequest.getLevel() != null) {
      existingEvent.setLevel(eventRequest.getLevel());
    }

    if (eventRequest.getStartTime() != null) {
      existingEvent.setStartTime(eventRequest.getStartTime());
    }

    if (eventRequest.getEndTime() != null) {
      existingEvent.setEndTime(eventRequest.getEndTime());
    }

    if (eventRequest.getStatus() != null) {
      existingEvent.setStatus(eventRequest.getStatus());
    }

    return eventRepository.save(existingEvent).toResponse();
  }

  /**
   * Deletes an event from the database and notifies connected clients.
   *
   * <p>
   * This method verifies the event exists before deleting it, broadcasts
   * a WebSocket notification about the deletion, and then removes it from the
   * database.
   * </p>
   *
   * @param id The ID of the event to delete
   * @throws EntityNotFoundException If no event with the specified ID exists
   */
  public void deleteEvent(Long id) {
    if (!eventRepository.existsById(id)) {
      throw new EntityNotFoundException("Event not found with id: " + id);
    }

    eventWebSocketService.notifyEventDeletion(id);
    eventRepository.deleteById(id);
  }
}