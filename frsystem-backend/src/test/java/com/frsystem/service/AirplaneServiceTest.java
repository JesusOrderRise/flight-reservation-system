package com.frsystem.service;

import com.frsystem.dto.AirplaneRequest;
import com.frsystem.model.Airplane;
import com.frsystem.repository.AirplaneRepository;
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
        AirplaneRequest request = new AirplaneRequest();
        request.setTailNumber("TC-123");
        request.setAirline("THY");
        request.setModel("Boeing");
        request.setCapacity(200);

        Airplane saved = airplaneService.saveAirplane(request);

        assertNotNull(saved.getId());
        assertTrue(airplaneRepository.findByTailNumber("TC-123").isPresent());
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

        Airplane savedAirplane = airplaneService.saveAirplane(airplane);
        Long ID = savedAirplane.getId();
        airplaneService.deleteAirplaneByID(ID);

        assertFalse(airplaneRepository.existsById(ID));
    }

    @Test
    void shouldThrowExceptionIfTheAirplaneWithProvidedIDDoesNotExistsTriedToBeDeleted() {
        AirplaneRequest airplane = new AirplaneRequest("TC-123", "THY", "Boeing", 200);

        Airplane savedAirplane = airplaneService.saveAirplane(airplane);
        Long ID = savedAirplane.getId();

        Exception exception = assertThrows(RuntimeException.class, () -> {
            airplaneService.deleteAirplaneByID(ID + 1);
        });

        assertEquals("There is no Airplane with this ID!", exception.getMessage());

    }

    @Test
    void shouldGetTheTrueAirplaneIfItExistsWhenSearchedByID() {

        AirplaneRequest request = new AirplaneRequest("TC-123", "THY", "Boeing", 200);
        Airplane savedAirplane = airplaneService.saveAirplane(request);


        Long searchedID = savedAirplane.getId();
        String searchedTailNumber = savedAirplane.getTailNumber();  // ← AYNI KAYITTAN AL!


        Airplane foundEntity = airplaneService.findByID(searchedID).get();


        assertEquals(searchedID, foundEntity.getId());
        assertEquals(searchedTailNumber, foundEntity.getTailNumber());
        assertEquals("THY", foundEntity.getAirline());
        assertEquals("Boeing", foundEntity.getModel());
        assertEquals(200, foundEntity.getCapacity());
    }

    @Test
    void shouldReturnEmptyWhenNonExistingIdSearched() {
        AirplaneRequest existingAirplane = new AirplaneRequest("TC-123", "THY", "Boeing", 200);
        Airplane savedAirplane = airplaneService.saveAirplane(existingAirplane);

        assertFalse(airplaneService.findByID(savedAirplane.getId() + 1).isPresent());

    }


}



