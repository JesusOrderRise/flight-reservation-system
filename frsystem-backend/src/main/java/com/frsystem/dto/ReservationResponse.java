package com.frsystem.dto;

import com.frsystem.enums.ReservationStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservationResponse {
    private Long id;

    private FlightResponse flight;

    private UserResponse user;

    private String seatNumber;

    private ReservationStatus status;
}
