package com.frsystem.repository;

import com.frsystem.model.Flight;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface FlightRepository extends JpaRepository<Flight, Long> {
    public Optional<Flight> findByFlightNumber(String flightNumber);
}
