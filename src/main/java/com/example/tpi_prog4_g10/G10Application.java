package com.example.tpi_prog4_g10;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class G10Application {

    public static void main(String[] args) {
        SpringApplication.run(G10Application.class, args);
    }
}