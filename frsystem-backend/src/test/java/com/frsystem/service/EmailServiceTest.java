package com.frsystem.service;

import com.frsystem.config.AbstractIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
public class EmailServiceTest extends AbstractIntegrationTest {

    @Autowired
    private EmailService emailService;

    @Value("${mailhog.web.host}")
    private String mailhogWebHost;

    @Value("${mailhog.web.port}")
    private String mailhogWebPort;

    private final RestTemplate restTemplate = new RestTemplate();

    private String mailhogApiUrl() {
        return "http://" + mailhogWebHost + ":" + mailhogWebPort + "/api/v2/messages";
    }

    @BeforeEach
    void clearMailhog() {
        restTemplate.delete("http://" + mailhogWebHost + ":" + mailhogWebPort + "/api/v1/messages");
    }

    @Test
    void shouldActuallySendEmailToMailhog() {
        emailService.sendReservationConfirmation("passenger@test.com", "A10", 5L);

        String response = restTemplate.getForObject(mailhogApiUrl(), String.class);

        assertTrue(response.contains("passenger@test.com"));
        assertTrue(response.contains("Reservation Confirmed"));
        assertTrue(response.contains("A10"));
    }
}