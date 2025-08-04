package com.example.Job.Offers.Aggregator.service.vacancyService;

import com.example.Job.Offers.Aggregator.api.HhApiClient;
import com.example.Job.Offers.Aggregator.model.Subscription;
import com.example.Job.Offers.Aggregator.model.User;
import com.example.Job.Offers.Aggregator.model.Vacancy;
import com.example.Job.Offers.Aggregator.repository.SubscriptionRepository;
import com.example.Job.Offers.Aggregator.repository.UserRepository;
import com.example.Job.Offers.Aggregator.repository.VacancyRepository;
import com.example.Job.Offers.Aggregator.service.VacancyService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class TestSearchVacancyAfterSubscribing {

    @Mock
    private VacancyRepository vacancyRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private HhApiClient hhApiClient;

    @InjectMocks
    private VacancyService vacancyService;

    private final User testUser = new User();
    private final Long testTelegramId = 12345L;
    private final String testQuery = "Java Developer";
    private final String testArea = "1";
    private final Subscription testSubscription = new Subscription();
    private final Vacancy testVacancy1 = new Vacancy();
    private final Vacancy testVacancy2 = new Vacancy();

    @Test
    void searchVacanciesAfterSubscribing_shouldReturnVacanciesWhenUserAndSubscriptionExist() {
        testUser.setTelegramId(testTelegramId);

        testSubscription.setQuery(testQuery);

        testVacancy1.setTitle("Java Developer");
        testVacancy1.setTitle("Java Junior");
        List<Vacancy> apiVacancies = List.of(testVacancy1, testVacancy2);

        when(userRepository.findByTelegramId(testTelegramId))
                .thenReturn(Optional.of(testUser));
        when(subscriptionRepository.findByUserAndQuery(testUser, testQuery))
                .thenReturn(Optional.of(testSubscription));
        when(hhApiClient.searchVacancies(testQuery, testArea))
                .thenReturn(apiVacancies);

        List<Vacancy> result = vacancyService.searchVacanciesAfterSubscribing(testQuery, testTelegramId);

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(vacancyRepository).saveAll(anyList());

    }

    @Test
    void searchVacanciesAfterSubscribing_shouldReturnExceptionWhenUserNotFound() {
        when(userRepository.findByTelegramId(testTelegramId))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> vacancyService.searchVacanciesAfterSubscribing(testQuery, testTelegramId),
                "User not found");
    }

    @Test
    void searchVacanciesAfterSubscribing_shouldReturnExceptionWhenSubscriptionNotFound() {
        when(userRepository.findByTelegramId(testTelegramId))
                .thenReturn(Optional.of(testUser));
        when(subscriptionRepository.findByUserAndQuery(testUser, testQuery))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> vacancyService.searchVacanciesAfterSubscribing(testQuery, testTelegramId),
                "Subscription not found");
    }

    @Test
    void searchVacanciesAfterSubscribing_shouldGenerateExternalIds() {
        when(userRepository.findByTelegramId(testTelegramId))
                .thenReturn(Optional.of(testUser));
        when(subscriptionRepository.findByUserAndQuery(testUser, testQuery))
                .thenReturn(Optional.of(testSubscription));
        when(hhApiClient.searchVacancies(testQuery, testArea))
                .thenReturn(List.of(testVacancy1, testVacancy2));

        List<Vacancy> result = vacancyService.searchVacanciesAfterSubscribing(testQuery, testTelegramId);

        assertNotNull(result.get(0).getExternalId());
        assertNotNull(result.get(1).getExternalId());
    }

}
