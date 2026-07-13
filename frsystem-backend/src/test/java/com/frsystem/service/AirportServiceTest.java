package com.frsystem.service;

import com.frsystem.dto.AirportRequest;
import com.frsystem.dto.AirportResponse;
import com.frsystem.repository.AirportRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
public class AirportServiceTest {

    @Autowired
    private AirportService airportService;
    @Autowired
    private AirportRepository airportRepository;

    @BeforeEach
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
        AirportRequest request = new AirportRequest("ESB", "Esenboğa", "Türkiye", "Ankara");


        AirportResponse saved = airportService.saveAirport(request);

        assertNotNull(saved.getId());
        assertTrue(airportRepository.findByIataCode("ESB").isPresent());
    }

    @Test
    void shouldThrowExceptionWhenTriedToSaveWithExistingIataCode() {
        AirportRequest airport = new AirportRequest("ESB", "Esenboğa", "Türkiye", "Ankara");
        AirportRequest airport1 = new AirportRequest("ESB", "Esmeyenboğa", "Suriye", "Şam");

        airportService.saveAirport(airport);

        Exception exception = assertThrows(RuntimeException.class, () -> {
            airportService.saveAirport(airport1);
        });

        assertEquals("There is an existing airplane with the same Iata Code!", exception.getMessage());

    }

    @Test
    void shouldDeleteIfTheAirportWithProvidedIDExists() {
        AirportRequest airport = new AirportRequest("ESB", "Esenboğa", "Türkiye", "Ankara");

        AirportResponse savedAirport = airportService.saveAirport(airport);
        Long ID = savedAirport.getId();
        airportService.deleteAirportByID(ID);

        assertFalse(airportRepository.existsById(ID));
    }

    @Test
    void shouldThrowExceptionIfTheAirportWithProvidedIDDoesNotExistsTriedToBeDeleted() {
        AirportRequest airport = new AirportRequest("ESB", "Esenboğa", "Türkiye", "Ankara");

        AirportResponse savedAirport = airportService.saveAirport(airport);
        Long ID = savedAirport.getId();

        Exception exception = assertThrows(RuntimeException.class, () -> {
            airportService.deleteAirportByID(ID + 1);
        });

        assertEquals("There is no Airport with this ID!", exception.getMessage());

    }

    @Test
    void shouldGetTheTrueAirportIfItExistsWhenSearchedByID() {

        AirportRequest request = new AirportRequest("ESB", "Esenboğa", "Türkiye", "Ankara");
        AirportResponse savedAirport = airportService.saveAirport(request);


        Long searchedID = savedAirport.getId();

        AirportResponse foundEntity = airportService.findByID(searchedID).get();


        assertEquals(searchedID, foundEntity.getId());
        assertEquals("ESB", foundEntity.getIataCode());
        assertEquals("Esenboğa", foundEntity.getName());
        assertEquals("Türkiye", foundEntity.getCountry());
        assertEquals("Ankara", foundEntity.getCity());
    }

    @Test
    void shouldReturnEmptyWhenNonExistingIdSearched() {
        AirportRequest existingAirport = new AirportRequest("ESB", "Esenboğa", "Türkiye", "Ankara");
        AirportResponse savedAirport = airportService.saveAirport(existingAirport);

        assertFalse(airportService.findByID(savedAirport.getId() + 1).isPresent());

    }

    @Test
    void shouldReturnEmptyWhenSearchWithParametersForNonExistingEntry() {
        AirportRequest existingAirport = new AirportRequest("ESB", "Esenboğa", "Türkiye", "Ankara");
        airportService.saveAirport(existingAirport);

        AirportRequest searchCriteria = new AirportRequest("IST", null, null, null);
        List<AirportResponse> returnEntities = airportService.searchWithParameters(searchCriteria);
        assertTrue(returnEntities.isEmpty());
    }

    @Test
    void shouldReturnAllTheMatchingEntriesWhenSearchedIfExists() {
        AirportRequest existingAirport = new AirportRequest("ESB", "Esenboğa Havalimanı", "Türkiye", "Ankara");
        AirportRequest existingAirport1 = new AirportRequest("IST", "İstanbul Havalimanı", "Türkiye", "İstanbul");
        AirportRequest existingAirport2 = new AirportRequest("SKP", "Üsküp Havalimanı", "Makedonya", "Üsküp");


        airportService.saveAirport(existingAirport);
        airportService.saveAirport(existingAirport1);
        airportService.saveAirport(existingAirport2);

        AirportRequest searchCriteria = new AirportRequest(null, null, "tür", null);
        List<AirportResponse> returnEntities = airportService.searchWithParameters(searchCriteria);

        //Did it catch exactly 2 values it supposed to catch?
        assertEquals(2, returnEntities.size());

        List<String> iataCodes = returnEntities.stream()
                .map(AirportResponse::getIataCode)
                .toList();

        assertTrue(iataCodes.contains("ESB"), "ESB should be Included");
        assertTrue(iataCodes.contains("IST"), "IST should be Included");
        assertFalse(iataCodes.contains("SKP"), "SKP should NOT be included");

    }

    @Test
    void shouldThrowExceptionWhenTheUpdateIDDoesNotExists() {
        AirportRequest airport = new AirportRequest("ESB", "Esenboğa", "Türkiye", "Ankara");
        AirportResponse savedAirport = airportService.saveAirport(airport);

        AirportRequest updateRequest = new AirportRequest("IST", "İstanbul Havalimanı", "Türkiye", "İstanbul");

        Long wrongSearchID = savedAirport.getId();

        Exception exception = assertThrows(RuntimeException.class, () -> {
            airportService.updateAirportByID(wrongSearchID + 1, updateRequest);
        });

        assertEquals("There is no Airport with this ID!", exception.getMessage());
    }

    @ParameterizedTest
    @MethodSource("provideInvalidAirports")
    void shouldThrowExceptionWhenUpdateBodyVioletesValidation(AirportRequest invalidRequest) {
        AirportRequest airport = new AirportRequest("ESB", "Esenboğa", "Türkiye", "Ankara");
        AirportResponse savedAirport = airportService.saveAirport(airport);
        Long savedID = savedAirport.getId();


        //ID Should be right to test only the body.
        assertThrows(ConstraintViolationException.class, () -> {
            airportService.updateAirportByID(savedID, invalidRequest);
        });

    }

    @Test
    void shouldSuccessfullyUpdateTheAirport() {
        AirportRequest airport = new AirportRequest("ESB", "Esenboğa", "Türkiye", "Ankara");
        AirportResponse savedAirport = airportService.saveAirport(airport);
        Long savedID = savedAirport.getId();

        AirportRequest updateRequest = new AirportRequest("IST", "İstanbul Havalimanı", "Türkiye", "İstanbul");
        airportService.updateAirportByID(savedID, updateRequest);

        Optional<AirportResponse> afterUpdateObject = airportService.findByID(savedID);

        assertEquals(updateRequest.getIataCode(), afterUpdateObject.get().getIataCode());
        assertEquals(updateRequest.getName(), afterUpdateObject.get().getName());
        assertEquals(updateRequest.getCountry(), afterUpdateObject.get().getCountry());
        assertEquals(updateRequest.getCity(), afterUpdateObject.get().getCity());

    }

    @Test
    void shouldThrowExceptionWhenThereIsAnExistingIataWhileUpdate() {
        AirportRequest airportWithSameIata = new AirportRequest("ESB", "Esenboğa", "Türkiye", "Ankara");
        AirportResponse savedAirportWithSameIata = airportService.saveAirport(airportWithSameIata);

        AirportRequest airportForUpdate = new AirportRequest("IST", "İstanbul Havalimanı", "Türkiye", "İstanbul");
        AirportResponse savedAirportForUpdate = airportService.saveAirport(airportForUpdate);
        Long ID = savedAirportForUpdate.getId();

        AirportRequest updateRequest = new AirportRequest("ESB", "İstanbul Havalimanı", "Türkiye", "İstanbul");
        Exception exception = assertThrows(RuntimeException.class, () -> {
            airportService.updateAirportByID(ID, updateRequest);
        });

        assertEquals("There is an existing airport with the same IATA!", exception.getMessage());


    }
}


