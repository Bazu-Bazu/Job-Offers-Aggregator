package com.example.Job.Offers.Aggregator.service.subscriptionService;

import com.example.Job.Offers.Aggregator.model.Subscription;
import com.example.Job.Offers.Aggregator.model.User;
import com.example.Job.Offers.Aggregator.repository.SubscriptionRepository;
import com.example.Job.Offers.Aggregator.repository.UserRepository;
import com.example.Job.Offers.Aggregator.service.SubscriptionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class TestGetUserSubscriptions {

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private SubscriptionService subscriptionService;

    private final Long testTelegramId = 123456789L;
    private final User testUser = new User();
    private final List<Subscription> testSubscriptions = List.of(
            new Subscription(testUser, "Java"),
            new Subscription(testUser, "Kotlin")
    );

    @Test
    void getUserSubscriptions_shouldReturnSubscriptionsWhenUserExist() {
        when(userRepository.findByTelegramId(testTelegramId))
                .thenReturn(Optional.of(testUser));
        when(subscriptionRepository.findByUser(testUser))
                .thenReturn(testSubscriptions);


        List<String> result = subscriptionService.getUserSubscriptions(testTelegramId);

        assertEquals(2, result.size());
        assertTrue(result.contains("Java"));
        assertTrue(result.contains("Kotlin"));
        verify(subscriptionRepository).findByUser(testUser);
    }

    @Test
    void getUserSubscriptions_shouldReturnEmptyListWhenUserNotFound() {
        when(userRepository.findByTelegramId(testTelegramId))
                .thenReturn(Optional.empty());

        List<String> result = subscriptionService.getUserSubscriptions(testTelegramId);

        assertTrue(result.isEmpty());
        verify(subscriptionRepository, never()).findByUser(any());
    }

    @Test
    void getUserSubscriptions_shouldReturnEmptyListWhenNoSubscriptions() {
        when(userRepository.findByTelegramId(testTelegramId))
                .thenReturn(Optional.of(testUser));
        when(subscriptionRepository.findByUser(testUser))
                .thenReturn(Collections.emptyList());

        List<String> result = subscriptionService.getUserSubscriptions(testTelegramId);

        assertTrue(result.isEmpty());
    }

}
