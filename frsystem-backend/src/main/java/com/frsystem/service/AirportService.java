package com.frsystem.service;

import com.frsystem.dto.AirportRequest;
import com.frsystem.dto.AirportResponse;
import com.frsystem.mapper.AirportMapper;
import com.frsystem.model.Airport;
import com.frsystem.repository.AirportRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Optional;

@Service
@Validated
public class AirportService {

    @Autowired
    private AirportRepository airportRepository;

    @Autowired
    private AirportMapper airportMapper;

    //***************************Gerekli mi??????*******************
    public List<AirportResponse> getAll() {
        return airportRepository.findAll()
                .stream()
                .map(airportMapper::toResponse)
                .toList();
    }

    //Delete using the ID, with validation of if it exists.
    public void deleteAirportByID(Long ID) {
        Airport existing = airportRepository.findById(ID)
                .orElseThrow(() -> new RuntimeException("There is no Airport with this ID!"));
        airportRepository.delete(existing);
    }

    //**************************************************
    //Parameter Search, injecting given parameters to the example Airplane class.
    public List<AirportResponse> searchWithParameters(AirportRequest request) {

        Airport example = airportMapper.toEntity(request);

        ExampleMatcher matcher = ExampleMatcher.matching()
                .withIgnoreNullValues()
                .withIgnoreCase()
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING); // Partial match

        return airportRepository.findAll(Example.of(example, matcher)).stream()
                .map(airportMapper::toResponse) // Her bir entity'yi Response DTO'ya çevir
                .toList();
    }
    //TODO: DTO İLE YAP! CHECK!

    //saving Airport With Validation.
    public AirportResponse saveAirport(@Valid AirportRequest request) {

        Airport airport = airportMapper.toEntity(request);

        if (airportRepository.findByIataCode(airport.getIataCode()).isPresent()) {
            throw new RuntimeException("There is an existing airplane with the same Iata Code!");
        }


        return airportMapper.toResponse(airportRepository.save(airport));
    }

    //Finding by ID.
    public Optional<AirportResponse> findByID(Long ID) {
        return airportRepository.findById(ID).map(airportMapper::toResponse);
    }

    //Updating Airport with new data. All the values should be given.
    public AirportResponse updateAirportByID(Long ID, @Valid AirportRequest newData) {

        Airport existing = airportRepository.findById(ID)
                .orElseThrow(() -> new RuntimeException("There is no Airport with this ID!"));

        if (!existing.getIataCode().equals(newData.getIataCode())) {
            Optional<Airport> airportWithSameIata = airportRepository.findByIataCode(newData.getIataCode());

            if (airportWithSameIata.isPresent()) {
                throw new RuntimeException("There is an existing airport with the same IATA!");
            }
        }

        airportMapper.updateEntity(existing, newData);
        Airport updatedEntity = airportRepository.save(existing);
        return airportMapper.toResponse(updatedEntity);


    }


}



