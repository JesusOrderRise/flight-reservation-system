package com.frsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AirportResponse implements Serializable {

    private Long id;

    private String iataCode;


    private String name;


    private String country;


    private String city;
}