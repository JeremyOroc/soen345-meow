package com.soen345.meow;

import com.soen345.meow.controller.AdminEventController;
import com.soen345.meow.entity.Event;
import com.soen345.meow.entity.Reservation;
import com.soen345.meow.entity.User;
import com.soen345.meow.repository.EventRepository;
import com.soen345.meow.repository.ReservationRepository;
import com.soen345.meow.repository.UserRepository;
import com.soen345.meow.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminEventControllerNotificationTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private AdminEventController adminEventController;

    @Test
    void shouldNotifyConfirmedReservationHoldersWhenEventIsCancelled() {
        Event event = new Event();
        event.setId(1);
        event.setTitle("Jazz Night");
        event.setStatus("ACTIVE");

        Reservation reservation = new Reservation();
        reservation.setId(10L);
        reservation.setUserId(5L);
        reservation.setEventId(1);
        reservation.setStatus("CONFIRMED");

        User user = new User("customer@example.com", null, "hash", "CUSTOMER");

        when(eventRepository.findById(1)).thenReturn(Optional.of(event));
        when(reservationRepository.findByEventIdAndStatus(1, "CONFIRMED")).thenReturn(List.of(reservation));
        when(userRepository.findById(5L)).thenReturn(Optional.of(user));
        when(eventRepository.save(event)).thenReturn(event);

        adminEventController.cancelEvent(1);

        verify(notificationService).sendEventCancellationNotice("customer@example.com", "Jazz Night");
    }

    @Test
    void shouldSkipCancellationEmailWhenUserHasNoEmail() {
        Event event = new Event();
        event.setId(1);
        event.setTitle("Jazz Night");
        event.setStatus("ACTIVE");

        Reservation reservation = new Reservation();
        reservation.setId(10L);
        reservation.setUserId(5L);
        reservation.setEventId(1);
        reservation.setStatus("CONFIRMED");

        User user = new User(null, "5141231234", "hash", "CUSTOMER");

        when(eventRepository.findById(1)).thenReturn(Optional.of(event));
        when(reservationRepository.findByEventIdAndStatus(1, "CONFIRMED")).thenReturn(List.of(reservation));
        when(userRepository.findById(5L)).thenReturn(Optional.of(user));
        when(eventRepository.save(event)).thenReturn(event);

        adminEventController.cancelEvent(1);

        verify(notificationService, never()).sendEventCancellationNotice(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString());
    }
}
