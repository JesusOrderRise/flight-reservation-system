package com.frsystem.service;

import com.frsystem.dto.AirplaneRequest;
import com.frsystem.dto.AirplaneResponse;
import com.frsystem.repository.AirplaneRepository;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class AirplaneServiceTest {

    @Autowired
    private AirplaneService airplaneService;
    @Autowired
    private AirplaneRepository airplaneRepository;

    @AfterEach
    void tearDown() {
        airplaneRepository.deleteAll();
    }

    //is service loaded?
    @Test
    void serviceShouldLoadContext() {
        assertNotNull(airplaneService);
    }


    // All possible invalid scenarios.
    private static Stream<AirplaneRequest> provideInvalidAirplanes() {
        return Stream.of(
                new AirplaneRequest(null, "THY", "Boeing", 200),
                new AirplaneRequest("TC-123", null, "Boeing", 200),
                new AirplaneRequest("TC-123", "THY", null, 200),
                new AirplaneRequest("TC-123", "THY", "Boeing", null),
                new AirplaneRequest("TC-123", "THY", "Boeing", -5),
                new AirplaneRequest("TC-123", "THY", "Boeing", 0),
                new AirplaneRequest("TC-123", "THY", "Boeing", 1000)

        );
    }


    @ParameterizedTest
    @MethodSource("provideInvalidAirplanes")
    void shouldThrowExceptionWhenSavingInvalidAirplane(AirplaneRequest invalidAirplaneRequest) {

        assertThrows(ConstraintViolationException.class, () -> {
            airplaneService.saveAirplane(invalidAirplaneRequest);
        });
    }

    @Test
    void shouldSaveToDatabase() {
        AirplaneRequest request = new AirplaneRequest("TC-123", "THY", "Boeing", 200);

        AirplaneResponse saved = airplaneService.saveAirplane(request);

        assertNotNull(saved.getId());
        assertTrue(airplaneRepository.findByTailNumber("TC-123").isPresent());//test everything
    }

    @Test
    void shouldThrowExceptionWhenTriedToSaveWithExistingTailNumber() {
        AirplaneRequest airplane = new AirplaneRequest("TC-123", "THY", "Boeing", 200);
        AirplaneRequest airplane1 = new AirplaneRequest("TC-123", "Pegasus", "A-10", 150);

        airplaneService.saveAirplane(airplane);

        assertThrows(DataIntegrityViolationException.class, () -> {
            airplaneService.saveAirplane(airplane1);
        });

    }

    @Test
    void shouldDeleteIfTheAirplaneWithProvidedIDExists() {
        AirplaneRequest airplane = new AirplaneRequest("TC-123", "THY", "Boeing", 200);

        AirplaneResponse savedAirplane = airplaneService.saveAirplane(airplane);
        Long ID = savedAirplane.getId();
        airplaneService.deleteAirplaneByID(ID);

        assertFalse(airplaneRepository.existsById(ID));
    }

    @Test
    void shouldThrowExceptionIfTheAirplaneWithProvidedIDDoesNotExistsTriedToBeDeleted() {
        AirplaneRequest airplane = new AirplaneRequest("TC-123", "THY", "Boeing", 200);

        AirplaneResponse savedAirplane = airplaneService.saveAirplane(airplane);
        Long ID = savedAirplane.getId();

        Exception exception = assertThrows(RuntimeException.class, () -> {
            airplaneService.deleteAirplaneByID(ID + 1);
        });

        assertEquals("There is no Airplane with this ID!", exception.getMessage());

    }

    @Test
    void shouldGetTheTrueAirplaneIfItExistsWhenSearchedByID() {

        AirplaneRequest request = new AirplaneRequest("TC-123", "THY", "Boeing", 200);
        AirplaneResponse savedAirplane = airplaneService.saveAirplane(request);


        Long searchedID = savedAirplane.getId();
        String searchedTailNumber = savedAirplane.getTailNumber();  // ← AYNI KAYITTAN AL!


        AirplaneResponse foundEntity = airplaneService.findByID(searchedID).get();


        assertEquals(searchedID, foundEntity.getId());
        assertEquals(searchedTailNumber, foundEntity.getTailNumber());
        assertEquals("THY", foundEntity.getAirline());
        assertEquals("Boeing", foundEntity.getModel());
        assertEquals(200, foundEntity.getCapacity());
    }

    @Test
    void shouldReturnEmptyWhenNonExistingIdSearched() {
        AirplaneRequest existingAirplane = new AirplaneRequest("TC-123", "THY", "Boeing", 200);
        AirplaneResponse savedAirplane = airplaneService.saveAirplane(existingAirplane);

        assertFalse(airplaneService.findByID(savedAirplane.getId() + 1).isPresent());

    }

    @Test
    void shouldReturnEmptyWhenSearchWithParametersForNonExistingEntry() {
        AirplaneRequest existingAirplane = new AirplaneRequest("TC-123", "THY", "Boeing", 200);
        airplaneService.saveAirplane(existingAirplane);

        AirplaneRequest searchCriteria = new AirplaneRequest("IST", null, null, null);
        List<AirplaneResponse> returnEntities = airplaneService.searchWithParameters(searchCriteria);
        assertTrue(returnEntities.isEmpty());
    }

    @Test
    void shouldReturnAllTheMatchingEntriesWhenSearchedIfExists() {
        AirplaneRequest existingAirplane = new AirplaneRequest("TC-1923", "THY", "Boeing", 200);
        AirplaneRequest existingAirplane1 = new AirplaneRequest("TC-1453", "Pegasus", "Boeing", 200);
        AirplaneRequest existingAirplane2 = new AirplaneRequest("TC-1071", "AJet", "A-10", 20);


        airplaneService.saveAirplane(existingAirplane);
        airplaneService.saveAirplane(existingAirplane1);
        airplaneService.saveAirplane(existingAirplane2);

        AirplaneRequest searchCriteria = new AirplaneRequest(null, null, "boe", null);
        List<AirplaneResponse> returnEntities = airplaneService.searchWithParameters(searchCriteria);

        //Did it catch exactly 2 values it supposed to catch?
        assertEquals(2, returnEntities.size());

        List<String> iataCodes = returnEntities.stream()
                .map(AirplaneResponse::getTailNumber)
                .toList();

        assertTrue(iataCodes.contains("TC-1923"), "This should be Included");
        assertTrue(iataCodes.contains("TC-1453"), "This should be Included");
        assertFalse(iataCodes.contains("TC-1071"), "This should NOT be included");

    }

    @Test
    void shouldThrowExceptionWhenTheUpdateIDDoesNotExists() {
        AirplaneRequest airplane = new AirplaneRequest("TC-1923", "THY", "Boeing", 200);
        AirplaneResponse savedAirplane = airplaneService.saveAirplane(airplane);

        AirplaneRequest updateRequest = new AirplaneRequest("TC-1071", "AJet", "A-10", 20);

        Long wrongSearchID = savedAirplane.getId();

        Exception exception = assertThrows(RuntimeException.class, () -> {
            airplaneService.updateAirplaneByID(wrongSearchID + 1, updateRequest);
        });

        assertEquals("There is no Airplane with this ID!", exception.getMessage());
    }

    @ParameterizedTest
    @MethodSource("provideInvalidAirplanes")
    void shouldThrowExceptionWhenUpdateBodyVioletesValidation(AirplaneRequest invalidRequest) {
        AirplaneRequest airplane = new AirplaneRequest("TC-1071", "AJet", "A-10", 20);
        AirplaneResponse savedAirplane = airplaneService.saveAirplane(airplane);
        Long savedID = savedAirplane.getId();


        //ID Should be right to test only the body.
        assertThrows(ConstraintViolationException.class, () -> {
            airplaneService.updateAirplaneByID(savedID, invalidRequest);
        });

    }

    @Test
    void shouldSuccessfullyUpdateTheAirplane() {
        AirplaneRequest airplane = new AirplaneRequest("TC-1923", "THY", "Boeing", 200);
        AirplaneResponse savedAirplane = airplaneService.saveAirplane(airplane);
        Long savedID = savedAirplane.getId();

        AirplaneRequest updateRequest = new AirplaneRequest("TC-1071", "AJet", "A-10", 20);
        airplaneService.updateAirplaneByID(savedID, updateRequest);

        Optional<AirplaneResponse> afterUpdateObject = airplaneService.findByID(savedID);

        assertEquals(updateRequest.getTailNumber(), afterUpdateObject.get().getTailNumber());
        assertEquals(updateRequest.getAirline(), afterUpdateObject.get().getAirline());
        assertEquals(updateRequest.getModel(), afterUpdateObject.get().getModel());
        assertEquals(updateRequest.getCapacity(), afterUpdateObject.get().getCapacity());

    }

    @Test
    void shouldThrowExceptionWhenThereIsAnExistingTailNumberWhileUpdate() {
        AirplaneRequest airplaneWithSameTailNumber = new AirplaneRequest("TC-1923", "THY", "Boeing", 200);
        AirplaneResponse savedAirplaneWithSameTailNumber = airplaneService.saveAirplane(airplaneWithSameTailNumber);

        AirplaneRequest airplaneForUpdate = new AirplaneRequest("TC-1071", "AJet", "A-10", 20);
        AirplaneResponse savedAirplaneForUpdate = airplaneService.saveAirplane(airplaneForUpdate);
        Long ID = savedAirplaneForUpdate.getId();

        AirplaneRequest updateRequest = new AirplaneRequest("TC-1923", "AJet", "A-10", 20);
        Exception exception = assertThrows(RuntimeException.class, () -> {
            airplaneService.updateAirplaneByID(ID, updateRequest);
        });

        assertEquals("There is an existing airplane with the same Tail Number!", exception.getMessage());


    }
}



