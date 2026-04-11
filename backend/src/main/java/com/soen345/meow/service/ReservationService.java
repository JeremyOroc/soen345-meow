package com.soen345.meow.service;

import com.soen345.meow.entity.Event;
import com.soen345.meow.entity.Reservation;
import com.soen345.meow.entity.User;
import com.soen345.meow.repository.EventRepository;
import com.soen345.meow.repository.ReservationRepository;
import com.soen345.meow.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public ReservationService(
            ReservationRepository reservationRepository,
            EventRepository eventRepository,
            UserRepository userRepository,
            NotificationService notificationService
    ) {
        this.reservationRepository = reservationRepository;
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    public Reservation createReservation(Long userId, Integer eventId, int qty) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID is required");
        }
        if (eventId == null) {
            throw new IllegalArgumentException("Event ID is required");
        }
        if (qty <= 0) {
            throw new IllegalArgumentException("Quantity must be at least 1");
        }

        Event event = eventRepository.findByIdForUpdate(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found"));

        if (event.getAvailableSeats() == null || event.getAvailableSeats() < qty) {
            throw new IllegalStateException("Insufficient seats");
        }

        event.setAvailableSeats(event.getAvailableSeats() - qty);
        eventRepository.save(event);

        Reservation reservation = new Reservation();
        reservation.setUserId(userId);
        reservation.setEventId(eventId);
        reservation.setTicketQuantity(qty);
        reservation.setStatus("CONFIRMED");
        reservation.setReservedAt(LocalDateTime.now());

        Reservation savedReservation = reservationRepository.save(reservation);
        sendConfirmationEmail(userId, event, savedReservation);
        return savedReservation;
    }

    @Transactional
    public Reservation cancelReservation(Long userId, Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found"));

        if (!reservation.getUserId().equals(userId)) {
            throw new AccessDeniedException("You do not own this reservation");
        }

        if ("CANCELLED".equals(reservation.getStatus())) {
            return reservation;
        }

        Event event = eventRepository.findByIdForUpdate(reservation.getEventId())
                .orElseThrow(() -> new IllegalArgumentException("Event not found"));

        int seats = event.getAvailableSeats() == null ? 0 : event.getAvailableSeats();
        event.setAvailableSeats(seats + reservation.getTicketQuantity());
        eventRepository.save(event);

        reservation.setStatus("CANCELLED");
        reservation.setCancelledAt(LocalDateTime.now());
        return reservationRepository.save(reservation);
    }

    public List<Reservation> getReservationsForUser(Long userId) {
        return reservationRepository.findByUserId(userId);
    }

    private void sendConfirmationEmail(Long userId, Event event, Reservation reservation) {
        userRepository.findById(userId)
                .map(User::getEmail)
                .filter(email -> email != null && !email.isBlank())
                .ifPresent(email -> notificationService.sendBookingConfirmation(
                        email,
                        event.getTitle(),
                        reservation.getId(),
                        parseEventDate(event.getEventDatetime())
                ));
    }

    private LocalDateTime parseEventDate(String eventDatetime) {
        if (eventDatetime == null || eventDatetime.isBlank()) {
            return null;
        }
        try {
            return LocalDateTime.parse(eventDatetime);
        } catch (Exception ex) {
            return null;
        }
    }
}