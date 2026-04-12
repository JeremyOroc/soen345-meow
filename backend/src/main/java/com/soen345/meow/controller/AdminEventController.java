package com.soen345.meow.controller;

import com.soen345.meow.dto.CreateEventRequest;
import com.soen345.meow.entity.Event;
import com.soen345.meow.entity.Reservation;
import com.soen345.meow.entity.User;
import com.soen345.meow.repository.EventRepository;
import com.soen345.meow.repository.ReservationRepository;
import com.soen345.meow.repository.UserRepository;
import com.soen345.meow.service.NotificationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/events")
public class AdminEventController {

    private final EventRepository eventRepository;
    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public AdminEventController(
            EventRepository eventRepository,
            ReservationRepository reservationRepository,
            UserRepository userRepository,
            NotificationService notificationService
    ) {
        this.eventRepository = eventRepository;
        this.reservationRepository = reservationRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
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
        Event savedEvent = eventRepository.save(event);
        notifyConfirmedReservationHolders(savedEvent);
        return ResponseEntity.ok(savedEvent);
    }

    private void applyRequestToEvent(CreateEventRequest request, Event event) {
        event.setTitle(request.getTitle());
        event.setCategory(request.getCategory());
        event.setLocation(request.getLocation());
        event.setEventDatetime(request.getEventDatetime());
        event.setCapacity(request.getCapacity());
    }

    private void notifyConfirmedReservationHolders(Event event) {
        List<Reservation> reservations = reservationRepository.findByEventIdAndStatus(event.getId(), "CONFIRMED");
        for (Reservation reservation : reservations) {
            userRepository.findById(reservation.getUserId())
                    .map(User::getEmail)
                    .filter(email -> email != null && !email.isBlank())
                    .ifPresent(email -> notificationService.sendEventCancellationNotice(email, event.getTitle()));
        }
    }
}
