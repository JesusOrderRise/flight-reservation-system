package com.frsystem.service;

import com.frsystem.dto.*;
import com.frsystem.enums.FlightStatus;
import com.frsystem.exception.ConflictException;
import com.frsystem.exception.ResourceNotFoundException;
import com.frsystem.repository.FlightRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
public class FlightServiceTest {

    //TODO: YANLIŞ FORMAT TESTLERİ SERVİS TESTİ DEĞİL CONTROLLER(INTEGRATION) TESTİ OLACAK.

    @Autowired
    private FlightService flightService;

    //
    @Autowired
    private AirplaneService airplaneService;
    @Autowired
    private AirportService airportService;

    @Autowired
    private FlightRepository flightRepository;


    //is service loaded?
    @Test
    void serviceShouldLoadContext() {
        assertNotNull(flightService);
    }

    private AirplaneResponse createTestAirplane() {
        AirplaneRequest newAirplane = new AirplaneRequest("TC-123", "THY", "Boeing", 20);
        return airplaneService.saveAirplane(newAirplane);
    }

    private AirplaneResponse createTestAirplane2() {
        AirplaneRequest newAirplane = new AirplaneRequest("TC-1071", "THY", "Boeing", 20);
        return airplaneService.saveAirplane(newAirplane);
    }

    private AirportResponse createTestDepartureAirport() {
        AirportRequest newDepartureAirport = new AirportRequest("ESB", "Esenboğa", "Türkiye", "Ankara");
        return airportService.saveAirport(newDepartureAirport);
    }

    private AirportResponse createTestArrivalAirport() {
        AirportRequest newArrivalAirport = new AirportRequest("IST", "İstanbul Havalimanı", "Türkiye", "İstanbul");
        return airportService.saveAirport(newArrivalAirport);
    }


    // All possible invalid scenarios.
    private static Stream<FlightRequest> provideInvalidFlights() {
        LocalDateTime now = LocalDateTime.now();
        Long placeHolder = 1L;

        return Stream.of(
                // @NotBlank/NotNull Controls
                new FlightRequest(null, placeHolder, placeHolder, placeHolder, now.plusDays(10), now.plusDays(11)),
                new FlightRequest("TC123", null, placeHolder, placeHolder, now.plusDays(10), now.plusDays(11)),
                new FlightRequest("TC123", placeHolder, null, placeHolder, now.plusDays(10), now.plusDays(11)),
                new FlightRequest("TC123", placeHolder, placeHolder, null, now.plusDays(10), now.plusDays(11)),
                new FlightRequest("TC123", placeHolder, placeHolder, placeHolder, null, now.plusDays(11)),
                new FlightRequest("TC123", placeHolder, placeHolder, placeHolder, now.plusDays(10), null),
                new FlightRequest(null, null, null, null, null, null),
                // @Future Controls
                new FlightRequest("TC123", placeHolder, placeHolder, placeHolder, now.minusDays(10), now.plusDays(11)),
                new FlightRequest(null, placeHolder, placeHolder, placeHolder, now.plusDays(10), now.minusDays(11))
        );
    }

    @ParameterizedTest
    @MethodSource("provideInvalidFlights")
    void shouldThrowExceptionWhenSavingInvalidFlight(FlightRequest invalidFlightRequest) {
        if (invalidFlightRequest.getAirplaneId() != null) {
            invalidFlightRequest.setAirplaneId(createTestAirplane().getId());
        }
        if (invalidFlightRequest.getDepartureAirportId() != null) {
            invalidFlightRequest.setDepartureAirportId(createTestDepartureAirport().getId());
        }
        if (invalidFlightRequest.getArrivalAirportId() != null) {
            invalidFlightRequest.setArrivalAirportId(createTestArrivalAirport().getId());
        }

        assertThrows(ConstraintViolationException.class, () -> {
            flightService.saveFlight(invalidFlightRequest);
        });
    }

    @Test
    void shouldThrowExceptionWhenArrivalTimeIsBeforeDepartureTime() {

        FlightRequest invalidTimeRequest = new FlightRequest();

        invalidTimeRequest.setFlightNumber("TC123");
        invalidTimeRequest.setAirplaneId(createTestAirplane().getId());
        invalidTimeRequest.setDepartureAirportId(createTestDepartureAirport().getId());
        invalidTimeRequest.setArrivalAirportId(createTestArrivalAirport().getId());

        LocalDateTime now = LocalDateTime.now();

        invalidTimeRequest.setDepartureTime(now.plusDays(10));
        invalidTimeRequest.setArrivalTime(now.plusDays(9));

        ConstraintViolationException exception = assertThrows(ConstraintViolationException.class, () -> {
            flightService.saveFlight(invalidTimeRequest);
        });

        String actualMessage = exception.getConstraintViolations()
                .iterator().next().getMessage();

        assertEquals("Arrival Time Must Be After Departure Time.", actualMessage);


    }

