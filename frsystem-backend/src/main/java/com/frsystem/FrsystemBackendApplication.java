package com.frsystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;


@SpringBootApplication
@EnableCaching
public class FrsystemBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(FrsystemBackendApplication.class, args);
    }

}
