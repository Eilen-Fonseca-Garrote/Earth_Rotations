package com.earthrotations.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class EarthquakesApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(EarthquakesApiApplication.class, args);
	}

}
