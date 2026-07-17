package com.frsystem.repository;

import com.frsystem.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    boolean existsByFlightIdAndSeatNumber(Long flightId, String seatNumber);

    Optional<Reservation> findByIdAndUserId(Long reservationId, Long userId);
}

