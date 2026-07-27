package com.frsystem.service;

import com.frsystem.dto.FlightRequest;
import com.frsystem.dto.FlightResponse;
import com.frsystem.enums.FlightStatus;
import com.frsystem.enums.ReservationStatus;
import com.frsystem.exception.ConflictException;
import com.frsystem.exception.ResourceNotFoundException;
import com.frsystem.mapper.FlightMapper;
import com.frsystem.model.Flight;
import com.frsystem.repository.FlightRepository;
import com.frsystem.repository.ReservationRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.Instant;
import java.util.List;

@Service
@Validated
public class FlightService {

    @Autowired
    private FlightRepository flightRepository;

    @Autowired
    private FlightMapper flightMapper;

    @Autowired
    private ReservationRepository reservationRepository;


    @Cacheable(value = "flights")
    public List<FlightResponse> getAll() {
        return flightRepository.findAll()
                .stream()
                .map(flightMapper::toResponse)
                .toList();
    }

    @CacheEvict(value = {"flights", "flightSearch"}, allEntries = true)
    public FlightResponse saveFlight(@Valid FlightRequest request) {

        Flight flight = flightMapper.toEntity(request);
        flight.setStatus(FlightStatus.ACTIVE);
        flight.setLastUpdate(Instant.now());


        if (flightRepository.findByFlightNumber(flight.getFlightNumber()).isPresent()) {
            throw new ConflictException("There is an already existing flight with same flight number!");
        }


        return flightMapper.toResponse(flightRepository.save(flight));
    }

    @Cacheable(value = "flightSearch", key = "#request.departureAirportId + '_' + #request.arrivalAirportId + '_' + #request.flightNumber")
    public List<FlightResponse> searchWithParameters(FlightRequest request) {

        Flight example = flightMapper.toEntity(request);

        ExampleMatcher matcher = ExampleMatcher.matching()
                .withIgnoreNullValues()
                .withMatcher("flightNumber", ExampleMatcher.GenericPropertyMatchers.contains().ignoreCase());


        return flightRepository.findAll(Example.of(example, matcher)).stream()
                .map(flightMapper::toResponse)
                .toList();
    }

    @CacheEvict(value = {"flights", "flightSearch"}, allEntries = true)
    public FlightResponse updateFlightStatus(Long ID, FlightStatus newStatus) {
        Flight existing = flightRepository.findById(ID)
                .orElseThrow(() -> new ResourceNotFoundException("There is no Flight with this ID!"));


        existing.setLastUpdate(Instant.now());
        existing.setStatus(newStatus);

        return flightMapper.toResponse(flightRepository.save(existing));
    }

    @CacheEvict(value = {"flights", "flightSearch"}, allEntries = true)
    public void deleteFlightByID(Long ID) {
        Flight existing = flightRepository.findById(ID)
                .orElseThrow(() -> new ResourceNotFoundException("There is no Flight with this ID!"));
        if (reservationRepository.existsByFlightAndStatus(existing, ReservationStatus.CONFIRMED)) {
            throw new ConflictException("This flight has reservations, first cancel the reservations.");
        }
        reservationRepository.deleteByFlight(existing);
        reservationRepository.flush();
        flightRepository.delete(existing);
    }
}
