package stud.ntnu.krisefikser.map.service;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import stud.ntnu.krisefikser.map.dto.EventResponse;
import stud.ntnu.krisefikser.map.entity.Event;
import stud.ntnu.krisefikser.map.repository.EventRepository;

@Service
@RequiredArgsConstructor
public class EventService {

  private final EventRepository eventRepository;

  public List<EventResponse> getAllEvents() {
    return eventRepository.findAll().stream().map(Event::toResponse).toList();
  }

  public EventResponse getEventById(Long id) {
    Event event = eventRepository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException("Event not found with id: " + id));

    return event.toResponse();
  }

  public EventResponse createEvent(Event event) {
    return eventRepository.save(event).toResponse();
  }

  public EventResponse updateEvent(Long id, Event event) {
    if (!eventRepository.existsById(id)) {
      throw new EntityNotFoundException("Event not found with id: " + id);
    }
    event.setId(id);
    return eventRepository.save(event).toResponse();
  }

  public void deleteEvent(Long id) {
    if (!eventRepository.existsById(id)) {
      throw new EntityNotFoundException("Event not found with id: " + id);
    }
    eventRepository.deleteById(id);
  }
}