package com.example.Job.Offers.Aggregator.api;

import com.example.Job.Offers.Aggregator.config.HhConfig;
import com.example.Job.Offers.Aggregator.dto.HhResponseDto;
import com.example.Job.Offers.Aggregator.dto.HhSalaryDto;
import com.example.Job.Offers.Aggregator.dto.HhVacancyDto;
import com.example.Job.Offers.Aggregator.model.Vacancy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;


@Slf4j
@Component
public class HhApiClient {

    private final HhConfig hhConfig;
    private final RestTemplate restTemplate;

    public HhApiClient(HhConfig hhConfig, RestTemplate restTemplate) {
        this.hhConfig = hhConfig;
        this.restTemplate = restTemplate;
    }

    public List<Vacancy> searchVacancies(String query, String area) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.set("User-Agent", "HH Vacancy Bot/1.0");

        URI uri = UriComponentsBuilder.fromHttpUrl(hhConfig.getHhBaseUrl())
                .path("/vacancies")
                .queryParam("text", query)
                .queryParam("area", area)
                .queryParam("per_page", 100)
                .queryParam("order_by", "publication_time")
                .queryParam("period", 7)
                .queryParam("employer_type", "company")
                .queryParam("only_with_salary", true)
                .queryParam("premium", "true")
                .build()
                .encode(StandardCharsets.UTF_8)
                .toUri();

        ResponseEntity<HhResponseDto> response = restTemplate.exchange(
                uri,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                HhResponseDto.class);
        log.info(String.valueOf(response    .getBody()));

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            return filterPopularVacancies(response.getBody().getItems(), query);
        }

        return Collections.emptyList();

    }

    private List<Vacancy> filterPopularVacancies(List<HhVacancyDto> items, String query) {
        return items.stream()
                .filter(Objects::nonNull)
                .sorted(Comparator
                        .comparing((HhVacancyDto v) ->
                                Optional.ofNullable(v.getSalary())
                                        .map(HhSalaryDto::getFrom)
                                        .orElse(0),
                                Comparator.reverseOrder())
                        .thenComparing(v ->
                                calculateRelevanceScore(v.getName(), query.toLowerCase()),
                                Comparator.reverseOrder())
                )
                .limit(5)
                .map(this::mapToVacancy)
                .collect(Collectors.toList());
    }

    private int calculateRelevanceScore(String title, String query) {
        if (title == null) {
            return 0;
        }

        int score = 0;

        if (title.toLowerCase().equals(query)) {
            score += 100;
        }

        if (title.toLowerCase().startsWith(query)) {
            score += 50;
        }

        return score;
    }

    private Vacancy mapToVacancy(HhVacancyDto item) {
        Vacancy vacancy = new Vacancy();
        vacancy.setTitle(item.getName());
        vacancy.setUrl(item.getUrl());
        vacancy.setEmployer(item.getEmployer().getName());
        vacancy.setSalary(parseSalary(item.getSalary()));
        return vacancy;
    }

    private String parseSalary(HhSalaryDto salary) {
        if (salary == null) {
            return "не указана";
        }

        String from = salary.getFrom() != null ? salary.getFrom().toString() : "";
        String to = salary.getTo() != null ? salary.getTo().toString() : "";
        String currency = salary.getCurrency() != null ? salary.getCurrency() : "";

        if (!from.isEmpty() && !to.isEmpty()) {
            return from + " - " + to + " " + currency;
        }
        else if (!from.isEmpty()) {
            return "oт " + from + " " + currency;
        }
        else if (!to.isEmpty()) {
            return "до " + to + " " + currency;
        }

        return "не указана";
    }

}
