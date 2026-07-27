package com.frsystem.service;

import com.frsystem.dto.ReservationRequest;
import com.frsystem.dto.ReservationResponse;
import com.frsystem.dto.ReservationSearchRequest;
import com.frsystem.enums.ReservationStatus;
import com.frsystem.exception.ConflictException;
import com.frsystem.exception.ResourceNotFoundException;
import com.frsystem.mapper.ReservationMapper;
import com.frsystem.model.Flight;
import com.frsystem.model.Reservation;
import com.frsystem.model.User;
import com.frsystem.repository.ReservationRepository;
import com.frsystem.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
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

    //search function with new search body.
    @Cacheable(value = "reservationSearch", key = "#request.id + '_' + #request.flightNumber + '_' + #request.firstName + '_' + #request.lastName")
    public List<ReservationResponse> searchWithParameters(ReservationSearchRequest request) {

        
        if (request.getId() != null) {
            return reservationRepository.findById(request.getId())
                    .map(reservation -> List.of(reservationMapper.toResponse(reservation)))
                    .orElse(List.of());
        }


        Reservation example = new Reservation();

        if (request.getFlightNumber() != null && !request.getFlightNumber().isBlank()) {
            Flight flight = new Flight();
            flight.setFlightNumber(request.getFlightNumber());
            example.setFlight(flight);
        }

        if ((request.getFirstName() != null && !request.getFirstName().isBlank()) ||
                (request.getLastName() != null && !request.getLastName().isBlank())) {
            User user = new User();
            if (request.getFirstName() != null && !request.getFirstName().isBlank())
                user.setFirstName(request.getFirstName());
            if (request.getLastName() != null && !request.getLastName().isBlank())
                user.setLastName(request.getLastName());
            example.setUser(user);
        }

        ExampleMatcher matcher = ExampleMatcher.matching()
                .withIgnoreNullValues()
                .withMatcher("user.firstName", ExampleMatcher.GenericPropertyMatchers.contains().ignoreCase())
                .withMatcher("user.lastName", ExampleMatcher.GenericPropertyMatchers.contains().ignoreCase())
                .withMatcher("flight.flightNumber", ExampleMatcher.GenericPropertyMatchers.contains().ignoreCase());

        return reservationRepository.findAll(Example.of(example, matcher)).stream()
                .map(reservationMapper::toResponse)
                .toList();
    }

    @CacheEvict(value = {"allReservations", "myReservations", "flightReservations", "reservationSearch"}, allEntries = true)
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

        try {
            emailService.sendReservationConfirmation(
                    saved.getUser().getEmail(),
                    saved.getSeatNumber(),
                    saved.getFlight().getId()
            );
        } catch (Exception e) {

            System.err.println("Email could not sent. Reason: " + e.getMessage());
        }

        return reservationMapper.toResponse(saved);
    }

    @CacheEvict(value = {"allReservations", "myReservations", "flightReservations", "reservationSearch"}, allEntries = true)
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

    //Reading user credential from context holder for creating cache specified to user..
    @Cacheable(value = "myReservations", key = "T(org.springframework.security.core.context.SecurityContextHolder).getContext().getAuthentication().getCredentials()")
    public List<ReservationResponse> getMyReservations() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long userId = (Long) authentication.getCredentials();

        return reservationRepository.findAllByUserId(userId)
                .stream()
                .map(reservationMapper::toResponse)
                .toList();
    }

    //caches all reservation for admin.
    @Cacheable(value = "allReservations")
    public List<ReservationResponse> getAllReservations() {
        return reservationRepository.findAll()
                .stream()
                .map(reservationMapper::toResponse)
                .toList();
    }

    @CacheEvict(value = {"allReservations", "myReservations", "flightReservations", "reservationSearch"}, allEntries = true)
    public ReservationResponse adminCancelReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found!"));

        reservation.setStatus(ReservationStatus.CANCELED);

        Reservation saved = reservationRepository.save(reservation);
        return reservationMapper.toResponse(saved);
    }

    //caches the seats for the specified flight id.
    @Cacheable(value = "flightReservations", key = "#id")
    public List<ReservationResponse> getReservationsForFlight(Long id) {
        return reservationRepository.findByFlightId(id)
                .stream()
                .filter(reservation -> (ReservationStatus.CONFIRMED).equals(reservation.getStatus()))
                .map(reservation -> {
                    ReservationResponse response = new ReservationResponse();
                    response.setSeatNumber(reservation.getSeatNumber());
                    return response;
                })
                .toList();
    }
}


