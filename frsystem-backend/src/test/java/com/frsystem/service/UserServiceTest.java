package com.frsystem.service;


import com.frsystem.dto.user.RegisterRequest;
import com.frsystem.dto.user.RegisterResponse;
import com.frsystem.model.User;
import com.frsystem.repository.UserRepository;
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
public class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

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

        User savedUser = userRepository.findById(saved.getId()).get();

        boolean passwordMatches = passwordEncoder.matches("Frsysytem123!", savedUser.getPasswordHash());
        assertTrue(passwordMatches);
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

}
