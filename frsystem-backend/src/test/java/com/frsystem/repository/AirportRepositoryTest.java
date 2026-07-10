package com.frsystem.repository;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
public class AirportRepositoryTest {
    @Autowired
    private AirportRepository airportRepository;

    //is repository loaded?
    @Test
    void repositoryShouldLoadContext() {
        assertNotNull(airportRepository);
    }
}
