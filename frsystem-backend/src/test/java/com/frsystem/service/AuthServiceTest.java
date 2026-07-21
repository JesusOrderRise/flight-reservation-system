package com.frsystem.service;


import com.frsystem.dto.auth.LoginRequest;
import com.frsystem.dto.auth.LoginResponse;
import com.frsystem.dto.auth.RegisterRequest;
import com.frsystem.dto.auth.RegisterResponse;
import com.frsystem.enums.UserRoles;
import com.frsystem.model.User;
import com.frsystem.repository.UserRepository;
import com.frsystem.security.JwtTokenProvider;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
public class AuthServiceTest {

    @Autowired
    private AuthService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void tearDown() {
        userRepository.deleteAll();
    }

    //all possible invalid scenarios of RegisterRequest
    private static Stream<RegisterRequest> provideInvalidRegisterRequest() {
        return Stream.of(
                new RegisterRequest(null, "Yılmaz", "frsystem@frsystem.com", "Frsystem123!"),
                new RegisterRequest("Ahmet", null, "frsystem@frsystem.com", "Frsystem123!"),
                new RegisterRequest("Ahmet", "Yılmaz", null, "Frsystem123!"),
                new RegisterRequest("Ahmet", "Yılmaz", "frsystem@frsystem.com", null),
                new RegisterRequest("Ahmet", "Yılmaz", "frsystemfrsystem.com", "Frsystem123!"),
                new RegisterRequest("Ahmet", "Yılmaz", "frsystem@frsystem.com", "frsystem123!"),
                new RegisterRequest("Ahmet", "Yılmaz", "frsystem@frsystem.com", "Frsystem!"),
                new RegisterRequest("Ahmet", "Yılmaz", "frsystem@frsystem.com", "Frsystem123")

        );
    }

    //all possible invalid login requests
    private static Stream<LoginRequest> provideInvalidLoginRequest() {
        return Stream.of(
                new LoginRequest(null, "password"),
                new LoginRequest("try@try.com", null),
                new LoginRequest("trytry.com", "password")

        );
    }

    @ParameterizedTest
    @MethodSource("provideInvalidRegisterRequest")
    void shouldThrowExceptionWhenRegisteringInvalidPassenger(RegisterRequest invalidRegisterRequest) {

        assertThrows(ConstraintViolationException.class, () -> {
            userService.registerPassenger(invalidRegisterRequest);
        });
    }

    @Test
    void shouldSavePassengerToDatabaseAndWithCorrectPasswordHash() {
        RegisterRequest request = new RegisterRequest("Ahmet", "Yılmaz", "frsystem@frsystem.com", "Frsysytem123!");

        RegisterResponse saved = userService.registerPassenger(request);

        assertNotNull(saved.getId());

        assertTrue(userRepository.findByEmail("frsystem@frsystem.com").isPresent());

        User savedPassenger = userRepository.findById(saved.getId()).get();

        boolean passwordMatches = passwordEncoder.matches("Frsysytem123!", savedPassenger.getPasswordHash());
        assertTrue(passwordMatches);
        assertEquals(UserRoles.PASSENGER, savedPassenger.getRole());
    }

    @Test
    void shouldThrowExceptionWhenRegisteringPassengerIfSameEmailExists() {
        RegisterRequest request = new RegisterRequest("Ahmet", "Yılmaz", "frsystem@frsystem.com", "Frsysytem123!");
        RegisterRequest request1 = new RegisterRequest("Mahmut", "Tuncer", "frsystem@frsystem.com", "Mahmut123!");

        userService.registerPassenger(request);

        Exception exception = assertThrows(RuntimeException.class, () -> {
            userService.registerPassenger(request1);
        });

        assertEquals("There is an existing passenger with the same email!", exception.getMessage());
    }

    @ParameterizedTest
    @MethodSource("provideInvalidRegisterRequest")
    void shouldThrowExceptionWhenRegisteringInvalidAdmin(RegisterRequest invalidRegisterRequest) {

        assertThrows(ConstraintViolationException.class, () -> {
            userService.registerAdmin(invalidRegisterRequest);
        });
    }

