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
public class TestUnsubscribeAll {

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private SubscriptionService subscriptionService;

    private final Long testTelegramId = 123456789L;
    private final User testUser = new User();

    @Test
    void unsubscribeAll_shouldReturnTrueWhenSubscriptionsExist() {
        List<Subscription> subscriptions = List.of(new Subscription(), new Subscription());

        when(userRepository.findByTelegramId(testTelegramId))
                .thenReturn(Optional.of(testUser));
        when(subscriptionRepository.findByUser(testUser))
                .thenReturn(subscriptions);

        boolean result = subscriptionService.unsubscribeAll(testTelegramId);

        assertTrue(result);
        verify(subscriptionRepository).deleteAll(subscriptions);
    }

    @Test
    void unsubscribe_shouldReturnFalseWhenSubscriptionsNotExist() {
        when(userRepository.findByTelegramId(testTelegramId))
                .thenReturn(Optional.of(testUser));
        when(subscriptionRepository.findByUser(testUser))
                .thenReturn(Collections.emptyList());

        boolean result = subscriptionService.unsubscribeAll(testTelegramId);

        assertFalse(result);
        verify(subscriptionRepository, never()).deleteByUser(testUser.getId());
    }

    @Test
    void unsubscribe_shouldThrowExceptionWhenUserNotFound() {
        when(userRepository.findByTelegramId(testTelegramId))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> subscriptionService.unsubscribeAll(testTelegramId));
    }

}
