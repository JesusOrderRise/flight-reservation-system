package com.frsystem.service;

import com.frsystem.dto.ReservationRequest;
import com.frsystem.dto.ReservationResponse;
import com.frsystem.enums.ReservationStatus;
import com.frsystem.exception.ConflictException;
import com.frsystem.exception.ResourceNotFoundException;
import com.frsystem.mapper.ReservationMapper;
import com.frsystem.model.Reservation;
import com.frsystem.repository.ReservationRepository;
import com.frsystem.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;


@Service
@Validated
public class ReservationService {
    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ReservationMapper reservationMapper;

    @Autowired
    private UserRepository userRepository;

    public ReservationResponse makeReservation(@Valid ReservationRequest request) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (reservationRepository.existsByFlightIdAndSeatNumber(request.getFlightId(), request.getSeatNumber())) {
            throw new ConflictException("This seat is occupied on that flight!");
        }

        Long userId = (Long) authentication.getCredentials();

        Reservation newReservation = reservationMapper.toEntity(request);
        newReservation.setUser(userRepository.findById(userId).get());
        newReservation.setStatus(ReservationStatus.CONFIRMED);

        return reservationMapper.toResponse(reservationRepository.save(newReservation));

    }

    //TODO: TESTLERİ TEKRAR YAZ.
    public ReservationResponse cancelSelfReservation(Long reservationId) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long userId = (Long) authentication.getCredentials();


        Reservation reservation = reservationRepository
                .findByIdAndUserId(reservationId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found or you don't have permission!"));


        reservation.setStatus(ReservationStatus.CANCELED);


        Reservation saved = reservationRepository.save(reservation);
        return reservationMapper.toResponse(saved);
    }

    public List<ReservationResponse> getMyReservations() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long userId = (Long) authentication.getCredentials();

        return reservationRepository.findAllByUserId(userId)
                .stream()
                .map(reservationMapper::toResponse)
                .toList();
    }

    public List<ReservationResponse> getAllReservations() {
        return reservationRepository.findAll()
                .stream()
                .map(reservationMapper::toResponse)
                .toList();
    }

    public ReservationResponse adminCancelReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found!"));

        reservation.setStatus(ReservationStatus.CANCELED);

        Reservation saved = reservationRepository.save(reservation);
        return reservationMapper.toResponse(saved);
    }
    
}


