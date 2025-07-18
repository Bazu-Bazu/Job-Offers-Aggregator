package com.example.Job.Offers.Aggregator.service;

import com.example.Job.Offers.Aggregator.model.Subscription;
import com.example.Job.Offers.Aggregator.model.User;
import com.example.Job.Offers.Aggregator.repository.SubscriptionRepository;
import com.example.Job.Offers.Aggregator.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;


@Service
@Slf4j
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;

    public SubscriptionService(SubscriptionRepository subscriptionRepository, UserRepository userRepository) {
        this.subscriptionRepository = subscriptionRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public boolean subscribe(Long telegramId, String query) {
        try {
            User user = userRepository.findByTelegramId(telegramId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if (subscriptionRepository.findByUserAndQuery(user, query).isEmpty()) {
                Subscription subscription = new Subscription();
                subscription.setUser(user);
                subscription.setQuery(query);
                subscriptionRepository.save(subscription);
                return true;
            }

            return false;
        } catch (Exception e) {
            log.error("Failed to subscribe user {} to query '{}'", telegramId, query, e);
            throw new RuntimeException("Subscription failed", e);
        }
    }

    @Transactional
    public boolean unsubscribe(Long telegramId, String query) {
        try {
            User user = userRepository.findByTelegramId(telegramId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if (subscriptionRepository.findByUserAndQuery(user, query).isPresent()) {
                subscriptionRepository.deleteByUserIdAndQuery(user.getId(), query);
                return true;
            }

            return false;
        } catch (Exception e) {
            log.error("Failed to unsubscribe user {} from query '{}'", telegramId, query, e);
            throw new RuntimeException("Unsubscription failed", e);
        }
    }

    @Transactional
    public boolean unsubscribeAll(Long telegramId) {
        try {
            User user = userRepository.findByTelegramId(telegramId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if (!subscriptionRepository.findByUser(user).isEmpty()) {
                subscriptionRepository.deleteByUserId(user.getId());
                return true;
            }

            return false;
        } catch (Exception e) {
            log.error("Failed to unsubscribeAll user {}", telegramId, e);
            throw new RuntimeException("UnsubscriptionAll failed", e);
        }
    }

    @Transactional
    public List<String> getUserSubscriptions(Long telegramId) {
        try {
            User user = userRepository.findByTelegramId(telegramId).orElse(null);

            if (user == null) {
                return Collections.emptyList();
            }

            return subscriptionRepository.findByUser(user)
                    .stream()
                    .map(Subscription::getQuery)
                    .toList();
        } catch (Exception e) {
            log.error("Failed to get subscriptions for user {}", telegramId, e);
            throw new RuntimeException("Failed to get subscriptions", e);
        }
    }

}