    @Test
    void shouldSaveAdminToDatabaseAndWithCorrectPasswordHash() {
        RegisterRequest request = new RegisterRequest("Ahmet", "Yılmaz", "admin@admin.com", "Admin123!");

        RegisterResponse saved = userService.registerAdmin(request);

        assertNotNull(saved.getId());

        assertTrue(userRepository.findByEmail("admin@admin.com").isPresent());

        User savedAdmin = userRepository.findById(saved.getId()).get();

        boolean passwordMatches = passwordEncoder.matches("Admin123!", savedAdmin.getPasswordHash());
        assertTrue(passwordMatches);
        assertEquals(UserRoles.ADMIN, savedAdmin.getRole());
    }

    @Test
    void shouldThrowExceptionWhenRegisteringAdminIfSameEmailExists() {
        RegisterRequest request = new RegisterRequest("Ahmet", "Yılmaz", "frsystem@frsystem.com", "Frsysytem123!");
        RegisterRequest request1 = new RegisterRequest("Mahmut", "Tuncer", "frsystem@frsystem.com", "Mahmut123!");

        userService.registerAdmin(request);

        Exception exception = assertThrows(RuntimeException.class, () -> {
            userService.registerAdmin(request1);
        });

        assertEquals("There is an existing admin with the same email!", exception.getMessage());
    }


    @ParameterizedTest
    @MethodSource("provideInvalidLoginRequest")
    void shouldThrowExceptionWhenLoginCredentialsAreInvalid(LoginRequest invalidLoginRequest) {

        assertThrows(ConstraintViolationException.class, () -> {
            userService.login(invalidLoginRequest);
        });
    }

    @Test
    void shouldThrowExceptionWhenTheUserCouldNotBeFoundInLogin() {
        RegisterRequest registerRequest = new RegisterRequest("Mahmut", "Tuncer", "mahmut@tuncer.com", "Mahmut123!");
        userService.registerPassenger(registerRequest);

        LoginRequest loginRequest = new LoginRequest("blank@blank.com", "Blank123!");

        Exception exception = assertThrows(RuntimeException.class, () -> {
            userService.login(loginRequest);
        });

        assertEquals("User not found!", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenThePasswordIsWrongInLogin() {
        RegisterRequest registerRequest = new RegisterRequest("Mahmut", "Tuncer", "mahmut@tuncer.com", "Mahmut123!");
        userService.registerPassenger(registerRequest);

        LoginRequest loginRequest = new LoginRequest("mahmut@tuncer.com", "Blank123!");

        Exception exception = assertThrows(RuntimeException.class, () -> {
            userService.login(loginRequest);
        });

        assertEquals("Invalid password!", exception.getMessage());
    }

    @Test
    void shouldSuccessfullyProvideATokenWhenPassengerLogin() {
        RegisterRequest registerRequest = new RegisterRequest("Mahmut", "Tuncer", "mahmut@tuncer.com", "Mahmut123!");
        userService.registerPassenger(registerRequest);

        LoginRequest loginRequest = new LoginRequest("mahmut@tuncer.com", "Mahmut123!");

        LoginResponse loginResponse = userService.login(loginRequest);

        assertNotNull(loginResponse.getToken());
        assertEquals("mahmut@tuncer.com", jwtTokenProvider.extractEmail(loginResponse.getToken()));
        assertEquals("PASSENGER", jwtTokenProvider.extractRole(loginResponse.getToken()));
    }

    @Test
    void shouldSuccessfullyProvideATokenWhenAdminLogin() {
        RegisterRequest registerRequest = new RegisterRequest("Mahmut", "Tuncer", "mahmut@tuncer.com", "Mahmut123!");
        userService.registerAdmin(registerRequest);

        LoginRequest loginRequest = new LoginRequest("mahmut@tuncer.com", "Mahmut123!");

        LoginResponse loginResponse = userService.login(loginRequest);

        assertNotNull(loginResponse.getToken());
        assertEquals("mahmut@tuncer.com", jwtTokenProvider.extractEmail(loginResponse.getToken()));
        assertEquals("ADMIN", jwtTokenProvider.extractRole(loginResponse.getToken()));
    }

}
