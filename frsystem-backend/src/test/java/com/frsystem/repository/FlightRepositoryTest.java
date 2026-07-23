package com.frsystem.repository;

import com.frsystem.config.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class FlightRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private FlightRepository flightRepository;

    //is repository loaded?
    @Test
    void repositoryShouldLoadContext() {
        assertNotNull(flightRepository);
    }
}