package com.frsystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservationRequest {

    @NotNull(message = "Flight Id Cannot Be Empty!")
    private Long flightId;

    @NotBlank(message = "Seat Number Cannot Be Empty!")
    private String seatNumber;

}
