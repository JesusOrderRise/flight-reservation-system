package com.frsystem.mapper;

import com.frsystem.dto.AirportRequest;
import com.frsystem.dto.AirportResponse;
import com.frsystem.model.Airport;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface AirportMapper {

    AirportRequest toRequest(Airport airport);

    AirportResponse toResponse(Airport airport);

    @Mapping(target = "id", ignore = true)
    Airport toEntity(AirportRequest request);

    @Mapping(target = "id", ignore = true)
    Airport updateEntity(@MappingTarget Airport existing, AirportRequest request);
}
