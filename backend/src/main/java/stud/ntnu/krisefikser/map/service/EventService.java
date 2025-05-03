package stud.ntnu.krisefikser.map.service;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import stud.ntnu.krisefikser.map.entity.Event;
import stud.ntnu.krisefikser.map.repository.EventRepository;

/**
 * Service class that handles business logic related to Event entities.
 * Provides CRUD operations for events and integrates with WebSocket notifications
 * to broadcast changes to connected clients in real-time.
 *
 * <p>This service acts as an intermediary between the controllers and the data access layer,
 * adding business validation and integrating with the WebSocket service for real-time updates.</p>
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
   * Automatically injected through constructor by Lombok's {@code @RequiredArgsConstructor}.
   */
  private final EventRepository eventRepository;

  /**
   * WebSocket service for broadcasting event changes to connected clients.
   * Automatically injected through constructor by Lombok's {@code @RequiredArgsConstructor}.
   */
  private final EventWebSocketService eventWebSocketService;

  /**
   * Retrieves all events from the database.
   *
   * @return A list containing all events in the database
   */
  public List<Event> getAllEvents() {
    return eventRepository.findAll();
  }

  /**
   * Retrieves a specific event by its ID.
   *
   * @param id The ID of the event to retrieve
   * @return The requested event
   * @throws EntityNotFoundException If no event with the specified ID exists
   */
  public Event getEventById(Long id) {
    return eventRepository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException("Event not found with id: " + id));
  }

  /**
   * Creates a new event in the database and notifies connected clients.
   *
   * <p>This method broadcasts a WebSocket notification about the new event
   * before saving it to the database.</p>
   *
   * @param event The event entity to be created
   * @return The created event with assigned ID
   */
  public Event createEvent(Event event) {
    eventWebSocketService.notifyEventCreation(event);
    return eventRepository.save(event);
  }

  /**
   * Updates an existing event in the database and notifies connected clients.
   *
   * <p>This method checks if the event exists before updating, broadcasts
   * a WebSocket notification about the update, and then saves the changes.</p>
   *
   * @param id    The ID of the event to update
   * @param event The updated event data
   * @return The updated event entity
   * @throws EntityNotFoundException If no event with the specified ID exists
   */
  public Event updateEvent(Long id, Event event) {
    if (!eventRepository.existsById(id)) {
      throw new EntityNotFoundException("Event not found with id: " + id);
    }
    eventWebSocketService.notifyEventUpdate(event);
    return eventRepository.save(event);
  }

  /**
   * Deletes an event from the database and notifies connected clients.
   *
   * <p>This method verifies the event exists before deleting it, broadcasts
   * a WebSocket notification about the deletion, and then removes it from the database.</p>
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