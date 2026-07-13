package com.frsystem.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "airplane")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Airplane {

    //Variables
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tail_number", nullable = false, unique = true)
    private String tailNumber; //UNIQUE

    @Column(name = "airline", nullable = false)
    private String airline;

    @Column(name = "model", nullable = false)
    private String model;

    @Column(name = "capacity", nullable = false)
    private Integer capacity;

}
