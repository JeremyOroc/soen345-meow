package com.soen345.meow.controller;

import com.soen345.meow.entity.Event;
import com.soen345.meow.repository.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/events")
public class EventController {

    @Autowired
    private EventRepository eventRepository;

    @GetMapping
    public List<Event> getActiveEvents() {
        return eventRepository.findByStatus("ACTIVE");
    }
}
