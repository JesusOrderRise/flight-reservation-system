package com.frsystem.controller;

import com.frsystem.dto.FlightRequest;
import com.frsystem.dto.FlightResponse;
import com.frsystem.enums.FlightStatus;
import com.frsystem.service.FlightService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/flights")
public class FlightController {

    @Autowired
    private FlightService flightService;

    @GetMapping
    public ResponseEntity<List<FlightResponse>> getAllFlights() {
        return ResponseEntity.ok(flightService.getAll());
    }

    @PostMapping("/search")
    public ResponseEntity<List<FlightResponse>> searchFlights(@RequestBody FlightRequest request) {
        return ResponseEntity.ok(flightService.searchWithParameters(request));
    }

    @PostMapping
    public ResponseEntity<FlightResponse> createFlight(@Valid @RequestBody FlightRequest request) {
        return ResponseEntity.ok(flightService.saveFlight(request));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<FlightResponse> updateStatus(@PathVariable Long id, @RequestParam FlightStatus status) {
        return ResponseEntity.ok(flightService.updateFlightStatus(id, status));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFlight(@PathVariable Long id) {
        flightService.deleteFlightByID(id);
        return ResponseEntity.noContent().build();
    }
}