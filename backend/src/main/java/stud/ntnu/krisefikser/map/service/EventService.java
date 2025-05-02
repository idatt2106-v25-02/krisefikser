package stud.ntnu.krisefikser.map.service;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import stud.ntnu.krisefikser.map.dto.EventRequest;
import stud.ntnu.krisefikser.map.dto.EventResponse;
import stud.ntnu.krisefikser.map.dto.UpdateEventRequest;
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

  public void deleteEvent(Long id) {
    if (!eventRepository.existsById(id)) {
      throw new EntityNotFoundException("Event not found with id: " + id);
    }
    eventRepository.deleteById(id);
  }
}