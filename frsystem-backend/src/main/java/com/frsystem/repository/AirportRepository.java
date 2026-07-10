package com.frsystem.repository;

import com.frsystem.model.Airport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AirportRepository extends JpaRepository<Airport, Long> {
    public Optional<Airport> findByIataCode(String iataCode);
}
