package com.frsystem.repository;

import com.frsystem.model.Airplane;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface AirplaneRepository extends JpaRepository<Airplane, Long> {
    public Optional<Airplane> findByTailNumber(String tailNumber);
}
