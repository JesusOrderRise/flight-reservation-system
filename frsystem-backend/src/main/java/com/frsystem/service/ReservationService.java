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
import java.util.Optional;


@Service
@Validated
public class ReservationService {
    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ReservationMapper reservationMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    public ReservationResponse makeReservation(@Valid ReservationRequest request) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long userId = (Long) authentication.getCredentials();


        Optional<Reservation> existingReservationOpt = reservationRepository
                .findByFlightIdAndSeatNumber(request.getFlightId(), request.getSeatNumber());

        Reservation reservationToSave;

        if (existingReservationOpt.isPresent()) {
            Reservation existingReservation = existingReservationOpt.get();


            if (existingReservation.getStatus() == ReservationStatus.CONFIRMED) {
                throw new ConflictException("This seat is occupied on that flight!");
            }


            existingReservation.setUser(userRepository.findById(userId).orElseThrow());
            existingReservation.setStatus(ReservationStatus.CONFIRMED);

            reservationToSave = existingReservation;

        } else {

            reservationToSave = reservationMapper.toEntity(request);
            reservationToSave.setUser(userRepository.findById(userId).orElseThrow());
            reservationToSave.setStatus(ReservationStatus.CONFIRMED);
        }


        Reservation saved = reservationRepository.save(reservationToSave);

        emailService.sendReservationConfirmation(
                saved.getUser().getEmail(),
                saved.getSeatNumber(),
                saved.getFlight().getId()
        );

        return reservationMapper.toResponse(saved);
    }

    //TODO: CANCEL OLAN BIR REZERVASYONU TEKRAR CANCEL ETMESI ÖNLENMELİ Mİ? (CONFLICT ERROR.)
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


