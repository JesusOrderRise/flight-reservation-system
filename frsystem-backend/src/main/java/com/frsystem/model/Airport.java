package com.frsystem.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "airport")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Airport {

    //Variables

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "iata_code", length = 3, nullable = false, unique = true)
    String iataCode; //UNIQUE

    @Column(name = "name", nullable = false)
    String name;

    @Column(name = "country", nullable = false)
    String country;

    @Column(name = "city", nullable = false)
    String city;


}
