package com.frsystem.controller;

import com.frsystem.dto.AirportRequest;
import com.frsystem.dto.AirportResponse;
import com.frsystem.service.AirportService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/airports")
@CrossOrigin(origins = "http://localhost:5173")
@Validated
public class AirportController {

    @Autowired
    private AirportService airportservice;

    @GetMapping
    public List<AirportResponse> getAll() {
        return airportservice.getAll();
    }

    @GetMapping(path = "/{id}")
    public AirportResponse findByID(@PathVariable Long id) {
        return airportservice.findByID(id);
    }

    @PostMapping(path = "/search")
    public List<AirportResponse> searchWithParameters(@RequestBody AirportRequest search) {
        return airportservice.searchWithParameters(search);
    }


    //VALİD İLE KONTROL YAPARKEN WHITESPACE VERILDIĞINDE HER İKİ KONTROLDEN DE GEÇEMİYOR (@NOTBLANK VE @PATTERN)
    //EXCEPTIONHANDLER DA İLK HATA MESAJINI ALDIĞIMIZ İÇİN BAZEN BİRİNİ BAZEN BİRİNİ ALERTLE GÖSTERİYOR.
    @PostMapping
    public AirportResponse saveAirport(@Valid @RequestBody AirportRequest request) {
        return airportservice.saveAirport(request);
    }

    @PutMapping(path = "/{id}")
    public AirportResponse updateAirportByID(@PathVariable Long id, @Valid @RequestBody AirportRequest request) {
        return airportservice.updateAirportByID(id, request);
    }

    @DeleteMapping(path = "/{id}")
    public void deleteAirportByID(@PathVariable Long id) {
        airportservice.deleteAirportByID(id);
    }
}
