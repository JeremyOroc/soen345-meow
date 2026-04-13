package com.soen345.meow;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.soen345.meow.dto.CreateReservationRequest;
import com.soen345.meow.entity.Event;
import com.soen345.meow.entity.Reservation;
import com.soen345.meow.entity.User;
import com.soen345.meow.repository.EventRepository;
import com.soen345.meow.repository.ReservationRepository;
import com.soen345.meow.repository.UserRepository;
import com.soen345.meow.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class ReservationControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();

        reservationRepository.deleteAll();
        eventRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void shouldReturn201WhenReservationCreated() throws Exception {
        User user = userRepository.save(new User("customer@example.com", null, passwordEncoder.encode("password123"), "CUSTOMER"));
        Event event = saveEvent(10);
        String token = jwtUtil.generateToken(user.getEmail(), user.getRole());

        CreateReservationRequest request = new CreateReservationRequest();
        request.setEventId(event.getId());
        request.setQuantity(2);

        mockMvc.perform(post("/api/reservations")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.eventId").value(event.getId()))
                .andExpect(jsonPath("$.ticketQuantity").value(2));

        Event updatedEvent = eventRepository.findById(event.getId()).orElseThrow();
        assertThat(updatedEvent.getAvailableSeats()).isEqualTo(8);
    }

    @Test
    void shouldReturn409WhenNoSeatsAvailable() throws Exception {
        User user = userRepository.save(new User("customer@example.com", null, passwordEncoder.encode("password123"), "CUSTOMER"));
        Event event = saveEvent(1);
        String token = jwtUtil.generateToken(user.getEmail(), user.getRole());

        CreateReservationRequest request = new CreateReservationRequest();
        request.setEventId(event.getId());
        request.setQuantity(2);

        mockMvc.perform(post("/api/reservations")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Insufficient seats"));
    }

    @Test
    void shouldReturn401WhenNotAuthenticated() throws Exception {
        Event event = saveEvent(5);

        CreateReservationRequest request = new CreateReservationRequest();
        request.setEventId(event.getId());
        request.setQuantity(1);

        mockMvc.perform(post("/api/reservations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldCancelReservationSuccessfully() throws Exception {
        User user = userRepository.save(new User("customer@example.com", null, passwordEncoder.encode("password123"), "CUSTOMER"));
        Event event = saveEvent(10);

        Reservation reservation = new Reservation();
        reservation.setUserId(user.getId());
        reservation.setEventId(event.getId());
        reservation.setTicketQuantity(2);
        reservation.setStatus("CONFIRMED");
        reservation.setReservedAt(LocalDateTime.now());
        reservation = reservationRepository.save(reservation);

        event.setAvailableSeats(8);
        eventRepository.save(event);

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole());

        mockMvc.perform(delete("/api/reservations/{id}", reservation.getId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Reservation cancelled successfully"));

        Reservation updatedReservation = reservationRepository.findById(reservation.getId()).orElseThrow();
        Event updatedEvent = eventRepository.findById(event.getId()).orElseThrow();

        assertThat(updatedReservation.getStatus()).isEqualTo("CANCELLED");
        assertThat(updatedReservation.getCancelledAt()).isNotNull();
        assertThat(updatedEvent.getAvailableSeats()).isEqualTo(10);
    }

    private Event saveEvent(int availableSeats) {
        Event event = new Event();
        event.setTitle("SOEN Concert");
        event.setCategory("Concert");
        event.setLocation("Montreal");
        event.setEventDatetime("2026-04-20T20:00:00");
        event.setCapacity(50);
        event.setAvailableSeats(availableSeats);
        event.setStatus("ACTIVE");
        return eventRepository.save(event);
    }
}