package com.frsystem.controller;

import com.frsystem.dto.ReservationRequest;
import com.frsystem.dto.ReservationResponse;
import com.frsystem.service.ReservationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reservations")
public class ReservationController {

    @Autowired
    private ReservationService reservationService;

    @PostMapping
    public ResponseEntity<ReservationResponse> makeReservation(@Valid @RequestBody ReservationRequest request) {
        return ResponseEntity.ok(reservationService.makeReservation(request));
    }

    @PatchMapping("/{reservationId}/cancel")
    public ResponseEntity<ReservationResponse> cancelReservation(@PathVariable Long reservationId) {
        return ResponseEntity.ok(reservationService.cancelSelfReservation(reservationId));
    }

    @GetMapping("/me")
    public ResponseEntity<List<ReservationResponse>> getMyReservations() {
        return ResponseEntity.ok(reservationService.getMyReservations());
    }

    @GetMapping
    public ResponseEntity<List<ReservationResponse>> getAllReservations() {
        return ResponseEntity.ok(reservationService.getAllReservations());
    }

    @PatchMapping("/{reservationId}/admin-cancel")
    public ResponseEntity<ReservationResponse> adminCancelReservation(@PathVariable Long reservationId) {
        return ResponseEntity.ok(reservationService.adminCancelReservation(reservationId));
    }
}