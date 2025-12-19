package com.kobe.pokekernle;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class PokekernelApplication {

    public static void main(String[] args) {
        SpringApplication.run(PokekernelApplication.class, args);
    }

}
