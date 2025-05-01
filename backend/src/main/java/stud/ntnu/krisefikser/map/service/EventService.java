package stud.ntnu.krisefikser.map.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import stud.ntnu.krisefikser.map.entity.Event;
import stud.ntnu.krisefikser.map.repository.EventRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EventService {
  private final EventRepository eventRepository;
  private final EventWebSocketService eventWebSocketService;

  public List<Event> getAllEvents() {
    return eventRepository.findAll();
  }

  public Event getEventById(Long id) {
    return eventRepository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException("Event not found with id: " + id));
  }

  public Event createEvent(Event event) {
    eventWebSocketService.notifyEventCreation(event);
    return eventRepository.save(event);
  }

  public Event updateEvent(Long id, Event event) {
    if (!eventRepository.existsById(id)) {
      throw new EntityNotFoundException("Event not found with id: " + id);
    }
    eventWebSocketService.notifyEventUpdate(event);
    return eventRepository.save(event);
  }

  public void deleteEvent(Long id) {
    if (!eventRepository.existsById(id)) {
      throw new EntityNotFoundException("Event not found with id: " + id);
    }
    eventWebSocketService.notifyEventDeletion(id);
    eventRepository.deleteById(id);
  }
}