package com.soen345.meow;

import com.soen345.meow.entity.Event;
import com.soen345.meow.entity.Reservation;
import com.soen345.meow.repository.EventRepository;
import com.soen345.meow.repository.ReservationRepository;
import com.soen345.meow.service.ReservationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private ReservationService reservationService;

    private Event event;

    @BeforeEach
    void setUp() {
        event = new Event();
        event.setId(1);
        event.setAvailableSeats(10);
        event.setStatus("ACTIVE");
    }

    @Test
    void shouldCreateReservationSuccessfully() {
        when(eventRepository.findByIdForUpdate(1)).thenReturn(Optional.of(event));
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(invocation -> {
            Reservation reservation = invocation.getArgument(0);
            reservation.setId(99L);
            return reservation;
        });

        Reservation reservation = reservationService.createReservation(7L, 1, 2);

        assertThat(reservation.getStatus()).isEqualTo("CONFIRMED");
        assertThat(reservation.getUserId()).isEqualTo(7L);
        assertThat(reservation.getEventId()).isEqualTo(1);
        assertThat(reservation.getTicketQuantity()).isEqualTo(2);
        assertThat(event.getAvailableSeats()).isEqualTo(8);

        verify(eventRepository).save(event);
        verify(reservationRepository).save(any(Reservation.class));
    }

    @Test
    void shouldFailWhenInsufficientSeats() {
        event.setAvailableSeats(1);
        when(eventRepository.findByIdForUpdate(1)).thenReturn(Optional.of(event));

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> reservationService.createReservation(7L, 1, 2));

        assertThat(exception.getMessage()).isEqualTo("Insufficient seats");
        verify(eventRepository, never()).save(any(Event.class));
        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    @Test
    void shouldFailWhenEventDoesNotExist() {
        when(eventRepository.findByIdForUpdate(999)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> reservationService.createReservation(7L, 999, 1));

        assertThat(exception.getMessage()).isEqualTo("Event not found");
        verify(eventRepository, never()).save(any(Event.class));
        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    @Test
    void shouldFailWhenUserIdIsMissing() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> reservationService.createReservation(null, 1, 1));

        assertThat(exception.getMessage()).isEqualTo("User ID is required");
        verifyNoInteractions(eventRepository);
        verifyNoInteractions(reservationRepository);
    }

    @Test
    void shouldCancelReservationAndRestoreSeats() {
        Reservation reservation = new Reservation();
        reservation.setId(33L);
        reservation.setUserId(7L);
        reservation.setEventId(1);
        reservation.setTicketQuantity(3);
        reservation.setStatus("CONFIRMED");

        event.setAvailableSeats(5);

        when(reservationRepository.findById(33L)).thenReturn(Optional.of(reservation));
        when(eventRepository.findByIdForUpdate(1)).thenReturn(Optional.of(event));
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Reservation cancelled = reservationService.cancelReservation(7L, 33L);

        assertThat(cancelled.getStatus()).isEqualTo("CANCELLED");
        assertThat(cancelled.getCancelledAt()).isNotNull();
        assertThat(event.getAvailableSeats()).isEqualTo(8);

        ArgumentCaptor<Reservation> reservationCaptor = ArgumentCaptor.forClass(Reservation.class);
        verify(reservationRepository).save(reservationCaptor.capture());
        assertThat(reservationCaptor.getValue().getStatus()).isEqualTo("CANCELLED");
    }

    @Test
    void shouldNotAllowCancellationByOtherUser() {
        Reservation reservation = new Reservation();
        reservation.setId(33L);
        reservation.setUserId(7L);
        reservation.setEventId(1);
        reservation.setTicketQuantity(3);
        reservation.setStatus("CONFIRMED");

        when(reservationRepository.findById(33L)).thenReturn(Optional.of(reservation));

        AccessDeniedException exception = assertThrows(AccessDeniedException.class,
                () -> reservationService.cancelReservation(8L, 33L));

        assertThat(exception.getMessage()).isEqualTo("You do not own this reservation");
        verify(eventRepository, never()).findByIdForUpdate(anyInt());
        verify(reservationRepository, never()).save(any(Reservation.class));
    }
}