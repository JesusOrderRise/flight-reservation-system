package com.frsystem.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FlightRequest {

    @NotBlank(message = "Flight Number Cannot Be Empty!")
    private String flightNumber;

    @NotNull(message = "Airplane Cannot Be Empty!")
    private Long airplaneId;

    @NotNull(message = "Departure Airport Cannot Be Empty!")
    private Long departureAirportId;

    @NotNull(message = "Arrival Airport Cannot Be Empty!")
    private Long arrivalAirportId;

    @NotNull(message = "Departure Time Cannot Be Empty!")
    @Future(message = "Departure Time Must Be In Future!")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime departureTime;

    @NotNull(message = "Arrival Time Cannot Be Empty!")
    @Future(message = "Arrival Time Must Be In Future!")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime arrivalTime;

    @AssertTrue(message = "Arrival Time Must Be After Departure Time.")
    public boolean isArrivalAfterDeparture() {
        if (departureTime == null || arrivalTime == null) {
            return true;
        }
        return arrivalTime.isAfter(departureTime);
    }

    @AssertTrue(message = "Departure and Arrival airports cannot be the same!")
    public boolean isAirportsDifferent() {
        if (departureAirportId == null || arrivalAirportId == null) {
            return true;
        }
        return !departureAirportId.equals(arrivalAirportId);
    }


}