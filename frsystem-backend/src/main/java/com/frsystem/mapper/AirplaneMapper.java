package com.frsystem.mapper;

import com.frsystem.dto.AirplaneRequest;
import com.frsystem.dto.AirplaneResponse;
import com.frsystem.model.Airplane;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface AirplaneMapper {

    AirplaneRequest toRequest(Airplane airplane);

    AirplaneResponse toResponse(Airplane airplane);

    @Mapping(target = "id", ignore = true)
    Airplane toEntity(AirplaneRequest request);

    @Mapping(target = "id", ignore = true)
    Airplane updateEntity(@MappingTarget Airplane existing, AirplaneRequest request);
}


