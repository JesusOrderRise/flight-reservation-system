package com.frsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AirplaneResponse {

    private Long id;

    private String tailNumber;

    private String airline;

    private String model;

    private Integer capacity;
}