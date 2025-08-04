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
public class TestSendVacanciesToUsers {

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

    private final User testUser = new User();
    private final Long testTelegramId = 12345L;
    private final String testQuery = "Java Developer";
    private final String testArea = "1";
    private final Subscription testSubscription = new Subscription();
    private final Vacancy testVacancy = new Vacancy();

    @Test
    void sendVacanciesToUsers_shouldSendFormattedMessage() {
        testUser.setTelegramId(testTelegramId);

        testSubscription.setQuery(testQuery);
        testSubscription.setUser(testUser);

        testVacancy.setTitle(testQuery);

        when(subscriptionRepository.findAll())
                .thenReturn(List.of(testSubscription));
        when(hhApiClient.searchVacancies(anyString(), eq(testArea)))
                .thenReturn(List.of(testVacancy));

        vacancyService.sendDailyVacancies();

        verify(messageInterface).sendMessage(eq(testTelegramId), contains(testQuery));
    }

}