    @Test
    void shouldThrowExceptionWhenDepartureAndArrivalAirportsAreSame() {
        FlightRequest invalidAirportsRequest = new FlightRequest();

        AirportResponse newAirport = createTestDepartureAirport();

        invalidAirportsRequest.setFlightNumber("TC123");
        invalidAirportsRequest.setAirplaneId(createTestAirplane().getId());
        invalidAirportsRequest.setDepartureAirportId(newAirport.getId());
        invalidAirportsRequest.setArrivalAirportId(newAirport.getId());
        invalidAirportsRequest.setDepartureTime(LocalDateTime.now().plusDays(10));
        invalidAirportsRequest.setArrivalTime(LocalDateTime.now().plusDays(11));

        ConstraintViolationException exception = assertThrows(ConstraintViolationException.class, () -> {
            flightService.saveFlight(invalidAirportsRequest);
        });

        String actualMessage = exception.getConstraintViolations()
                .iterator().next().getMessage();

        assertEquals("Departure and Arrival airports cannot be the same!", actualMessage);

    }

    @Test
    void shouldThrowExceptionWhenSavingWithSameFlightNumber() {
        AirplaneResponse testAirplane = createTestAirplane();
        AirportResponse testDepartureAirport = createTestDepartureAirport();
        AirportResponse testArrivalAirport = createTestArrivalAirport();

        LocalDateTime now = LocalDateTime.now();

        flightService.saveFlight(new FlightRequest("TC123", testAirplane.getId(), testDepartureAirport.getId(), testArrivalAirport.getId(), now.plusDays(10), now.plusDays(11)));

        Exception exception = assertThrows(ConflictException.class, () -> {
            flightService.saveFlight(new FlightRequest("TC123", testAirplane.getId(), testDepartureAirport.getId(), testArrivalAirport.getId(), now.plusDays(10), now.plusDays(11)));
        });

        assertEquals("There is an already existing flight with same flight number!", exception.getMessage());
    }

    @Test
    void shouldSuccessfullyCreateFlight() {
        AirplaneResponse testAirplane = createTestAirplane();
        AirportResponse testDepartureAirport = createTestDepartureAirport();
        AirportResponse testArrivalAirport = createTestArrivalAirport();

        LocalDateTime now = LocalDateTime.now();

        FlightResponse saved = flightService.saveFlight(new FlightRequest("TC123", testAirplane.getId(), testDepartureAirport.getId(), testArrivalAirport.getId(), now.plusDays(10), now.plusDays(11)));

        assertNotNull(saved.getId());
        assertNotNull(saved.getLastUpdate());
        assertEquals("TC-123", saved.getAirplane().getTailNumber());
        assertEquals(testAirplane.getId(), saved.getAirplane().getId());
    }

    @Test
    void shouldGetAllEntries() {
        AirplaneResponse testAirplane1 = createTestAirplane();
        AirplaneResponse testAirplane2 = createTestAirplane2();
        AirportResponse testDepartureAirport = createTestDepartureAirport();
        AirportResponse testArrivalAirport = createTestArrivalAirport();

        LocalDateTime now = LocalDateTime.now();

        FlightResponse saved1 = flightService.saveFlight(new FlightRequest("TC123", testAirplane1.getId(), testDepartureAirport.getId(), testArrivalAirport.getId(), now.plusDays(10), now.plusDays(11)));
        FlightResponse saved2 = flightService.saveFlight(new FlightRequest("TC321", testAirplane2.getId(), testDepartureAirport.getId(), testArrivalAirport.getId(), now.plusDays(10), now.plusDays(11)));

        List<FlightResponse> returnEntities = flightService.getAll();

        //Did it catch exactly 2 values it supposed to catch?
        assertEquals(2, returnEntities.size());

        List<String> flightNumbers = returnEntities.stream()
                .map(FlightResponse::getFlightNumber)
                .toList();

        List<String> AirplaneTailNumbers = returnEntities.stream()
                .map(FlightResponse::getAirplane)
                .map(AirplaneResponse::getTailNumber)
                .toList();

        assertTrue(flightNumbers.contains("TC123"), "This should be Included");
        assertTrue(flightNumbers.contains("TC321"), "This should be Included");
        assertTrue(AirplaneTailNumbers.contains("TC-123"), "This should be Included");
        assertTrue(AirplaneTailNumbers.contains("TC-1071"), "This should be Included");

    }

