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

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class TestUnsubscribe {

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
    void unsubscribe_shouldReturnTrueWhenSubscriptionExist() {
        when(userRepository.findByTelegramId(testTelegramId))
                .thenReturn(Optional.of(testUser));
        when(subscriptionRepository.findByUserAndQuery(testUser, testQuery))
                .thenReturn(Optional.of(new Subscription()));

        boolean result = subscriptionService.unsubscribe(testTelegramId, testQuery);

        assertTrue(result);
        verify(subscriptionRepository).deleteByUserAndQuery(testUser.getId(), testQuery);
    }

    @Test
    void unsubscribe_shouldReturnFalseWhenSubscriptionNotExist() {
        when(userRepository.findByTelegramId(testTelegramId))
                .thenReturn(Optional.of(testUser));
        when(subscriptionRepository.findByUserAndQuery(testUser, testQuery))
                .thenReturn(Optional.empty());

        boolean result = subscriptionService.unsubscribe(testTelegramId, testQuery);

        assertFalse(result);
        verify(subscriptionRepository, never()).deleteByUserAndQuery(testUser.getId(), testQuery);
    }

    @Test
    void unsubscribe_shouldThrowExceptionWhenUserNotFound() {
        when(userRepository.findByTelegramId(testTelegramId))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> subscriptionService.unsubscribe(testTelegramId, testQuery));
    }

}
