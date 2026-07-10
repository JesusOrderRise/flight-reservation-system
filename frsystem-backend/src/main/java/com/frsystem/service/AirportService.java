package com.frsystem.service;

import com.frsystem.dto.AirportRequest;
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

    //Delete using the ID, with validation of if it exists.
    public void deleteAirportByID(Long ID) {
        Airport existing = airportRepository.findById(ID)
                .orElseThrow(() -> new RuntimeException("There is no Airport with this ID!"));
        airportRepository.delete(existing);
    }

    //Parameter Search, injecting given parameters to the example Airplane class.
    public List<Airport> searchWithParameters(String iataCode, String name, String city, String country) {

        Airport example = new Airport();

        if (iataCode != null && !iataCode.isBlank()) {
            String cleaned = iataCode.trim().toUpperCase();
            // Optional: Geçersiz IATA code'u filtrele
            if (cleaned.matches("^[A-Z]{3}$")) {
                example.setIataCode(cleaned);
            }
        }

        if (name != null && !name.isBlank()) {
            example.setName(name.trim());
        }


        if (city != null && !city.isBlank()) {
            example.setCity(city.trim());
        }


        if (country != null && !country.isBlank()) {
            example.setCountry(country.trim());
        }

        ExampleMatcher matcher = ExampleMatcher.matching()
                .withIgnoreNullValues()
                .withIgnoreCase()
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING); // Partial match

        return airportRepository.findAll(Example.of(example, matcher));
    }

    //saving Airport With Validation.
    public Airport saveAirport(@Valid AirportRequest request) {

        Airport airport = new Airport();

        airport.setIataCode(request.getIataCode());
        airport.setName(request.getName());
        airport.setCountry(request.getCountry());
        airport.setCity(request.getCity());

        return airportRepository.save(airport);
    }

    //Finding by ID.
    public Optional<Airport> findByID(Long ID) {
        return airportRepository.findById(ID);
    }

    //Updating Airport with new data. The values that wont change should be given as null or blank.
    public Airport updateAirport(Long ID, AirportRequest newData) {

        Airport existing = airportRepository.findById(ID)
                .orElseThrow(() -> new RuntimeException("There is no airport with this ID!"));


        if (newData.getIataCode() != null && !newData.getIataCode().isBlank()) {
            String cleaned = newData.getIataCode();

            if (cleaned.matches("^[A-Z]{3}$")) {
                if (!existing.getIataCode().equals(newData.getIataCode())) {
                    Optional<Airport> airportWithSameIata = airportRepository.findByIataCode(newData.getIataCode());

                    if (airportWithSameIata.isPresent()) {
                        throw new RuntimeException("There is an existing airport with the same IATA!");
                    }
                }
                existing.setIataCode(newData.getIataCode());
            }
        }
        if (newData.getName() != null && !newData.getName().isBlank()) {
            existing.setName(newData.getName());
        }
        if (newData.getCountry() != null && !newData.getCountry().isBlank()) {
            existing.setCountry(newData.getCountry());
        }
        if (newData.getCity() != null && !newData.getCity().isBlank()) {
            existing.setCity(newData.getCity());
        }
        return airportRepository.save(existing);
    }


}



