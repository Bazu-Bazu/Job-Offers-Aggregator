package com.example.Job.Offers.Aggregator.service;

import com.example.Job.Offers.Aggregator.model.Subscription;
import com.example.Job.Offers.Aggregator.model.User;
import com.example.Job.Offers.Aggregator.repository.SubscriptionRepository;
import com.example.Job.Offers.Aggregator.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class SubscruptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;

    public SubscruptionService(SubscriptionRepository subscriptionRepository, UserRepository userRepository) {
        this.subscriptionRepository = subscriptionRepository;
        this.userRepository = userRepository;
    }

    public boolean subscribe(Long telegramId, String query) {
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
    }

    public void unsubscribe(Long telegramId, String query) {
        User user = userRepository.findByTelegramId(telegramId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        subscriptionRepository.deleteByUserAndQuery(user, query);
    }

    public List<String> getUserSubscriptions(Long telegramId) {
        User user = userRepository.findByTelegramId(telegramId).orElse(null);

        if (user == null) {
            return Collections.emptyList();
        }

        return subscriptionRepository.findByUser(user)
                .stream()
                .map(Subscription::getQuery)
                .toList();
    }

}
