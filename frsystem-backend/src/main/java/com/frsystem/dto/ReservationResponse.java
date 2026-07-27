package com.frsystem.dto;

import com.frsystem.enums.ReservationStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservationResponse implements Serializable {
    private Long id;

    private FlightResponse flight;

    private UserResponse user;

    private String seatNumber;

    private ReservationStatus status;
}
