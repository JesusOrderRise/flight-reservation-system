package com.frsystem.service;

import com.frsystem.config.AbstractIntegrationTest;
import com.frsystem.dto.*;
import com.frsystem.dto.auth.LoginRequest;
import com.frsystem.dto.auth.LoginResponse;
import com.frsystem.dto.auth.RegisterRequest;
import com.frsystem.enums.ReservationStatus;
import com.frsystem.exception.ConflictException;
import com.frsystem.exception.ResourceNotFoundException;
import com.frsystem.repository.ReservationRepository;
import com.frsystem.security.JwtTokenProvider;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
public class ReservationServiceTest extends AbstractIntegrationTest {
    @Autowired
    ReservationService reservationService;

    @Autowired
    ReservationRepository reservationRepository;

    @Autowired
    AuthService authService;

    @Autowired
    AirportService airportService;

    @Autowired
    AirplaneService airplaneService;

    @Autowired
    FlightService flightService;

    @Autowired
    JwtTokenProvider jwtTokenProvider;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private void loginAndAddToSecurityContext(String email, String password) {
        LoginResponse response = authService.login(new LoginRequest(email, password));
        String token = response.getToken();

        String extractedEmail = jwtTokenProvider.extractEmail(token);
        String role = jwtTokenProvider.extractRole(token);
        Long userId = jwtTokenProvider.extractUserId(token);

        var authorities = Collections.singletonList(new SimpleGrantedAuthority(role));
        var authentication = new UsernamePasswordAuthenticationToken(extractedEmail, userId, authorities);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private void registerTestPassenger(String firstName, String lastName, String email, String password) {
        authService.registerPassenger(new RegisterRequest(firstName, lastName, email, password));
    }

    private AirportResponse addTestDepartureAirport1() {
        return airportService.saveAirport(new AirportRequest("ESB", "Esenboğa", "Türkiye", "Ankara"));
    }

    private AirportResponse addTestArrivalAirport1() {
        return airportService.saveAirport(new AirportRequest("IST", "Istanbul Havalimanı", "Türkiye", "Istanbul"));
    }

    private AirplaneResponse addTestAirplane1() {
        return airplaneService.saveAirplane(new AirplaneRequest("TC-123", "THY", "BOEING", 50));
    }

    private AirportResponse addTestDepartureAirport2() {
        return airportService.saveAirport(new AirportRequest("ABC", "Esenboğa", "Türkiye", "Ankara"));
    }

    private AirportResponse addTestArrivalAirport2() {
        return airportService.saveAirport(new AirportRequest("CBA", "Istanbul Havalimanı", "Türkiye", "Istanbul"));
    }

    private AirplaneResponse addTestAirplane2() {
        return airplaneService.saveAirplane(new AirplaneRequest("TC-1071", "THY", "BOEING", 50));
    }

    private FlightResponse addTestFlight1(String flightNumber) {
        Long departureId = addTestDepartureAirport1().getId();
        Long arrivalId = addTestArrivalAirport1().getId();
        Long airplaneId = addTestAirplane1().getId();
        LocalDateTime now = LocalDateTime.now();
        return flightService.saveFlight(new FlightRequest(flightNumber, airplaneId, departureId, arrivalId, now.plusDays(10), now.plusDays(11)));

    }

    private FlightResponse addTestFlight2(String flightNumber) {
        Long departureId = addTestDepartureAirport2().getId();
        Long arrivalId = addTestArrivalAirport2().getId();
        Long airplaneId = addTestAirplane2().getId();
        LocalDateTime now = LocalDateTime.now();
        return flightService.saveFlight(new FlightRequest(flightNumber, airplaneId, departureId, arrivalId, now.plusDays(10), now.plusDays(11)));

    }


    @Test
    void serviceShouldLoadContext() {
        assertNotNull(reservationService);
    }

    private static Stream<ReservationRequest> provideInvalidReservations() {
        Long placeHolder = 1L;

        return Stream.of(
                // @NotBlank/NotNull Controls
                new ReservationRequest(null, "A10"),
                new ReservationRequest(placeHolder, null)

        );
    }

    //TODO: RELATIONSHIPLI ENTITYLERDE VAROLMAYAN ID İÇEREN BODY OLAYINI KONTROL ET. MAPPER İLE YAPILDI!!
    @ParameterizedTest
    @MethodSource("provideInvalidReservations")
    void shouldThrowExceptionWhenReservationRequestViolatesValidation(ReservationRequest invalidReservationRequest) {

        assertThrows(ConstraintViolationException.class, () -> {
            reservationService.makeReservation(invalidReservationRequest);
        });
    }

    @Test
    void shouldMakeReservationCorrectly() {
        loginAndAddToSecurityContext("admin@frsystem.com", "Admin123!");
        FlightResponse testFlight = addTestFlight1("TC123");
        ReservationRequest reservationRequest = new ReservationRequest(testFlight.getId(), "A10");

        ReservationResponse reservationResponse = reservationService.makeReservation(reservationRequest);
        assertEquals("admin@frsystem.com", reservationResponse.getUser().getEmail());
        assertEquals(ReservationStatus.CONFIRMED, reservationResponse.getStatus());
    }

    @Test
    void shouldMakeReservationForCorrectPassenger() {
        registerTestPassenger("Mahmut", "Tuncer", "passenger1@test.com", "Passenger123!");
        loginAndAddToSecurityContext("passenger1@test.com", "Passenger123!");

        FlightResponse testFlight = addTestFlight1("TC123");
        ReservationRequest request = new ReservationRequest(testFlight.getId(), "B12");

        ReservationResponse response = reservationService.makeReservation(request);

        assertEquals("passenger1@test.com", response.getUser().getEmail());
        assertEquals(ReservationStatus.CONFIRMED, response.getStatus());
    }


    @Test
    void shouldThrowExceptionWhenSameSeatNumberOnSameFlightTriedToBeBooked() {
        loginAndAddToSecurityContext("admin@frsystem.com", "Admin123!");
        FlightResponse testFlight = addTestFlight1("TC123");
        ReservationRequest reservationRequest = new ReservationRequest(testFlight.getId(), "A10");
        ReservationRequest reservationRequestWithSameSeat = new ReservationRequest(testFlight.getId(), "A10");

        reservationService.makeReservation(reservationRequest);

        Exception exception = assertThrows(ConflictException.class, () -> {
            reservationService.makeReservation(reservationRequestWithSameSeat);
        });

        assertEquals("This seat is occupied on that flight!", exception.getMessage());

    }

    @Test
    void shouldAllowSameSeatNumberOnDifferentFlights() {
        loginAndAddToSecurityContext("admin@frsystem.com", "Admin123!");

        FlightResponse flight1 = addTestFlight1("TC123");
        FlightResponse flight2 = addTestFlight2("123TC");

        reservationService.makeReservation(new ReservationRequest(flight1.getId(), "A10"));

        assertDoesNotThrow(() -> {
            reservationService.makeReservation(new ReservationRequest(flight2.getId(), "A10"));
        });
    }

    @Test
    void shouldAllowDifferentSeatNumbersOnSameFlight() {
        loginAndAddToSecurityContext("admin@frsystem.com", "Admin123!");
        FlightResponse testFlight = addTestFlight1("TC123");

        reservationService.makeReservation(new ReservationRequest(testFlight.getId(), "A10"));

        assertDoesNotThrow(() -> {
            reservationService.makeReservation(new ReservationRequest(testFlight.getId(), "A11"));
        });
    }

    @Test
    void shouldAllowDifferentUsersToBookDifferentSeatsOnSameFlight() {
        loginAndAddToSecurityContext("admin@frsystem.com", "Admin123!");
        FlightResponse testFlight = addTestFlight1("TC123");
        reservationService.makeReservation(new ReservationRequest(testFlight.getId(), "A10"));

        registerTestPassenger("Mahmut", "Tuncer", "passenger2@test.com", "Passenger123!");
        loginAndAddToSecurityContext("passenger2@test.com", "Passenger123!");

        assertDoesNotThrow(() -> {
            reservationService.makeReservation(new ReservationRequest(testFlight.getId(), "A11"));
        });
    }

    @Test
    void shouldThrowExceptionWhenFlightDoesNotExist() {
        loginAndAddToSecurityContext("admin@frsystem.com", "Admin123!");
        ReservationRequest request = new ReservationRequest(999999L, "A10");

        Exception exception = assertThrows(ResourceNotFoundException.class, () -> {
            reservationService.makeReservation(request);
        });

        assertEquals("Flight not found!", exception.getMessage());
    }

    //CANCELSELFRESERVATION TESTS
    @Test
    void shouldSoftDeleteByPassenger() {
        loginAndAddToSecurityContext("admin@frsystem.com", "Admin123!");
        FlightResponse testFlight = addTestFlight1("TC123");
        ReservationRequest reservationRequest = new ReservationRequest(testFlight.getId(), "A10");

        ReservationResponse reservation = reservationService.makeReservation(reservationRequest);
        long countBeforeCancel = reservationRepository.count();

        ReservationResponse cancelReservation = reservationService.cancelSelfReservation(reservation.getId());

        //DOES THE ENTRY STILL EXISTS
        assertTrue(reservationRepository.existsById(cancelReservation.getId()));
        assertEquals(countBeforeCancel, reservationRepository.count());

        //STATUS CHECK
        assertEquals(ReservationStatus.CANCELED, reservationRepository.findById(cancelReservation.getId()).get().getStatus());
    }

    @Test
    void shouldThrowExceptionWhenAPassengerTriesToDeleteAnothersReservation() {
        //User1 makes a reservation.
        loginAndAddToSecurityContext("admin@frsystem.com", "Admin123!");
        FlightResponse testFlight = addTestFlight1("TC123");
        ReservationRequest reservationRequest = new ReservationRequest(testFlight.getId(), "A10");

        ReservationResponse reservation = reservationService.makeReservation(reservationRequest);

        //Another User logins.
        registerTestPassenger("Mahmut", "Tuncer", "passenger2@test.com", "Passenger123!");
        loginAndAddToSecurityContext("passenger2@test.com", "Passenger123!");

        Exception exception = assertThrows(ResourceNotFoundException.class, () -> {
            reservationService.cancelSelfReservation(reservation.getId());
        });

        assertEquals("Reservation not found or you don't have permission!", exception.getMessage());

    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistingReservation() {
        loginAndAddToSecurityContext("admin@frsystem.com", "Admin123!");
        FlightResponse testFlight = addTestFlight1("TC123");
        ReservationRequest reservationRequest = new ReservationRequest(testFlight.getId(), "A10");

        ReservationResponse reservation = reservationService.makeReservation(reservationRequest);

        Exception exception = assertThrows(ResourceNotFoundException.class, () -> {
            reservationService.cancelSelfReservation(reservation.getId() + 1);
        });

        assertEquals("Reservation not found or you don't have permission!", exception.getMessage());
    }

    @Test
    void shouldSoftDeleteByAdmin() {
        loginAndAddToSecurityContext("admin@frsystem.com", "Admin123!");
        FlightResponse testFlight = addTestFlight1("TC123");
        ReservationRequest reservationRequest = new ReservationRequest(testFlight.getId(), "A10");

        ReservationResponse reservation = reservationService.makeReservation(reservationRequest);
        long countBeforeCancel = reservationRepository.count();

        ReservationResponse cancelReservation = reservationService.adminCancelReservation(reservation.getId());

        //DOES THE ENTRY STILL EXISTS
        assertTrue(reservationRepository.existsById(cancelReservation.getId()));
        assertEquals(countBeforeCancel, reservationRepository.count());

        //STATUS CHECK
        assertEquals(ReservationStatus.CANCELED, reservationRepository.findById(cancelReservation.getId()).get().getStatus());
    }

    @Test
    void adminShouldDeleteAnothersReservation() {
        //User1 makes a reservation.
        registerTestPassenger("Mahmut", "Tuncer", "passenger2@test.com", "Passenger123!");
        loginAndAddToSecurityContext("passenger2@test.com", "Passenger123!");
        FlightResponse testFlight = addTestFlight1("TC123");
        ReservationRequest reservationRequest = new ReservationRequest(testFlight.getId(), "A10");

        ReservationResponse reservation = reservationService.makeReservation(reservationRequest);
        long countBeforeCancel = reservationRepository.count();


        //Admin logins.
        loginAndAddToSecurityContext("admin@frsystem.com", "Admin123!");

        //Admin Cancels
        ReservationResponse cancelReservation = reservationService.adminCancelReservation(reservation.getId());

        //DOES THE ENTRY STILL EXISTS
        assertTrue(reservationRepository.existsById(cancelReservation.getId()));
        assertEquals(countBeforeCancel, reservationRepository.count());

        //STATUS CHECK
        assertEquals(ReservationStatus.CANCELED, reservationRepository.findById(cancelReservation.getId()).get().getStatus());

    }

    @Test
    void shouldThrowExceptionWhenAdminTriesToDeleteNonExistingReservation() {
        loginAndAddToSecurityContext("admin@frsystem.com", "Admin123!");
        FlightResponse testFlight = addTestFlight1("TC123");
        ReservationRequest reservationRequest = new ReservationRequest(testFlight.getId(), "A10");

        ReservationResponse reservation = reservationService.makeReservation(reservationRequest);

        Exception exception = assertThrows(ResourceNotFoundException.class, () -> {
            reservationService.adminCancelReservation(reservation.getId() + 1);
        });

        assertEquals("Reservation not found!", exception.getMessage());
    }

    //GETMYRESERVATIONS TESTS

    @Test
    void shouldReturnOnlyOwnReservationsForPassenger() {
        // Passenger1, makes 2 reservations for 2 different flights
        registerTestPassenger("Mahmut", "Tuncer", "passenger1@test.com", "Passenger123!");
        loginAndAddToSecurityContext("passenger1@test.com", "Passenger123!");
        FlightResponse flight1 = addTestFlight1("TC123");
        FlightResponse flight2 = addTestFlight2("123TC");
        reservationService.makeReservation(new ReservationRequest(flight1.getId(), "A10"));
        reservationService.makeReservation(new ReservationRequest(flight2.getId(), "A11"));

        // Passenger2, makes another reservation
        registerTestPassenger("Ahmet", "Yılmaz", "passenger2@test.com", "Passenger123!");
        loginAndAddToSecurityContext("passenger2@test.com", "Passenger123!");
        reservationService.makeReservation(new ReservationRequest(flight1.getId(), "A12"));

        // Passenger1 logins and searches for his own reservations.
        loginAndAddToSecurityContext("passenger1@test.com", "Passenger123!");
        List<ReservationResponse> myReservations = reservationService.getMyReservations();

        assertEquals(2, myReservations.size());
        assertTrue(myReservations.stream().allMatch(r -> r.getUser().getEmail().equals("passenger1@test.com")));
    }

    @Test
    void shouldReturnEmptyListWhenPassengerHasNoReservations() {
        registerTestPassenger("Mahmut", "Tuncer", "passenger1@test.com", "Passenger123!");
        loginAndAddToSecurityContext("passenger1@test.com", "Passenger123!");

        List<ReservationResponse> myReservations = reservationService.getMyReservations();

        assertTrue(myReservations.isEmpty());
    }

    //GETALLRESERVATIONS TESTS

    @Test
    void shouldReturnAllReservationsForAdmin() {
        registerTestPassenger("Mahmut", "Tuncer", "passenger1@test.com", "Passenger123!");
        loginAndAddToSecurityContext("passenger1@test.com", "Passenger123!");
        FlightResponse flight1 = addTestFlight1("TC123");
        reservationService.makeReservation(new ReservationRequest(flight1.getId(), "A10"));

        registerTestPassenger("Ahmet", "Yılmza", "passenger2@test.com", "Passenger123!");
        loginAndAddToSecurityContext("passenger2@test.com", "Passenger123!");
        reservationService.makeReservation(new ReservationRequest(flight1.getId(), "A11"));

        loginAndAddToSecurityContext("admin@frsystem.com", "Admin123!");
        List<ReservationResponse> allReservations = reservationService.getAllReservations();

        assertEquals(2, allReservations.size());

        List<String> emails = allReservations.stream()
                .map(r -> r.getUser().getEmail())
                .toList();

        assertTrue(emails.contains("passenger1@test.com"));
        assertTrue(emails.contains("passenger2@test.com"));
    }

    @Test
    void shouldReturnEmptyListWhenNoReservationsExist() {
        loginAndAddToSecurityContext("admin@frsystem.com", "Admin123!");

        List<ReservationResponse> allReservations = reservationService.getAllReservations();

        assertTrue(allReservations.isEmpty());
    }

}