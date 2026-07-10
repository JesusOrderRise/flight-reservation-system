package com.frsystem.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AirplaneRequest {

    @NotBlank(message = "Tail Number Cannot Be Empty!")
    private String tailNumber;

    @NotBlank(message = "Airline Cannot Be Empty!")
    private String airline;

    @NotBlank(message = "Model Cannot Be Empty!")
    private String model;

    @NotNull(message = "Capacity Cannot Be Empty!")
    @Min(value = 1, message = "Capacity must be a positive integer!")
    @Max(value = 999, message = "Capacity Cannot Be Bigger than 999!")
    private Integer capacity;
}