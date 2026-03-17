package com.soen345.meow;

import com.soen345.meow.controller.EventController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
public class EventControllerTest {

    @Autowired
    private EventController eventController;

    @Test
    public void contextLoads() {
        assertNotNull(eventController, "The EventController should be loaded in the application context.");
    }
}
