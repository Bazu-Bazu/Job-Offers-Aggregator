package com.example.Job.Offers.Aggregator.api;

import com.example.Job.Offers.Aggregator.dto.HhResponseDto;
import com.example.Job.Offers.Aggregator.dto.HhVacancyDto;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;


@Component
public class HhApiClient {

    private final WebClient hhWebClient;

    public HhApiClient(WebClient hhWebClient) {
        this.hhWebClient = hhWebClient;
    }

    public Flux<HhVacancyDto> searchVacancies(String query) {
        return hhWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/vacancies")
                        .queryParam("text", query)
                        .queryParam("area", "1")
                        .queryParam("per_page", 50)
                        .build())
                .retrieve()
                .bodyToMono(HhResponseDto.class)
                .flatMapMany(response -> Flux.fromIterable(response.getItems()));
    }

}
