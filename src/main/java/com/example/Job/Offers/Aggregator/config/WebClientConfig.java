package com.example.Job.Offers.Aggregator.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;


@Configuration
public class WebClientConfig {

    @Value("${hh.api.base-url}")
    private String baseUrl;

    @Bean
    public WebClient hhWebClient() {
        return WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("User-Agent", "Job-Offers-Aggregator")
                .build();
    }

}
