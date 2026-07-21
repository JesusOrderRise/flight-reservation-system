package com.frsystem.mapper;

import com.frsystem.dto.FlightRequest;
import com.frsystem.dto.FlightResponse;
import com.frsystem.model.Airplane;
import com.frsystem.model.Airport;
import com.frsystem.model.Flight;
import com.frsystem.repository.AirplaneRepository;
import com.frsystem.repository.AirportRepository;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring", uses = {AirportMapper.class, AirplaneMapper.class})
public abstract class FlightMapper {

    @Autowired
    protected AirplaneRepository airplaneRepository;

    @Autowired
    protected AirportRepository airportRepository;

    // ✅ Long → Airport
    protected Airport airportMap(Long value) {
        if (value == null) return null;
        return airportRepository.findById(value).get();
    }


    protected Airplane airplaneMap(Long value) {
        if (value == null) return null;
        return airplaneRepository.findById(value).get();

    }


    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "lastUpdate", ignore = true)
    @Mapping(target = "departureAirport", source = "departureAirportId")
    @Mapping(target = "arrivalAirport", source = "arrivalAirportId")
    @Mapping(target = "airplane", source = "airplaneId")
    public abstract Flight toEntity(FlightRequest request);


    public abstract FlightResponse toResponse(Flight flight);
    

}