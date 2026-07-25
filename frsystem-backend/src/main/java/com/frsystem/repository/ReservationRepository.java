package com.frsystem.repository;

import com.frsystem.enums.ReservationStatus;
import com.frsystem.model.Flight;
import com.frsystem.model.Reservation;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    boolean existsByFlightIdAndSeatNumber(Long flightId, String seatNumber);

    Optional<Reservation> findByFlightIdAndSeatNumber(Long flightId, String seatNumber);

    Optional<Reservation> findByIdAndUserId(Long reservationId, Long userId);

    List<Reservation> findAllByUserId(Long userId);

    List<Reservation> findByFlightId(Long flightId);

    boolean existsByFlightAndStatus(Flight flight, ReservationStatus status);

    @Modifying
    @Transactional
    @Query("DELETE FROM Reservation r WHERE r.flight = :flight")
    void deleteByFlight(@Param("flight") Flight flight);
}

