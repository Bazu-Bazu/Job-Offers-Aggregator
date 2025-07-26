package com.example.Job.Offers.Aggregator.service;

import com.example.Job.Offers.Aggregator.model.Subscription;
import com.example.Job.Offers.Aggregator.model.User;
import com.example.Job.Offers.Aggregator.repository.SubscriptionRepository;
import com.example.Job.Offers.Aggregator.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class TestSubscriptionService {

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private SubscriptionService subscriptionService;

    private final Long testTelegramId = 123456789L;
    private final String testQuery = "Java Developer";
    private final User testUser = new User();

    @Test
    void subscribe_shouldCreateNewSubscribeWhenNotExist() {
        when(userRepository.findByTelegramId(testTelegramId))
                .thenReturn(Optional.of(testUser));
        when(subscriptionRepository.findByUserAndQuery(testUser, testQuery))
                .thenReturn(Optional.empty());

        boolean result = subscriptionService.subscribe(testTelegramId, testQuery);

        assertTrue(result);
        verify(subscriptionRepository).save(any());
    }

    @Test
    void subscribe_shouldReturnFalseSubscriptionExists() {
        Subscription subscription = new Subscription();
        when(userRepository.findByTelegramId(testTelegramId))
                .thenReturn(Optional.of(testUser));
        when(subscriptionRepository.findByUserAndQuery(testUser, testQuery))
                .thenReturn(Optional.of(subscription));

        boolean result = subscriptionService.subscribe(testTelegramId, testQuery);

        assertFalse(result);
        verify(subscriptionRepository, never()).save(any());
    }

    @Test
    void subscribe_shouldThrowWhenUserNotFound() {
        when(userRepository.findByTelegramId(testTelegramId))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> subscriptionService.subscribe(testTelegramId, testQuery));
    }
}