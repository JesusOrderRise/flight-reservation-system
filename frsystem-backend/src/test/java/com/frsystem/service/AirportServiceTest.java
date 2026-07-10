package com.frsystem.service;

import com.frsystem.dto.AirportRequest;
import com.frsystem.model.Airport;
import com.frsystem.repository.AirportRepository;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class AirportServiceTest {

    @Autowired
    private AirportService airportService;
    @Autowired
    private AirportRepository airportRepository;

    @AfterEach
    void tearDown() {
        airportRepository.deleteAll();
    }

    //is service loaded?
    @Test
    void serviceShouldLoadContext() {
        assertNotNull(airportService);
    }

    // All possible invalid scenarios.
    private static Stream<AirportRequest> provideInvalidAirports() {
        return Stream.of(
                new AirportRequest("ESB", "Esenboğa", "Türkiye", null),
                new AirportRequest("ESB", "Esenboğa", null, "Ankara"),
                new AirportRequest("ESB", null, "Türkiye", "Ankara"),
                new AirportRequest(null, "Esenboğa", "Türkiye", "Ankara"),
                new AirportRequest("ES7", "Esenboğa", "Türkiye", "Ankara"),
                new AirportRequest("ES", "Esenboğa", "Türkiye", "Ankara"),
                new AirportRequest("ESBB", "Esenboğa", "Türkiye", "Ankara"),
                new AirportRequest("esb", "Esenboğa", "Türkiye", "Ankara")
        );
    }

    @ParameterizedTest
    @MethodSource("provideInvalidAirports")
    void shouldThrowExceptionWhenSavingInvalidAirport(AirportRequest invalidAirportRequest) {

        assertThrows(ConstraintViolationException.class, () -> {
            airportService.saveAirport(invalidAirportRequest);
        });
    }

    @Test
    void shouldSaveToDatabase() {
        AirportRequest request = new AirportRequest();
        request.setIataCode("ESB");
        request.setName("Esenboğa");
        request.setCountry("Türkiye");
        request.setCity("Ankara");

        Airport saved = airportService.saveAirport(request);

        assertNotNull(saved.getId());
        assertTrue(airportRepository.findByIataCode("ESB").isPresent());
    }

    @Test
    void shouldThrowExceptionWhenTriedToSaveWithExistingIataCode() {
        AirportRequest airport = new AirportRequest("ESB", "Esenboğa", "Türkiye", "Ankara");
        AirportRequest airport1 = new AirportRequest("ESB", "Esmeyenboğa", "Suriye", "Şam");

        airportService.saveAirport(airport);

        assertThrows(DataIntegrityViolationException.class, () -> {
            airportService.saveAirport(airport1);
        });

    }

    @Test
    void shouldDeleteIfTheAirportWithProvidedIDExists() {
        AirportRequest airport = new AirportRequest("ESB", "Esenboğa", "Türkiye", "Ankara");

        Airport savedAirport = airportService.saveAirport(airport);
        Long ID = savedAirport.getId();
        airportService.deleteAirportByID(ID);

        assertFalse(airportRepository.existsById(ID));
    }

    @Test
    void shouldThrowExceptionIfTheAirportWithProvidedIDDoesNotExistsTriedToBeDeleted() {
        AirportRequest airport = new AirportRequest("ESB", "Esenboğa", "Türkiye", "Ankara");

        Airport savedAirport = airportService.saveAirport(airport);
        Long ID = savedAirport.getId();

        Exception exception = assertThrows(RuntimeException.class, () -> {
            airportService.deleteAirportByID(ID + 1);
        });

        assertEquals("There is no Airport with this ID!", exception.getMessage());

    }

    @Test
    void shouldGetTheTrueAirportIfItExistsWhenSearchedByID() {

        AirportRequest request = new AirportRequest("ESB", "Esenboğa", "Türkiye", "Ankara");
        Airport savedAirport = airportService.saveAirport(request);


        Long searchedID = savedAirport.getId();

        Airport foundEntity = airportService.findByID(searchedID).get();


        assertEquals(searchedID, foundEntity.getId());
        assertEquals("ESB", foundEntity.getIataCode());
        assertEquals("Esenboğa", foundEntity.getName());
        assertEquals("Türkiye", foundEntity.getCountry());
        assertEquals("Ankara", foundEntity.getCity());
    }

    @Test
    void shouldReturnEmptyWhenNonExistingIdSearched() {
        AirportRequest existingAirport = new AirportRequest("ESB", "Esenboğa", "Türkiye", "Ankara");
        Airport savedAirport = airportService.saveAirport(existingAirport);

        assertFalse(airportService.findByID(savedAirport.getId() + 1).isPresent());

    }
}


