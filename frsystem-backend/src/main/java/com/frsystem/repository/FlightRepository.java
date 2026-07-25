package com.frsystem.repository;

import com.frsystem.model.Airplane;
import com.frsystem.model.Airport;
import com.frsystem.model.Flight;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface FlightRepository extends JpaRepository<Flight, Long> {
    public Optional<Flight> findByFlightNumber(String flightNumber);

    boolean existsByAirplane(Airplane airplane);

    boolean existsByDepartureAirport(Airport departureAirport);

    boolean existsByArrivalAirport(Airport arrivalAirport);
}
