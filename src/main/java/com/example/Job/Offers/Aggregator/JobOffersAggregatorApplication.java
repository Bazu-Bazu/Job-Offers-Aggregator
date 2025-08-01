package com.example.Job.Offers.Aggregator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication
@EnableScheduling
public class JobOffersAggregatorApplication {

	public static void main(String[] args) {
		SpringApplication.run(JobOffersAggregatorApplication.class, args);
	}

}
