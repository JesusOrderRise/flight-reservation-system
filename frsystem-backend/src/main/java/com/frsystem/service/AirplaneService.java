package com.frsystem.service;

import com.frsystem.dto.AirplaneRequest;
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

    //Delete using the ID, with validation of if it exists.
    public void deleteAirplaneByID(Long ID) {
        Airplane existing = airplaneRepository.findById(ID)
                .orElseThrow(() -> new RuntimeException("There is no Airplane with this ID!"));
        airplaneRepository.delete(existing);
    }

    //Parameter Search, injecting given parameters to the example Airplane class.
    public List<Airplane> searchWithParameters(String tailNumber, String airline, String model, Integer capacity) {
        Airplane example = new Airplane();
        if (tailNumber != null && !tailNumber.isBlank()) {
            example.setTailNumber(tailNumber);
        }
        if (airline != null && !airline.isBlank()) {
            example.setAirline(airline);
        }
        if (model != null && !model.isBlank()) {
            example.setModel(model);
        }
        if (capacity != null && capacity > 0 && capacity < 1000) {
            example.setCapacity(capacity);
        }


        //Ignoring nulls.
        ExampleMatcher matcher = ExampleMatcher.matching()
                .withIgnoreNullValues()
                .withIgnoreCase()
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING);

        return airplaneRepository.findAll(Example.of(example, matcher));
    }


    //saving Airplane With Validation.
    public Airplane saveAirplane(@Valid AirplaneRequest request) {

        Airplane airplane = new Airplane();


        airplane.setTailNumber(request.getTailNumber());
        airplane.setAirline(request.getAirline());
        airplane.setModel(request.getModel());
        airplane.setCapacity(request.getCapacity());

        return airplaneRepository.save(airplane);
    }

    //Finding by ID.
    public Optional<Airplane> findByID(Long ID) {
        return airplaneRepository.findById(ID);
    }


    //Updating Airplane with new data. The values that wont change should be given as null or blank.
    public Airplane updateAirplane(Long ID, AirplaneRequest newData) {
        Airplane existing = airplaneRepository.findById(ID)
                .orElseThrow(() -> new RuntimeException("There is no airplane with this ID!"));


        if (newData.getTailNumber() != null && !newData.getTailNumber().isBlank()) {
            if (!existing.getTailNumber().equals(newData.getTailNumber())) {
                Optional<Airplane> airplaneWithSameTail = airplaneRepository.findByTailNumber(newData.getTailNumber());

                if (airplaneWithSameTail.isPresent()) {
                    throw new RuntimeException("There is an existing plane with the same tail number!");
                }
            }
            existing.setTailNumber(newData.getTailNumber());
        }
        if (newData.getAirline() != null && !newData.getAirline().isBlank()) {
            existing.setAirline(newData.getAirline());
        }
        if (newData.getModel() != null && !newData.getModel().isBlank()) {
            existing.setModel(newData.getModel());
        }

        if (newData.getCapacity() != null) {
            if (newData.getCapacity() < 1 || newData.getCapacity() > 999) {
                throw new RuntimeException("The capacity must be a between 1 and 999!");
            }
            existing.setCapacity(newData.getCapacity());
        }
        return airplaneRepository.save(existing);
    }
    //test yaz.
}


