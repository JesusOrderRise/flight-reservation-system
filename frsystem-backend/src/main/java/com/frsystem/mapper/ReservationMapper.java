package com.frsystem.mapper;

import com.frsystem.dto.ReservationRequest;
import com.frsystem.dto.ReservationResponse;
import com.frsystem.model.Flight;
import com.frsystem.model.Reservation;
import com.frsystem.repository.FlightRepository;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring", uses = {FlightMapper.class, UserMapper.class})
public abstract class ReservationMapper {

    @Autowired
    protected FlightRepository flightRepository;

    protected Flight flightMap(Long value) {
        return flightRepository.findById(value).get();
    }


    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "flight", source = "flightId")
    public abstract Reservation toEntity(ReservationRequest request);

    public abstract ReservationResponse toResponse(Reservation reservation);
}