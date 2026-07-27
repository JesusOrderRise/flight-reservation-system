package com.frsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservationSearchRequest {

    private Long id;
    private String flightNumber;
    private String firstName;
    private String lastName;

}
