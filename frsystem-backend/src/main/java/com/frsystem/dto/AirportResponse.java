package com.frsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AirportResponse {

    private Long id;

    private String iataCode;


    private String name;


    private String country;


    private String city;
}