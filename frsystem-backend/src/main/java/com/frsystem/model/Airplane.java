package com.frsystem.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Airplane {
    //Variables
    String tailNumber; //UNIQUE
    String airline;
    String model;
    Integer capacity;

    //Constructor
    public Airplane(String tailNumber, String airline, String model, Integer capacity) {
        this.tailNumber = validate(tailNumber, "Tail Number");
        this.airline = validate(airline, "Airline");
        this.model = validate(model, "Model");
        this.capacity = validateCapacity(capacity);
    }

    //Capacity validator for negative, zero and null values.
    private Integer validateCapacity(Integer capacity) {
        if (capacity == null || capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be positive!");
        }
        return capacity;
    }

    //Validator to ensure values are not null or blank.
    private String validate(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " cannot be emtpy or null!");
        }
        return value;
    }


}
