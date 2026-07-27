package com.frsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AirplaneResponse implements Serializable {

    private Long id;

    private String tailNumber;

    private String airline;

    private String model;

    private Integer capacity;
}