    @Test
    void shouldGetCorrespondingEntriesWhenSearched() {
        AirplaneResponse testAirplane1 = createTestAirplane();
        AirplaneResponse testAirplane2 = createTestAirplane2();
        AirportResponse testDepartureAirport = createTestDepartureAirport();
        AirportResponse testArrivalAirport = createTestArrivalAirport();

        LocalDateTime now = LocalDateTime.now();

        FlightResponse saved1 = flightService.saveFlight(new FlightRequest("TC123", testAirplane1.getId(), testDepartureAirport.getId(), testArrivalAirport.getId(), now.plusDays(10), now.plusDays(11)));
        FlightResponse saved2 = flightService.saveFlight(new FlightRequest("TC321", testAirplane2.getId(), testDepartureAirport.getId(), testArrivalAirport.getId(), now.plusDays(10), now.plusDays(11)));

        FlightRequest searchBody = new FlightRequest("tc3", null, null, null, null, null);
        List<FlightResponse> returnEntities = flightService.searchWithParameters(searchBody);

        //Did it catch exactly 2 values it supposed to catch?
        assertEquals(1, returnEntities.size());

        List<String> flightNumbers = returnEntities.stream()
                .map(FlightResponse::getFlightNumber)
                .toList();

        List<String> AirplaneTailNumbers = returnEntities.stream()
                .map(FlightResponse::getAirplane)
                .map(AirplaneResponse::getTailNumber)
                .toList();

        assertFalse(flightNumbers.contains("TC123"), "This should NOT be Included");
        assertTrue(flightNumbers.contains("TC321"), "This should be Included");
        assertFalse(AirplaneTailNumbers.contains("TC-123"), "This should NOT be Included");
        assertTrue(AirplaneTailNumbers.contains("TC-1071"), "This should NOT be Included");

    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonExistingId() {
        AirplaneResponse testAirplane1 = createTestAirplane();
        AirportResponse testDepartureAirport = createTestDepartureAirport();
        AirportResponse testArrivalAirport = createTestArrivalAirport();

        LocalDateTime now = LocalDateTime.now();

        FlightResponse saved1 = flightService.saveFlight(new FlightRequest("TC123", testAirplane1.getId(), testDepartureAirport.getId(), testArrivalAirport.getId(), now.plusDays(10), now.plusDays(11)));
        Long updateId = saved1.getId();

        Exception exception = assertThrows(ResourceNotFoundException.class, () -> {
            flightService.updateFlightStatus(updateId + 1, FlightStatus.CANCELLED);
        });

        assertEquals("There is no Flight with this ID!", exception.getMessage());
    }

    @Test
    void shouldSuccessfullyUpdateStatusAndChangeLastUpdate() {
        AirplaneResponse testAirplane1 = createTestAirplane();
        AirportResponse testDepartureAirport = createTestDepartureAirport();
        AirportResponse testArrivalAirport = createTestArrivalAirport();

        LocalDateTime now = LocalDateTime.now();

        FlightResponse saved1 = flightService.saveFlight(new FlightRequest("TC123", testAirplane1.getId(), testDepartureAirport.getId(), testArrivalAirport.getId(), now.plusDays(10), now.plusDays(11)));
        Long updateId = saved1.getId();


        FlightResponse updatedFlight = flightService.updateFlightStatus(updateId, FlightStatus.CANCELLED);


        assertEquals(FlightStatus.CANCELLED, updatedFlight.getStatus());
        assertNotEquals(updatedFlight.getLastUpdate(), saved1.getLastUpdate());

    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistingFlight() {
        AirplaneResponse testAirplane1 = createTestAirplane();
        AirportResponse testDepartureAirport = createTestDepartureAirport();
        AirportResponse testArrivalAirport = createTestArrivalAirport();

        LocalDateTime now = LocalDateTime.now();

        FlightResponse saved1 = flightService.saveFlight(new FlightRequest("TC123", testAirplane1.getId(), testDepartureAirport.getId(), testArrivalAirport.getId(), now.plusDays(10), now.plusDays(11)));
        Long deleteId = saved1.getId();

        Exception exception = assertThrows(ResourceNotFoundException.class, () -> {
            flightService.deleteFlightByID(deleteId + 1);
        });

        assertEquals("There is no Flight with this ID!", exception.getMessage());

    }

    @Test
    void shouldSuccessfullyDelete() {
        AirplaneResponse testAirplane1 = createTestAirplane();
        AirportResponse testDepartureAirport = createTestDepartureAirport();
        AirportResponse testArrivalAirport = createTestArrivalAirport();

        LocalDateTime now = LocalDateTime.now();

        FlightResponse saved1 = flightService.saveFlight(new FlightRequest("TC123", testAirplane1.getId(), testDepartureAirport.getId(), testArrivalAirport.getId(), now.plusDays(10), now.plusDays(11)));
        Long deleteId = saved1.getId();

        assertTrue(flightRepository.existsById(deleteId));


        flightService.deleteFlightByID(deleteId);


        assertFalse(flightRepository.existsById(deleteId));

    }

}
