package com.example.Job.Offers.Aggregator.service.vacancyService;

import com.example.Job.Offers.Aggregator.model.Subscription;
import com.example.Job.Offers.Aggregator.model.User;
import com.example.Job.Offers.Aggregator.model.Vacancy;
import com.example.Job.Offers.Aggregator.repository.VacancyRepository;
import com.example.Job.Offers.Aggregator.service.VacancyService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class TestSaveNewVacancies {

    @Mock
    private VacancyRepository vacancyRepository;

    @InjectMocks
    private VacancyService vacancyService;

    private final User testUser = new User();
    private final Subscription testSubscription = new Subscription();
    private final Vacancy testVacancy1 = new Vacancy();
    private final Vacancy testVacancy2 = new Vacancy();

    @Test
    void saveNewVacancies_shouldSetMissingFieldsAndSaveOnlyNewVacancies() {
        testVacancy1.setTitle("Java Developer");
        testVacancy1.setExternalId("externalId1");

        testVacancy2.setTitle("Golang Developer");
        testVacancy2.setExternalId("externalId2");

        List<Vacancy> vacancies = List.of(testVacancy1, testVacancy2);

        when(vacancyRepository.findExternalIdsByUserAndSubscription(testUser, testSubscription))
                .thenReturn(Set.of("externalId2"));

        vacancyService.saveNewVacancies(vacancies, testUser, testSubscription);

        assertNotNull(testVacancy1.getPublishedAt());
        assertNotNull(testVacancy2.getPublishedAt());
        assertEquals(testUser, testVacancy1.getUser());
        assertEquals(testUser, testVacancy2.getUser());
        assertEquals(testSubscription, testVacancy1.getSubscription());
        assertEquals(testSubscription, testVacancy2.getSubscription());
        verify(vacancyRepository).saveAll(List.of(testVacancy1));
    }

}
