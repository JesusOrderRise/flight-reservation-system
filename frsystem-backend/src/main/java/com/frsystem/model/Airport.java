package com.frsystem.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Airport {
    //Variables
    String iataCode; //UNIQUE
    String name;
    String country;
    String city;

    //Constructor
    public Airport(String iataCode, String name, String country, String city) {
        this.iataCode = validateIata(iataCode);
        this.name = validate(name, "Name");
        this.country = validate(country, "Country");
        this.city = validate(city, "City");
    }

    //Besides the iata cannot be null or blank, it must be following a rule being a 3 letter only code.
    private String validateIata(String iata) {
        if (iata == null || iata.length() != 3 || iata.chars().anyMatch(Character::isDigit)) {
            throw new IllegalArgumentException("IATA should be a letter only 3 length value!");
        }
        return iata;
    }

    //Validator to ensure values are not null or blank.
    private String validate(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " cannot be emtpy or null!");
        }
        return value;
    }


}
