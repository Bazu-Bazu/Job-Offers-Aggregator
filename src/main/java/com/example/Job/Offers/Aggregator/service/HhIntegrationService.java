package com.example.Job.Offers.Aggregator.service;

import com.example.Job.Offers.Aggregator.api.HhApiClient;
import com.example.Job.Offers.Aggregator.dto.HhVacancyDto;
import com.example.Job.Offers.Aggregator.model.Vacancy;
import com.example.Job.Offers.Aggregator.repository.VacancyRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@Service
public class HhIntegrationService {

    private final HhApiClient hhApiClient;
    private final VacancyRepository vacancyRepository;

    public HhIntegrationService(HhApiClient hhApiClient, VacancyRepository vacancyRepository) {
        this.hhApiClient = hhApiClient;
        this.vacancyRepository = vacancyRepository;
    }

    public Flux<Vacancy> fetchAndSaveVacancies(String query) {
        return hhApiClient.searchVacancies(query)
                .map(this::convertToEntity)
                .flatMap(vacancy -> Mono.fromRunnable(() -> vacancyRepository.save(vacancy)));
    }

    private Vacancy convertToEntity(HhVacancyDto dto) {
        Vacancy vacancy = new Vacancy();
        vacancy.setExternalId(dto.getId());
        vacancy.setTitle(dto.getName());
        vacancy.setCompany(dto.getEmployer());
        vacancy.setSalary(dto.getSalary());
        return vacancy;
    }

}
