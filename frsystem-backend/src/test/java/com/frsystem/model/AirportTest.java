package com.frsystem.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;


public class AirportTest {
    //Constructor Test
    @Test
    void shouldCreateAirportSuccessfullyWhenDataIsValid() {
        Airport airport = new Airport("ESB", "Esenboğa Havalimanı", "Türkiye", "Ankara");
        assertNotNull(airport);
    }

    //Parameter test for iataCode value.
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"ES", "ESBB", "123", "E1B", "1234", " ", "\t", "\n"})
    void shouldThrowExceptionWhenIataIsInvalid(String invalidIata) {
        assertThrows(IllegalArgumentException.class, () ->
                new Airport(invalidIata, "Esenboğa Havalimanı", "Türkiye", "Ankara"));
    }

    //Another test for iataCode since it must be a 3 letter only value.
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"ES", "ESBB", "123", "E1B", "1234"})
    void shouldThrowExceptionWhenIataIsNotFormattedRight(String invalidIata) {
        assertThrows(IllegalArgumentException.class, () ->
                new Airport(invalidIata, "Esenboğa Havalimanı", "Türkiye", "Ankara"));
    }

    // Parameter test for name value.
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t", "\n"})
    void shouldThrowExceptionWhenNameIsInvalid(String invalidValue) {
        assertThrows(IllegalArgumentException.class, () ->
                new Airport(invalidValue, "ESB", "Türkiye", "Ankara"));
    }

    // Parameter test for country value.
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t", "\n"})
    void shouldThrowExceptionWhenCountryIsInvalid(String invalidValue) {
        assertThrows(IllegalArgumentException.class, () ->
                new Airport("Esenboğa", "ESB", invalidValue, "Ankara"));
    }

    // Parameter test for city value.
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t", "\n"})
    void shouldThrowExceptionWhenCityIsInvalid(String invalidValue) {
        assertThrows(IllegalArgumentException.class, () ->
                new Airport("Esenboğa", "ESB", "Türkiye", invalidValue));
    }


}
