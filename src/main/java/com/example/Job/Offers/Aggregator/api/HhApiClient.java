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
        try {
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
            log.info(String.valueOf(response.getBody()));

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return filterPopularVacancies(response.getBody().getItems(), query);
            }

            return Collections.emptyList();
        } catch (Exception e) {
            log.error("Error searching vacancies for query {}", query, e);
            throw new RuntimeException("Error searching vacancies", e);
        }

    }

    private List<Vacancy> filterPopularVacancies(List<HhVacancyDto> items, String query) {
        try {
            return items.stream()
                    .filter(Objects::nonNull)
                    .filter(v -> v.getName() != null)
                    .sorted(Comparator
                            .comparingInt((HhVacancyDto v) ->
                                    calculateRelevanceScore(v.getName(), query.toLowerCase()))
                            .reversed()
                            .thenComparing(v ->
                                            Optional.ofNullable(v.getSalary())
                                                    .map(HhSalaryDto::getFrom)
                                                    .orElse(0),
                                    Comparator.reverseOrder())
                    )
                    .limit(50)
                    .map(this::mapToVacancy)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error filtering popular vacancies for query {}", query, e);
            throw new RuntimeException("Error filtering popular vacancies", e);
        }
    }

    private int calculateRelevanceScore(String title, String query) {
        try {
            if (title.toLowerCase().equals(query)) {
                return 1000;
            }

            if (title.toLowerCase().contains(" " + query + " ")) {
                return 500;
            }

            String[] queryWords = query.split(" ");
            String[] titleWords = title.toLowerCase().split(" ");

            long matchedWords = Arrays.stream(queryWords)
                    .filter(q -> Arrays.asList(titleWords).contains(q))
                    .count();

            int score = (int) (matchedWords * 100 / queryWords.length);

            if (title.toLowerCase().startsWith(query)) {
                score += 80;
            }

            if (title.toLowerCase().contains(query)) {
                score += 50;
            }

            return score;
        } catch (Exception e) {
            log.error("Error calculating relevance score fo query {}", query, e);
            throw new RuntimeException("Error calculating relevance score", e);
        }
    }

    private Vacancy mapToVacancy(HhVacancyDto item) {
        try {
            Vacancy vacancy = new Vacancy();
            vacancy.setTitle(item.getName());
            vacancy.setUrl(item.getUrl());
            vacancy.setEmployer(item.getEmployer().getName());
            vacancy.setSalary(parseSalary(item.getSalary()));
            return vacancy;
        } catch (Exception e) {
            log.error("Error mapping to vacancy", e);
            throw new RuntimeException(e);
        }
    }

    private String parseSalary(HhSalaryDto salary) {
        try {
            if (salary == null) {
                return "не указана";
            }

            String from = salary.getFrom() != null ? salary.getFrom().toString() : "";
            String to = salary.getTo() != null ? salary.getTo().toString() : "";
            String currency = salary.getCurrency() != null ? salary.getCurrency() : "";

            if (!from.isEmpty() && !to.isEmpty()) {
                return from + " - " + to + " " + currency;
            } else if (!from.isEmpty()) {
                return "oт " + from + " " + currency;
            } else if (!to.isEmpty()) {
                return "до " + to + " " + currency;
            }

            return "не указана";
        } catch (Exception e) {
            log.error("Error parsing salary", e);
            throw new RuntimeException(e);
        }
    }

}
