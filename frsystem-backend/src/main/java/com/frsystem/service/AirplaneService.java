package com.frsystem.service;

import com.frsystem.dto.AirplaneRequest;
import com.frsystem.dto.AirplaneResponse;
import com.frsystem.mapper.AirplaneMapper;
import com.frsystem.model.Airplane;
import com.frsystem.repository.AirplaneRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Optional;


@Service
@Validated
public class AirplaneService {

    @Autowired
    private AirplaneRepository airplaneRepository;

    @Autowired
    private AirplaneMapper airplaneMapper;

    public List<AirplaneResponse> getAll() {
        return airplaneRepository.findAll()
                .stream()
                .map(airplaneMapper::toResponse)
                .toList();
    }

    //Delete using the ID, with validation of if it exists.
    public void deleteAirplaneByID(Long ID) {
        Airplane existing = airplaneRepository.findById(ID)
                .orElseThrow(() -> new RuntimeException("There is no Airplane with this ID!"));
        airplaneRepository.delete(existing);
    }

    //Parameter Search, injecting given parameters to the example Airplane class.
    public List<AirplaneResponse> searchWithParameters(AirplaneRequest request) {

        Airplane example = airplaneMapper.toEntity(request);

        ExampleMatcher matcher = ExampleMatcher.matching()
                .withIgnoreNullValues()
                .withIgnoreCase()
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING);


        return airplaneRepository.findAll(Example.of(example, matcher)).stream()
                .map(airplaneMapper::toResponse) // Her bir entity'yi Response DTO'ya çevir
                .toList();
    }


    //saving Airplane With Validation.
    public AirplaneResponse saveAirplane(@Valid AirplaneRequest request) {

        Airplane airplane = airplaneMapper.toEntity(request);

        if (airplaneRepository.findByTailNumber(airplane.getTailNumber()).isPresent()) {
            throw new RuntimeException("There is an existing airplane with the same Tail Number!");
        }


        return airplaneMapper.toResponse(airplaneRepository.save(airplane));
    }

    //Finding by ID.
    public Optional<AirplaneResponse> findByID(Long ID) {
        return airplaneRepository.findById(ID).map(airplaneMapper::toResponse);
    }


    //Updating Airplane with new data. All the values should be given
    public AirplaneResponse updateAirplaneByID(Long ID, @Valid AirplaneRequest newData) {
        Airplane existing = airplaneRepository.findById(ID)
                .orElseThrow(() -> new RuntimeException("There is no Airplane with this ID!"));


        if (!existing.getTailNumber().equals(newData.getTailNumber())) {
            Optional<Airplane> airplaneWithSameTail = airplaneRepository.findByTailNumber(newData.getTailNumber());

            if (airplaneWithSameTail.isPresent()) {
                throw new RuntimeException("There is an existing airplane with the same Tail Number!");
            }
        }

        airplaneMapper.updateEntity(existing, newData);
        Airplane updatedEntity = airplaneRepository.save(existing);
        return airplaneMapper.toResponse(updatedEntity);
    }

}


