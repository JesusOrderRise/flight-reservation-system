package com.frsystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AirportRequest {

    @NotBlank(message = "IATA CODE Cannot Be Empty!")
    @Size(min = 3, max = 3, message = "IATA code must be exactly 3 characters!")
    @Pattern(
            regexp = "^[A-Z]{3}$",
            message = "IATA code must be exactly 3 uppercase letters (A-Z), no numbers or special characters!"
    )
    private String iataCode;

    @NotBlank(message = "Airport Name Cannot Be Empty!")
    private String name;

    @NotBlank(message = "Country Cannot Be Empty!")
    private String country;

    @NotBlank(message = "City Cannot Be Empty!")
    private String city;
}