package com.soen345.meow;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.soen345.meow.entity.Event;
import com.soen345.meow.entity.User;
import com.soen345.meow.repository.EventRepository;
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

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class AdminEventControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventRepository eventRepository;

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

        eventRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void shouldCreateEventAsAdmin() throws Exception {
        String adminToken = tokenFor("admin@example.com", "ADMIN");

        mockMvc.perform(post("/api/admin/events")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCreateRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Jazz Night"))
                .andExpect(jsonPath("$.availableSeats").value(120))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        assertThat(eventRepository.findAll()).hasSize(1);
    }

    @Test
    void shouldReturn403WhenCustomerTriesToCreateEvent() throws Exception {
        String customerToken = tokenFor("customer@example.com", "CUSTOMER");

        mockMvc.perform(post("/api/admin/events")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCreateRequest())))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturn401WhenUnauthenticated() throws Exception {
        mockMvc.perform(post("/api/admin/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCreateRequest())))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldEditEventAsAdmin() throws Exception {
        String adminToken = tokenFor("admin@example.com", "ADMIN");
        Event event = saveEvent("Initial Event", "Concert", "Montreal", "2026-05-01T19:00:00", 100, "ACTIVE");

        Map<String, Object> updateRequest = new HashMap<>();
        updateRequest.put("title", "Updated Event");
        updateRequest.put("category", "Sports");
        updateRequest.put("location", "Toronto");
        updateRequest.put("eventDatetime", "2026-05-15T18:30:00");
        updateRequest.put("capacity", 180);

        mockMvc.perform(put("/api/admin/events/{id}", event.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Event"))
                .andExpect(jsonPath("$.category").value("Sports"))
                .andExpect(jsonPath("$.availableSeats").value(180));
    }

    @Test
    void shouldCancelEventAsAdmin() throws Exception {
        String adminToken = tokenFor("admin@example.com", "ADMIN");
        Event event = saveEvent("To Cancel", "Movies", "Montreal", "2026-06-01T21:00:00", 80, "ACTIVE");

        mockMvc.perform(delete("/api/admin/events/{id}/cancel", event.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        Event updated = eventRepository.findById(event.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo("CANCELLED");
    }

    private String tokenFor(String email, String role) {
        userRepository.save(new User(email, null, passwordEncoder.encode("password123"), role));
        return jwtUtil.generateToken(email, role);
    }

    private Map<String, Object> validCreateRequest() {
        Map<String, Object> body = new HashMap<>();
        body.put("title", "Jazz Night");
        body.put("category", "Concert");
        body.put("location", "Montreal");
        body.put("eventDatetime", "2026-05-10T20:00:00");
        body.put("capacity", 120);
        return body;
    }

    private Event saveEvent(String title, String category, String location, String eventDatetime, int capacity, String status) {
        Event event = new Event();
        event.setTitle(title);
        event.setCategory(category);
        event.setLocation(location);
        event.setEventDatetime(eventDatetime);
        event.setCapacity(capacity);
        event.setAvailableSeats(capacity);
        event.setStatus(status);
        return eventRepository.save(event);
    }
}
