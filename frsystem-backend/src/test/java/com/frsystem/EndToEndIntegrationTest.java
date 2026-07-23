package com.frsystem;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.frsystem.config.AbstractIntegrationTest;
import com.frsystem.dto.auth.LoginRequest;
import com.frsystem.dto.auth.RegisterRequest;
import com.frsystem.enums.FlightStatus;
import com.frsystem.repository.ReservationRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Fail.fail;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class EndToEndIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private ReservationRepository reservationRepository;

    @Value("${mailhog.web.host:localhost}")
    private String mailhogWebHost;

    @Value("${mailhog.web.port:8025}")
    private String mailhogWebPort;

    private final RestTemplate restTemplate = new RestTemplate();

    @BeforeEach
    void setup() {
        restTemplate.delete("http://" + mailhogWebHost + ":" + mailhogWebPort + "/api/v1/messages");

    }


    private String registerPassengerAndLogin(String firstName, String lastName, String email, String password) throws Exception {
        RegisterRequest registerRequest = new RegisterRequest(firstName, lastName, email, password);

        //Register
        mockMvc.perform(post("/api/v1/auth/register/passenger")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk());


        // Login
        LoginRequest loginRequest = new LoginRequest(email, password);
        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        return root.get("token").asText();
    }

    private String registerAdminAndLogin(String firstName, String lastName, String email, String password, String token) throws Exception {
        RegisterRequest registerRequest = new RegisterRequest(firstName, lastName, email, password);

        //Register
        mockMvc.perform(post("/api/v1/auth/register/passenger")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk());


        // Login
        LoginRequest loginRequest = new LoginRequest(email, password);
        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        return root.get("token").asText();
    }

    // Admin login helper
    private String getAdminToken(String email, String password) throws Exception {
        LoginRequest adminLoginRequest = new LoginRequest(email, password);

        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(adminLoginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        return root.get("token").asText();


    }

    //AUTHFLOW
    @Test
    void shouldCompleteAuthFlowSuccessfullyForPassenger() throws Exception {
        String token = registerPassengerAndLogin("Test123", "Test123", "auth@test.com", "Password123!");
        assertTrue(token != null && token.length() > 20);
    }

    @Test
    void shouldCompleteAuthFlowSuccessfullyForAdmin() throws Exception {
        String seedAdminToken = getAdminToken("admin@frsystem.com", "Admin123!");
        String token = registerAdminAndLogin("Admin123", "Admin123", "admin@test.com", "Password123!", seedAdminToken);
        assertTrue(token != null && token.length() > 20);
    }

    @Test
    void shouldThrow403ForUnauthorizedAdminRegister() throws Exception {

        String passengerToken = registerPassengerAndLogin("Passenger", "Passenger", "passenger@test.com", "Pass123!");

        RegisterRequest registerRequest = new RegisterRequest("Hacker", "User", "hacker@test.com", "Hacker123!");

        // with passenger token
        mockMvc.perform(post("/api/v1/auth/register/admin")
                        .header("Authorization", "Bearer " + passengerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isForbidden());

        // without token
        mockMvc.perform(post("/api/v1/auth/register/admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isForbidden());
    }

    //RBAC
    @Test
    void shouldEnforceRbacRulesForAirportAndAirplaneAndFlight() throws Exception {
        String passengerToken = registerPassengerAndLogin("passenger", "passenger", "passenger_rbac@test.com", "Pass123!");
        String adminToken = getAdminToken("admin@frsystem.com", "Admin123!");

        String airplaneJson = """
                {
                    "tailNumber": "TC-RBAC",
                    "airline": "THY",
                    "model": "Boeing 737",
                    "capacity": 150
                }
                """;

        String airportJson = """
                {
                    "iataCode": "IST",
                    "name": "İstanbul Havalimanı",
                    "city": "İstanbul",
                    "country": "Türkiye"
                }
                """;

        // ----- AIRPLANE TESTS -----

        // Passenger adds airplane -> 403 FORBIDDEN
        mockMvc.perform(post("/api/v1/airplanes")
                        .header("Authorization", "Bearer " + passengerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(airplaneJson))
                .andExpect(status().isForbidden());

        // Admin adds airplane -> 200 OK
        MvcResult airplaneResult = mockMvc.perform(post("/api/v1/airplanes")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(airplaneJson))
                .andExpect(status().isOk())
                .andReturn();

        Long airplaneId = null;
        try {
            airplaneId = objectMapper.readTree(airplaneResult.getResponse().getContentAsString())
                    .get("id").asLong();
        } catch (Exception e) {
            fail("Airplane ID couldnt be fetched: " + e.getMessage());
        }

        // Passenger reads airplanes -> 200 OK
        mockMvc.perform(get("/api/v1/airplanes")
                        .header("Authorization", "Bearer " + passengerToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // Passenger updates airplane -> 403 FORBIDDEN
        String updateAirplaneJson = """
                {
                    "tailNumber": "TC-RBAC-UPDATED",
                    "airline": "Pegasus",
                    "model": "Airbus A320",
                    "capacity": 180
                }
                """;

        mockMvc.perform(put("/api/v1/airplanes/" + airplaneId)
                        .header("Authorization", "Bearer " + passengerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateAirplaneJson))
                .andExpect(status().isForbidden());

        // Admin updates airplane -> 200 OK
        mockMvc.perform(put("/api/v1/airplanes/" + airplaneId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateAirplaneJson))
                .andExpect(status().isOk());

        // Passenger deletes airplane -> 403 FORBIDDEN
        mockMvc.perform(delete("/api/v1/airplanes/" + airplaneId)
                        .header("Authorization", "Bearer " + passengerToken))
                .andExpect(status().isForbidden());

        // ----- AIRPORT TESTS -----

        // Passenger adds airport -> 403 FORBIDDEN
        mockMvc.perform(post("/api/v1/airports")
                        .header("Authorization", "Bearer " + passengerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(airportJson))
                .andExpect(status().isForbidden());

        // Admin adds airport -> 200 OK
        MvcResult airportResult = mockMvc.perform(post("/api/v1/airports")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(airportJson))
                .andExpect(status().isOk())
                .andReturn();

        Long airportId = null;
        try {
            airportId = objectMapper.readTree(airportResult.getResponse().getContentAsString())
                    .get("id").asLong();
        } catch (Exception e) {
            fail("Airport ID couldnt be fetched: " + e.getMessage());
        }

        // Passenger reads airports -> 200 OK
        mockMvc.perform(get("/api/v1/airports")
                        .header("Authorization", "Bearer " + passengerToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // Passenger updates airport -> 403 FORBIDDEN
        String updateAirportJson = """
                {
                    "iataCode": "IST",
                    "name": "İstanbul Yeni Havalimanı",
                    "city": "İstanbul",
                    "country": "Türkiye"
                }
                """;

        mockMvc.perform(put("/api/v1/airports/" + airportId)
                        .header("Authorization", "Bearer " + passengerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateAirportJson))
                .andExpect(status().isForbidden());

        // Admin updates airport -> 200 OK
        mockMvc.perform(put("/api/v1/airports/" + airportId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateAirportJson))
                .andExpect(status().isOk());

        // Passenger deletes airport -> 403 FORBIDDEN
        mockMvc.perform(delete("/api/v1/airports/" + airportId)
                        .header("Authorization", "Bearer " + passengerToken))
                .andExpect(status().isForbidden());

        // ----- FLIGHT TESTS -----

        String flightAirplaneJson = """
                {
                    "tailNumber": "TC-FLIGHT",
                    "airline": "THY",
                    "model": "Boeing 737-800",
                    "capacity": 180
                }
                """;

        MvcResult flightAirplaneResult = mockMvc.perform(post("/api/v1/airplanes")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(flightAirplaneJson))
                .andExpect(status().isOk())
                .andReturn();

        Long flightAirplaneId = null;
        try {
            flightAirplaneId = objectMapper.readTree(flightAirplaneResult.getResponse().getContentAsString())
                    .get("id").asLong();
        } catch (Exception e) {
            fail("Flight Airplane ID couldnt be fetched: " + e.getMessage());
        }

        String departureAirportJson = """
                {
                    "iataCode": "SAW",
                    "name": "Sabiha Gökçen Havalimanı",
                    "city": "İstanbul",
                    "country": "Türkiye"
                }
                """;

        MvcResult departureAirportResult = mockMvc.perform(post("/api/v1/airports")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(departureAirportJson))
                .andExpect(status().isOk())
                .andReturn();

        Long departureAirportId = null;
        try {
            departureAirportId = objectMapper.readTree(departureAirportResult.getResponse().getContentAsString())
                    .get("id").asLong();
        } catch (Exception e) {
            fail("Departure Airport ID couldnt be fetched: " + e.getMessage());
        }

        String arrivalAirportJson = """
                {
                    "iataCode": "WAS",
                    "name": "Sabiha Gökçen Havalimanı",
                    "city": "İstanbul",
                    "country": "Türkiye"
                }
                """;

        MvcResult arrivalAirportResult = mockMvc.perform(post("/api/v1/airports")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(arrivalAirportJson))
                .andExpect(status().isOk())
                .andReturn();

        Long arrivalAirportId = null;
        try {
            arrivalAirportId = objectMapper.readTree(arrivalAirportResult.getResponse().getContentAsString())
                    .get("id").asLong();
        } catch (Exception e) {
            fail("Arrival Airport ID couldnt be fetched: " + e.getMessage());
        }

        // Flight JSON
        String flightJson = String.format("""
                {
                    "flightNumber": "TC-TC-TC",
                    "airplaneId": %d,
                    "departureAirportId": %d,
                    "arrivalAirportId": %d,
                    "departureTime": "2050-08-23 10:00",
                    "arrivalTime": "2050-08-23 12:00"
                }
                """, flightAirplaneId, departureAirportId, arrivalAirportId);

        // Passenger adds flight -> 403 FORBIDDEN
        mockMvc.perform(post("/api/v1/flights")
                        .header("Authorization", "Bearer " + passengerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(flightJson))
                .andExpect(status().isForbidden());

        // Admin adds flight -> 200 OK
        MvcResult flightResult = mockMvc.perform(post("/api/v1/flights")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(flightJson))
                .andExpect(status().isOk())
                .andReturn();

        Long flightId = null;
        try {
            flightId = objectMapper.readTree(flightResult.getResponse().getContentAsString())
                    .get("id").asLong();
        } catch (Exception e) {
            fail("Flight ID couldnt be fetched: " + e.getMessage());
        }

        // Passenger reads flights -> 200 OK
        mockMvc.perform(get("/api/v1/flights")
                        .header("Authorization", "Bearer " + passengerToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // Passenger updates flight -> 403 FORBIDDEN
        mockMvc.perform(put("/api/v1/flights/" + flightId + "/status")
                        .header("Authorization", "Bearer " + passengerToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());

        // Admin updates flight -> 200 OK
        mockMvc.perform(put("/api/v1/flights/" + flightId + "/status")
                        .param("status", FlightStatus.CANCELED.name())
                        .header("Authorization", "Bearer " + adminToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // Passenger deletes flight -> 403 FORBIDDEN
        mockMvc.perform(delete("/api/v1/flights/" + flightId)
                        .header("Authorization", "Bearer " + passengerToken))
                .andExpect(status().isForbidden());

        // Admin deletes flight -> 200 OK
        mockMvc.perform(delete("/api/v1/flights/" + flightId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());
    }

    //Reservation Lifecycle
    @Test
    void shouldExecuteFullReservationLifecycle() throws Exception {
        //SEED ADMIN LOGIN
        String adminToken = getAdminToken("admin@frsystem.com", "Admin123!");

        // ADMIN CREATES AIRPLANE
        String airplaneJson = """
                {
                    "tailNumber": "TC-LIFE",
                    "airline": "THY",
                    "model": "Boeing 737-800",
                    "capacity": 180
                }
                """;

        MvcResult airplaneResult = null;
        try {
            airplaneResult = mockMvc.perform(post("/api/v1/airplanes")
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(airplaneJson))
                    .andExpect(status().isOk())
                    .andReturn();
        } catch (Exception e) {
            fail("Airplane couldnt be created: " + e.getMessage());
        }

        Long airplaneId = null;
        try {
            airplaneId = objectMapper.readTree(airplaneResult.getResponse().getContentAsString())
                    .get("id").asLong();
        } catch (Exception e) {
            fail("Airplane ID couldnt be fetched: " + e.getMessage());
        }

        //ADMIN CREATES AIRPORTS
        String departureAirportJson = """
                {
                    "iataCode": "IST",
                    "name": "İstanbul Havalimanı",
                    "city": "İstanbul",
                    "country": "Türkiye"
                }
                """;

        MvcResult departureAirportResult = null;
        try {
            departureAirportResult = mockMvc.perform(post("/api/v1/airports")
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(departureAirportJson))
                    .andExpect(status().isOk())
                    .andReturn();
        } catch (Exception e) {
            fail("Departure Airport couldnt be created: " + e.getMessage());
        }

        Long departureAirportId = null;
        try {
            departureAirportId = objectMapper.readTree(departureAirportResult.getResponse().getContentAsString())
                    .get("id").asLong();
        } catch (Exception e) {
            fail("Departure Airport ID couldnt be fetched: " + e.getMessage());
        }

        String arrivalAirportJson = """
                {
                    "iataCode": "SAW",
                    "name": "Sabiha Gökçen Havalimanı",
                    "city": "İstanbul",
                    "country": "Türkiye"
                }
                """;

        MvcResult arrivalAirportResult = null;
        try {
            arrivalAirportResult = mockMvc.perform(post("/api/v1/airports")
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(arrivalAirportJson))
                    .andExpect(status().isOk())
                    .andReturn();
        } catch (Exception e) {
            fail("Arrival Airport couldnt be created: " + e.getMessage());
        }

        Long arrivalAirportId = null;
        try {
            arrivalAirportId = objectMapper.readTree(arrivalAirportResult.getResponse().getContentAsString())
                    .get("id").asLong();
        } catch (Exception e) {
            fail("Arrival Airport ID couldnt be fetched: " + e.getMessage());
        }

        //ADMIN CREATES FLIGHT
        String departureTime = "2050-08-23 10:00";
        String arrivalTime = "2050-08-23 12:30";

        String flightJson = String.format("""
                {
                    "flightNumber": "TC-TC-TC",
                    "airplaneId": %d,
                    "departureAirportId": %d,
                    "arrivalAirportId": %d,
                    "departureTime": "%s",
                    "arrivalTime": "%s"
                }
                """, airplaneId, departureAirportId, arrivalAirportId, departureTime, arrivalTime);

        MvcResult flightResult = null;
        try {
            flightResult = mockMvc.perform(post("/api/v1/flights")
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(flightJson))
                    .andExpect(status().isOk())
                    .andReturn();
        } catch (Exception e) {
            fail("Flight couldnt be created: " + e.getMessage());
        }

        Long flightId = null;
        try {
            flightId = objectMapper.readTree(flightResult.getResponse().getContentAsString())
                    .get("id").asLong();
        } catch (Exception e) {
            fail("Flight ID couldnt be fetched: " + e.getMessage());
        }

        // PASSENGERS REGISTER AND LOGINS
        String passenger1Token = registerPassengerAndLogin("Ahmet", "Yılmaz", "ahmet@test.com", "Pass123!");
        String passenger2Token = registerPassengerAndLogin("Mehmet", "Demir", "mehmet@test.com", "Pass123!");

        // RESERVATIONS
        String seatToBook = "14A";

        String reservationPayload = String.format("""
                {
                    "flightId": %d,
                    "seatNumber": "%s"
                }
                """, flightId, seatToBook);

        // AHMET RESERVES THE SEAT
        MvcResult reservationResult = null;
        try {
            reservationResult = mockMvc.perform(post("/api/v1/reservations")
                            .header("Authorization", "Bearer " + passenger1Token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(reservationPayload))
                    .andExpect(status().isOk())
                    .andReturn();
        } catch (Exception e) {
            fail("Ahmet couldnt book: " + e.getMessage());
        }

        Long ahmetReservationId = null;
        try {
            ahmetReservationId = objectMapper.readTree(reservationResult.getResponse().getContentAsString())
                    .get("id").asLong();
        } catch (Exception e) {
            fail("Ahmet rezervation ID couldnt be fetched: " + e.getMessage());
        }

        // MAILHOG TEST FOR AHMET
        try {
            String mailhogResponse = restTemplate.getForObject(
                    "http://" + mailhogWebHost + ":" + mailhogWebPort + "/api/v2/messages",
                    String.class);
            assertTrue(mailhogResponse.contains("ahmet@test.com"));
        } catch (Exception e) {
            fail("MAIL NOT SENT for ahmet: " + e.getMessage());
        }

        // MEHMET TRIES TO BOOK SAME SEAT -> 409 CONFLICT
        try {
            mockMvc.perform(post("/api/v1/reservations")
                            .header("Authorization", "Bearer " + passenger2Token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(reservationPayload))
                    .andExpect(status().isConflict());
        } catch (Exception e) {
            fail("MEHMET SHOULDNT BOOK SAME SEAT: " + e.getMessage());
        }

        // AHMET CANCELS HIS RESERVATION
        try {
            mockMvc.perform(patch("/api/v1/reservations/" + ahmetReservationId + "/cancel")
                            .header("Authorization", "Bearer " + passenger1Token))
                    .andExpect(status().isOk());
        } catch (Exception e) {
            fail("Ahmet COULDNT CANCEL HIS RESERVATION: " + e.getMessage());
        }

        // NOW MEHMET IS ABLE TO BOOK THE SEAT SINCE AHMET CANCELLED
        MvcResult secondReservationResult = null;
        try {
            secondReservationResult = mockMvc.perform(post("/api/v1/reservations")
                            .header("Authorization", "Bearer " + passenger2Token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(reservationPayload))
                    .andExpect(status().isOk())
                    .andReturn();
        } catch (Exception e) {
            fail("Mehmet COULDNT MAKE HIS RESERVATION: " + e.getMessage());
        }

        Long mehmetReservationId = null;
        try {
            mehmetReservationId = objectMapper.readTree(secondReservationResult.getResponse().getContentAsString())
                    .get("id").asLong();
        } catch (Exception e) {
            fail("Mehmet rezervation ID couldnt be fetched: " + e.getMessage());
        }

        // RESERVATION ID CHECK
        assertTrue(mehmetReservationId != null);

        // MAIL FOR MEHMET
        try {
            String mailhogResponse2 = restTemplate.getForObject(
                    "http://" + mailhogWebHost + ":" + mailhogWebPort + "/api/v2/messages",
                    String.class);
            assertTrue(mailhogResponse2.contains("mehmet@test.com"), "Mehmet'e onay maili gitmiş olmalı");
        } catch (Exception e) {
            fail("Mail not sent for mehmet: " + e.getMessage());
        }
    }


}