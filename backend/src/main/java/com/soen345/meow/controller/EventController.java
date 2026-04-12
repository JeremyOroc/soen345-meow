package com.soen345.meow.controller;

import com.soen345.meow.entity.Event;
import com.soen345.meow.repository.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/events")
public class EventController {

    @Autowired
    private EventRepository eventRepository;

    @GetMapping
    public List<Event> getActiveEvents(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        if (category == null && location == null && startDate == null && endDate == null) {
            return eventRepository.findByStatus("ACTIVE");
        }
        return eventRepository.findActiveWithFilters(category, location, startDate, endDate);
    }
}
