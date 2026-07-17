package com.frsystem.dto;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.frsystem.enums.FlightStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FlightResponse {

    private Long id;
    private String flightNumber;
    private AirplaneResponse airplane;
    private AirportResponse departureAirport;
    private AirportResponse arrivalAirport;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime departureTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime arrivalTime;

    private FlightStatus status;
    private Instant lastUpdate;
}
