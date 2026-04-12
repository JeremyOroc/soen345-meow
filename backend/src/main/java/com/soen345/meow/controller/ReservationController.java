package com.soen345.meow.controller;

import com.soen345.meow.dto.CreateReservationRequest;
import com.soen345.meow.dto.MessageResponse;
import com.soen345.meow.entity.Reservation;
import com.soen345.meow.security.AuthenticatedUser;
import com.soen345.meow.service.ReservationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    public ResponseEntity<?> createReservation(@RequestBody CreateReservationRequest request) {
        try {
            Long userId = getAuthenticatedUserId();
            Reservation reservation = reservationService.createReservation(userId, request.getEventId(), request.getQuantity());
            return ResponseEntity.status(HttpStatus.CREATED).body(reservation);
        } catch (IllegalStateException e) {
            return error(HttpStatus.CONFLICT, e.getMessage());
        } catch (IllegalArgumentException e) {
            return error(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> cancelReservation(@PathVariable Long id) {
        try {
            Long userId = getAuthenticatedUserId();
            reservationService.cancelReservation(userId, id);
            return ResponseEntity.ok(new MessageResponse("Reservation cancelled successfully"));
        } catch (AccessDeniedException e) {
            return error(HttpStatus.FORBIDDEN, e.getMessage());
        } catch (IllegalArgumentException e) {
            return error(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @GetMapping("/my")
    public ResponseEntity<List<Reservation>> getMyReservations() {
        Long userId = getAuthenticatedUserId();
        return ResponseEntity.ok(reservationService.getReservationsForUser(userId));
    }

    private Long getAuthenticatedUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser authenticatedUser)) {
            throw new AccessDeniedException("Unauthorized");
        }
        return authenticatedUser.id();
    }

    private ResponseEntity<Map<String, String>> error(HttpStatus status, String message) {
        Map<String, String> body = new HashMap<>();
        body.put("error", message);
        return ResponseEntity.status(status).body(body);
    }
}