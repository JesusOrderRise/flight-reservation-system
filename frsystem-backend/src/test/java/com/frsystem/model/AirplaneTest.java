package com.frsystem.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;


public class AirplaneTest {

    //Constructor Test
    @Test
    void shouldCreateAirplaneSuccessfullyWhenDataIsValid() {
        Airplane airplane = new Airplane("TC-JJA", "Turkish Airlines", "Boeing 777-3F2(ER)", 349);
        assertNotNull(airplane);
    }

    //Parameter test for tailNumber value.
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t", "\n"})
    void shouldThrowExceptionWhenTailNumberIsInvalid(String invalidValue) {
        assertThrows(IllegalArgumentException.class, () ->
                new Airplane(invalidValue, "Turkish Airlines", "Boeing 777-3F2(ER)", 349));
    }

    //Parameter test for airline value.
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t", "\n"})
    void shouldThrowExceptionWhenAirlineIsInvalid(String invalidValue) {
        assertThrows(IllegalArgumentException.class, () ->
                new Airplane("TC-JJA", invalidValue, "Boeing 777-3F2(ER)", 349));
    }

    //Parameter test for model value.
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t", "\n"})
    void shouldThrowExceptionWhenModelIsInvalid(String invalidValue) {
        assertThrows(IllegalArgumentException.class, () ->
                new Airplane("TC-JJA", "Turkish Airlines", invalidValue, 349));
    }

    //Parameter test for capacity value for extra, it cannot be null, negative or zero.
    @ParameterizedTest
    @NullSource
    @ValueSource(ints = {-1, 0})
    void shouldThrowExceptionWhenCapacityIsInvalid(Integer invalidValue) {
        assertThrows(IllegalArgumentException.class, () ->
                new Airplane("TC-JJA", "Turkish Airlines", "Boeing 777-3F2(ER)", invalidValue));
    }

}
