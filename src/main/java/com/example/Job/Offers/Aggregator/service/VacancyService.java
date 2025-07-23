package com.example.Job.Offers.Aggregator.service;

import com.example.Job.Offers.Aggregator.model.Subscription;
import com.example.Job.Offers.Aggregator.model.User;
import com.example.Job.Offers.Aggregator.model.Vacancy;
import com.example.Job.Offers.Aggregator.repository.VacancyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;


@Service
public class VacancyService {

    private final VacancyRepository vacancyRepository;

    public VacancyService(VacancyRepository vacancyRepository) {
        this.vacancyRepository = vacancyRepository;
    }

    @Transactional
    public List<Vacancy> saveNewVacancies(List<Vacancy> vacancies, User user, Subscription subscription) {
        vacancies.forEach(v -> {
            if (v.getExternalId() == null) {
                v.setExternalId(generateExternalId(v));
            }
            if (v.getPublishedAt() == null) {
                v.setPublishedAt(LocalDateTime.now());
            }
            v.setUser(user);
            v.setSubscription(subscription);
        });

        Set<String> allExternalIds = vacancies.stream()
                .map(Vacancy::getExternalId)
                .collect(Collectors.toSet());

        Set<String> existingIds = vacancyRepository.findExistingIds(allExternalIds);

        List<Vacancy> newVacancies = vacancies.stream()
                .filter(v -> !existingIds.contains(v.getExternalId()))
                .toList();

        if (!newVacancies.isEmpty()) {
            vacancyRepository.saveAll(newVacancies);
        }

        return vacancies;
    }

    private String generateExternalId(Vacancy vacancy) {
        return "gen-" + UUID.nameUUIDFromBytes(
                (vacancy.getEmployer() + "-" + vacancy.getTitle()).getBytes(StandardCharsets.UTF_8));
    }
}
