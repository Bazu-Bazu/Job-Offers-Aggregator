package com.example.Job.Offers.Aggregator.service;

import com.example.Job.Offers.Aggregator.model.Subscription;
import com.example.Job.Offers.Aggregator.model.User;
import com.example.Job.Offers.Aggregator.repository.SubscriptionRepository;
import com.example.Job.Offers.Aggregator.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;

    public SubscriptionService(SubscriptionRepository subscriptionRepository, UserRepository userRepository) {
        this.subscriptionRepository = subscriptionRepository;
        this.userRepository = userRepository;
    }

    @Transactional
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

    @Transactional
    public boolean unsubscribe(Long telegramId, String query) {
        User user = userRepository.findByTelegramId(telegramId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (subscriptionRepository.findByUserAndQuery(user, query).isPresent()) {
            subscriptionRepository.deleteByUserIdAndQuery(user.getId(), query);
            return true;
        }

        return false;
    }

    @Transactional
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
