package com.example.mlapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class MlAppApplication {
    public static void main(String[] args) {
        SpringApplication.run(MlAppApplication.class, args);
    }
}
