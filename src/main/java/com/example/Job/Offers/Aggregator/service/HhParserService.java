package com.example.Job.Offers.Aggregator.service;

import com.example.Job.Offers.Aggregator.model.User;
import com.example.Job.Offers.Aggregator.model.Vacancy;
import com.example.Job.Offers.Aggregator.repository.VacancyRepository;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.lang.reflect.Executable;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Service
@Slf4j
public class HhParserService {

    private final WebClient webClient;
    private final VacancyRepository vacancyRepository;
    private final String hh_url = "https://hh.ru";
    private final String defaultArea = "1";

    public HhParserService(WebClient.Builder webClientBuilder, VacancyRepository vacancyRepository) {
        this.webClient = webClientBuilder.baseUrl("https://api.example.com").build();
        this.vacancyRepository = vacancyRepository;
        log.info("HhParserService initialized");
    }

    public void parseAndSaveVacancies(String query, User user) {
        try {
            String url = buildUrl(query);
            String html = fetchHtml(url);
            List<Vacancy> vacancies = parseHtml(html, user);
            saveUniqueVacancies(vacancies);
        } catch (Exception e) {
            log.error("Error parsing vacancies for query '{}'", query, e);
            throw new RuntimeException("Failed to parse vacancies", e);
        }
    }

    private String buildUrl(String query) {
        try {
            return String.format("%s/search/vacancy?text=%s&area=%s",
                    hh_url,
                    URLEncoder.encode(query, StandardCharsets.UTF_8),
                    defaultArea);
        } catch (Exception e) {
            log.error("Error building URL for query: '{}", query, e);
            throw new RuntimeException("Failed to build URL", e);
        }
    }

    private String fetchHtml(String url) {
        try {
            return webClient.get()
                    .uri(url)
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (Exception e) {
            log.error("Failed to fetch HTML from URL: {}", url, e);
            throw new RuntimeException("Failed to fetch HTML", e);
        }
    }

    private List<Vacancy> parseHtml(String html, User user) {
        try {
            Document document = Jsoup.parse(html);
            Elements items = document.select(".serp-item");

            return items.stream()
                    .map(item -> convertToVacancy(item, user))
                    .filter(Objects::nonNull)
                    .toList();
        } catch (Exception e) {
            log.error("Error parsing HTML content", e);
            throw new RuntimeException("Failed to parse HTML", e);
        }
    }

    private Vacancy convertToVacancy(Element item, User user) {
        try {
            Vacancy vacancy = new Vacancy();

            Element linkElement = item.select("[data-qa=vacancy-serp__vacancy-title]").first();
            if (linkElement != null) {
                vacancy.setTitle(linkElement.text());
                vacancy.setLink(linkElement.attr("href"));
            }

            Element salaryElement = item.select("[data-qa=vacancy-serp__vacancy-compensation]").first();
            if (salaryElement != null) {
                vacancy.setSalary(salaryElement.text());
            }

            Element companyElement = item.select("[data-qa=vacancy-serp__vacancy-employer]").first();
            if (companyElement != null) {
                vacancy.setCompany(companyElement.text());
            }

            Element dateElement = item.select("[data-qa=vacancy-serp__vacancy-date]").first();
            if (dateElement != null) {
                vacancy.setPublishedAt(parseDate(dateElement.text()));
            }

            vacancy.setExternalId("hh_" + extractIdFromUrl(vacancy.getLink()));
            vacancy.setUser(user);

            return vacancy;
        } catch (Exception e) {
            log.warn("Failed to convert element to vacancy: {}", item, e);
            throw new RuntimeException("Failed to convert element to vacancy", e);
        }
    }

    private String extractIdFromUrl(String url) {
        try {
            Pattern pattern = Pattern.compile("vacancy/(\\d+)");
            Matcher matcher = pattern.matcher(url);
            return matcher.find() ? matcher.group(1) : String.valueOf(url.hashCode());
        } catch (Exception e) {
            log.warn("Failed to extract ID from URL: {}", url, e);
            throw new RuntimeException("Failed to extract ID from URL");
        }
    }

    private LocalDateTime parseDate(String dateText) {
        try {
            if (dateText.contains("сегодня")) {
                return LocalDate.now().atStartOfDay();
            }
            if (dateText.contains("вчера")) {
                return LocalDateTime.now().minusDays(1).toLocalDate().atStartOfDay();
            }
            return LocalDateTime.now();
        } catch (Exception e) {
            log.warn("Failed to parse date text: {}", dateText, e);
            throw new RuntimeException("Failed to parse date text");
        }
    }

    private void saveUniqueVacancies(List<Vacancy> vacancies) {
        try {
            vacancies.forEach(vacancy -> {
                if (!vacancyRepository.existsByExternalId(vacancy.getExternalId())) {
                    vacancyRepository.save(vacancy);
                }
            });
        } catch (Exception e) {
            log.error("Failed to save vacancies", e);
            throw new RuntimeException("Failed to save vacancies");
        }
    }

}
