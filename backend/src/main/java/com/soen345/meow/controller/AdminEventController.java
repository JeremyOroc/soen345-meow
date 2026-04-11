package com.soen345.meow.controller;

import com.soen345.meow.dto.CreateEventRequest;
import com.soen345.meow.entity.Event;
import com.soen345.meow.repository.EventRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/admin/events")
public class AdminEventController {

    private final EventRepository eventRepository;

    public AdminEventController(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Event> createEvent(@RequestBody CreateEventRequest request) {
        Event event = new Event();
        applyRequestToEvent(request, event);
        event.setStatus("ACTIVE");
        event.setAvailableSeats(request.getCapacity());
        return ResponseEntity.status(HttpStatus.CREATED).body(eventRepository.save(event));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Event> editEvent(@PathVariable Integer id, @RequestBody CreateEventRequest request) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Event not found"));

        applyRequestToEvent(request, event);
        event.setAvailableSeats(request.getCapacity());
        return ResponseEntity.ok(eventRepository.save(event));
    }

    @DeleteMapping("/{id}/cancel")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Event> cancelEvent(@PathVariable Integer id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Event not found"));

        event.setStatus("CANCELLED");
        return ResponseEntity.ok(eventRepository.save(event));
    }

    private void applyRequestToEvent(CreateEventRequest request, Event event) {
        event.setTitle(request.getTitle());
        event.setCategory(request.getCategory());
        event.setLocation(request.getLocation());
        event.setEventDatetime(request.getEventDatetime());
        event.setCapacity(request.getCapacity());
    }
}
