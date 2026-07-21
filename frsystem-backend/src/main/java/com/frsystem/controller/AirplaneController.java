package com.frsystem.controller;

import com.frsystem.dto.AirplaneRequest;
import com.frsystem.dto.AirplaneResponse;
import com.frsystem.service.AirplaneService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/airplanes")
@CrossOrigin(origins = "http://localhost:5173")
@Validated
public class AirplaneController {

    @Autowired
    private AirplaneService airplaneService;

    @GetMapping
    public List<AirplaneResponse> getAll() {
        return airplaneService.getAll();
    }

    @GetMapping(path = "/{id}")
    public AirplaneResponse findByID(@PathVariable Long id) {
        return airplaneService.findByID(id);
    }

    @PostMapping(path = "/search")
    public List<AirplaneResponse> searchWithParameters(@RequestBody AirplaneRequest search) {
        return airplaneService.searchWithParameters(search);
    }

    @PostMapping
    public AirplaneResponse saveAirplane(@Valid @RequestBody AirplaneRequest request) {
        return airplaneService.saveAirplane(request);
    }

    @PutMapping(path = "/{id}")
    public AirplaneResponse updateAirplaneByID(@PathVariable Long id, @Valid @RequestBody AirplaneRequest request) {
        return airplaneService.updateAirplaneByID(id, request);
    }

    @DeleteMapping(path = "/{id}")
    public void deleteAirplaneByID(@PathVariable Long id) {
        airplaneService.deleteAirplaneByID(id);
    }
}