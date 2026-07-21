package com.frsystem.service;

import com.frsystem.dto.*;
import com.frsystem.dto.auth.LoginRequest;
import com.frsystem.dto.auth.LoginResponse;
import com.frsystem.enums.ReservationStatus;
import com.frsystem.exception.ConflictException;
import com.frsystem.repository.ReservationRepository;
import com.frsystem.security.JwtTokenProvider;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolationException;
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
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
public class ReservationServiceTest {
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

    private void loginAndAddToSecurityContext() {
        LoginResponse response = authService.login(new LoginRequest("admin@frsystem.com", "Admin123!"));
        String token = response.getToken();

        String email = jwtTokenProvider.extractEmail(token);
        String role = jwtTokenProvider.extractRole(token);
        Long userId = jwtTokenProvider.extractUserId(token);

        var authorities = Collections.singletonList(new SimpleGrantedAuthority(role));
        var authentication = new UsernamePasswordAuthenticationToken(email, userId, authorities);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private AirportResponse addTestDepartureAirport() {
        return airportService.saveAirport(new AirportRequest("ESB", "Esenboğa", "Türkiye", "Ankara"));
    }

    private AirportResponse addTestArrivalAirport() {
        return airportService.saveAirport(new AirportRequest("IST", "Istanbul Havalimanı", "Türkiye", "Istanbul"));
    }

    private AirplaneResponse addTestAirplane() {
        return airplaneService.saveAirplane(new AirplaneRequest("TC-123", "THY", "BOEING", 50));
    }

    private FlightResponse addTestFlight() {
        Long departureId = addTestDepartureAirport().getId();
        Long arrivalId = addTestArrivalAirport().getId();
        Long airplaneId = addTestAirplane().getId();
        LocalDateTime now = LocalDateTime.now();
        return flightService.saveFlight(new FlightRequest("TC123", airplaneId, departureId, arrivalId, now.plusDays(10), now.plusDays(11)));

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

    //TODO: RELATIONSHIPLI ENTITYLERDE VAROLMAYAN ID İÇEREN BODY OLAYINI KONTROL ET.
    @ParameterizedTest
    @MethodSource("provideInvalidReservations")
    void shouldThrowExceptionWhenReservationRequestViolatesValidation(ReservationRequest invalidReservationRequest) {

        assertThrows(ConstraintViolationException.class, () -> {
            reservationService.makeReservation(invalidReservationRequest);
        });
    }

    @Test
    void shouldMakeReservationCorrectly() {
        loginAndAddToSecurityContext();
        FlightResponse testFlight = addTestFlight();
        ReservationRequest reservationRequest = new ReservationRequest(testFlight.getId(), "A10");

        ReservationResponse reservationResponse = reservationService.makeReservation(reservationRequest);
        assertEquals("admin@frsystem.com", reservationResponse.getUser().getEmail());
        assertEquals(ReservationStatus.CONFIRMED, reservationResponse.getStatus());
    }


    @Test
    void shouldThrowExceptionWhenSameSeatNumberOnSameFlightTriedToBeBooked() {
        loginAndAddToSecurityContext();
        FlightResponse testFlight = addTestFlight();
        ReservationRequest reservationRequest = new ReservationRequest(testFlight.getId(), "A10");
        ReservationRequest reservationRequestWithSameSeat = new ReservationRequest(testFlight.getId(), "A10");

        reservationService.makeReservation(reservationRequest);

        Exception exception = assertThrows(ConflictException.class, () -> {
            reservationService.makeReservation(reservationRequestWithSameSeat);
        });

        assertEquals("This seat is occupied on that flight!", exception.getMessage());

    }


}
