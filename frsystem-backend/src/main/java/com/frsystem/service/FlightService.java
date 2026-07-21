package com.frsystem.service;

import com.frsystem.dto.FlightRequest;
import com.frsystem.dto.FlightResponse;
import com.frsystem.enums.FlightStatus;
import com.frsystem.exception.ConflictException;
import com.frsystem.exception.ResourceNotFoundException;
import com.frsystem.mapper.FlightMapper;
import com.frsystem.model.Flight;
import com.frsystem.repository.FlightRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
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

    public List<FlightResponse> getAll() {
        return flightRepository.findAll()
                .stream()
                .map(flightMapper::toResponse)
                .toList();
    }

    public FlightResponse saveFlight(@Valid FlightRequest request) {

        Flight flight = flightMapper.toEntity(request);
        flight.setStatus(FlightStatus.ACTIVE);
        flight.setLastUpdate(Instant.now());

        //TODO: BU VE DİĞER SERVİSLER İÇİN ARAMAK YERİNE REPOSİTORYDE EXİSTSBY TANIMLAYIP ONU KULLANABİLİRSİN.
        if (flightRepository.findByFlightNumber(flight.getFlightNumber()).isPresent()) {
            throw new ConflictException("There is an already existing flight with same flight number!");
        }


        return flightMapper.toResponse(flightRepository.save(flight));
    }

    public List<FlightResponse> searchWithParameters(FlightRequest request) {

        Flight example = flightMapper.toEntity(request);

        ExampleMatcher matcher = ExampleMatcher.matching()
                .withIgnoreNullValues()
                .withIgnoreCase()
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING);


        return flightRepository.findAll(Example.of(example, matcher)).stream()
                .map(flightMapper::toResponse)
                .toList();
    }

    //TRANSACTIONAL OLABİLİR Mİ???
    public FlightResponse updateFlightStatus(Long ID, FlightStatus newStatus) {
        Flight existing = flightRepository.findById(ID)
                .orElseThrow(() -> new ResourceNotFoundException("There is no Flight with this ID!"));


        existing.setLastUpdate(Instant.now());
        existing.setStatus(newStatus);

        return flightMapper.toResponse(flightRepository.save(existing));
    }

    public void deleteFlightByID(Long ID) {
        Flight existing = flightRepository.findById(ID)
                .orElseThrow(() -> new ResourceNotFoundException("There is no Flight with this ID!"));
        flightRepository.delete(existing);
    }
}
