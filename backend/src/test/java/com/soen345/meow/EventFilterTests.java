package com.soen345.meow;

import com.soen345.meow.entity.Event;
import com.soen345.meow.repository.EventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class EventFilterTests {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private EventRepository eventRepository;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        eventRepository.deleteAll();

        Event concert = new Event();
        concert.setTitle("Rock Night");
        concert.setCategory("Concerts");
        concert.setLocation("Montreal");
        concert.setEventDatetime("2026-06-15T20:00:00");
        concert.setCapacity(200);
        concert.setAvailableSeats(150);
        concert.setStatus("ACTIVE");
        eventRepository.save(concert);

        Event movie = new Event();
        movie.setTitle("Inception Rerun");
        movie.setCategory("Movies");
        movie.setLocation("Toronto");
        movie.setEventDatetime("2026-07-20T18:00:00");
        movie.setCapacity(100);
        movie.setAvailableSeats(80);
        movie.setStatus("ACTIVE");
        eventRepository.save(movie);

        Event sports = new Event();
        sports.setTitle("Soccer Finals");
        sports.setCategory("Sports");
        sports.setLocation("Montreal");
        sports.setEventDatetime("2026-08-10T15:00:00");
        sports.setCapacity(500);
        sports.setAvailableSeats(300);
        sports.setStatus("ACTIVE");
        eventRepository.save(sports);

        Event cancelled = new Event();
        cancelled.setTitle("Cancelled Show");
        cancelled.setCategory("Concerts");
        cancelled.setLocation("Montreal");
        cancelled.setEventDatetime("2026-06-01T20:00:00");
        cancelled.setCapacity(100);
        cancelled.setAvailableSeats(100);
        cancelled.setStatus("CANCELLED");
        eventRepository.save(cancelled);
    }

    @Test
    void shouldReturnAllActiveEventsWhenNoFilters() throws Exception {
        mockMvc.perform(get("/api/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));
    }

    @Test
    void shouldFilterByCategory() throws Exception {
        mockMvc.perform(get("/api/events").param("category", "Concerts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Rock Night"));
    }

    @Test
    void shouldFilterByLocation() throws Exception {
        mockMvc.perform(get("/api/events").param("location", "montreal"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void shouldFilterByDateRange() throws Exception {
        mockMvc.perform(get("/api/events")
                        .param("startDate", "2026-07-01T00:00:00")
                        .param("endDate", "2026-08-31T23:59:59"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void shouldReturnEmptyListWhenNoMatch() throws Exception {
        mockMvc.perform(get("/api/events").param("category", "Travel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
