package com.example.Job.Offers.Aggregator.service.vacancyService;

import com.example.Job.Offers.Aggregator.api.HhApiClient;
import com.example.Job.Offers.Aggregator.api.MessageInterface;
import com.example.Job.Offers.Aggregator.model.Subscription;
import com.example.Job.Offers.Aggregator.model.User;
import com.example.Job.Offers.Aggregator.model.Vacancy;
import com.example.Job.Offers.Aggregator.repository.SubscriptionRepository;
import com.example.Job.Offers.Aggregator.repository.VacancyRepository;
import com.example.Job.Offers.Aggregator.service.VacancyService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class TestSendDailyVacancies {

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private HhApiClient hhApiClient;

    @Mock
    private VacancyRepository vacancyRepository;

    @Mock
    private MessageInterface messageInterface;

    @InjectMocks
    private VacancyService vacancyService;

    private final User testUser1 = new User();
    private final User testUser2 = new User();
    private final Long testTelegramId1 = 12345L;
    private final Long testTelegramId2 = 1234567L;
    private final String testQuery1 = "Java Developer";
    private final String testQuery2 = "Golang Developer";
    private final String testArea = "1";
    private final Subscription testSubscription1 = new Subscription();
    private final Subscription testSubscription2 = new Subscription();

    @Test
    void sendDailyVacancies_shouldProcessAllSubscription() {
        testUser1.setTelegramId(testTelegramId1);
        testUser2.setTelegramId(testTelegramId2);

        testSubscription1.setId(1L);
        testSubscription1.setQuery(testQuery1);
        testSubscription1.setUser(testUser1);

        testSubscription2.setId(2L);
        testSubscription2.setQuery(testQuery2);
        testSubscription2.setUser(testUser2);

        when(subscriptionRepository.findAll())
                .thenReturn(List.of(testSubscription1, testSubscription2));
        when(hhApiClient.searchVacancies(anyString(), eq(testArea)))
                .thenReturn(List.of(new Vacancy()));

        vacancyService.sendDailyVacancies();

        verify(subscriptionRepository).findAll();
        verify(hhApiClient, times(2)).searchVacancies(anyString(), eq(testArea));
        verify(messageInterface, atLeastOnce()).sendMessage(anyLong(), anyString());
    }

    @Test
    void sendDailyVacancies_shouldSendNoVacanciesMessageWhenNotFound() {
        testUser1.setTelegramId(testTelegramId1);

        testSubscription1.setQuery(testQuery1);
        testSubscription1.setUser(testUser1);

        when(subscriptionRepository.findAll())
                .thenReturn(List.of(testSubscription1));
        when(hhApiClient.searchVacancies(anyString(), eq(testArea)))
                .thenReturn(List.of());

        vacancyService.sendDailyVacancies();

        verify(vacancyRepository, never()).saveAll(anyList());
        verify(messageInterface).sendMessage(eq(testTelegramId1), contains("не удалось сегодня найти вакансии"));
    }

}
