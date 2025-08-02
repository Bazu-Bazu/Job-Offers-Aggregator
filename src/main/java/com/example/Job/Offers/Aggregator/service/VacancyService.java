package com.example.Job.Offers.Aggregator.service;

import com.example.Job.Offers.Aggregator.api.HhApiClient;
import com.example.Job.Offers.Aggregator.api.MessageInterface;
import com.example.Job.Offers.Aggregator.model.Subscription;
import com.example.Job.Offers.Aggregator.model.User;
import com.example.Job.Offers.Aggregator.model.Vacancy;
import com.example.Job.Offers.Aggregator.repository.SubscriptionRepository;
import com.example.Job.Offers.Aggregator.repository.UserRepository;
import com.example.Job.Offers.Aggregator.repository.VacancyRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;


@Service
@Slf4j
public class VacancyService {

    private final VacancyRepository vacancyRepository;
    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final HhApiClient hhApiClient;
    private final String area = "1";
    private final MessageInterface messageInterface;

    public VacancyService(VacancyRepository vacancyRepository, UserRepository userRepository,
                          SubscriptionRepository subscriptionRepository, HhApiClient hhApiClient, MessageInterface messageInterface) {
        this.vacancyRepository = vacancyRepository;
        this.userRepository = userRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.hhApiClient = hhApiClient;
        this.messageInterface = messageInterface;
    }

    @Transactional
    public void saveNewVacancies(List<Vacancy> vacancies, User user, Subscription subscription) {
        vacancies.forEach(v -> {
            if (v.getExternalId() == null) {
                v.setExternalId(generateExternalId(v));
            }
            if (v.getPublishedAt() == null) {
                v.setPublishedAt(LocalDateTime.now());
            }
            v.setUser(user);
            v.setSubscription(subscription);
        });

        Set<String> allExternalIds = vacancies.stream()
                .map(Vacancy::getExternalId)
                .collect(Collectors.toSet());

        Set<String> existingIds = vacancyRepository.findExistingIds(allExternalIds);

        List<Vacancy> newVacancies = vacancies.stream()
                .filter(v -> !existingIds.contains(v.getExternalId()))
                .toList();

        if (!newVacancies.isEmpty()) {
            vacancyRepository.saveAll(newVacancies);
        }
    }

    @Transactional
    public List<Vacancy> searchVacancyAfterSubscribing(String query, Long chatId) {
        com.example.Job.Offers.Aggregator.model.User user = userRepository.findByTelegramId(chatId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Subscription subscription = subscriptionRepository.findByUserAndQuery(user, query)
                .orElseThrow(() -> new RuntimeException("Subscription not found"));

        List<Vacancy> vacancies = hhApiClient.searchVacancies(query, area);
        saveNewVacancies(vacancies.stream().limit(5).toList(), user, subscription);
        return vacancies;
    }

    private String generateExternalId(Vacancy vacancy) {
        return "gen-" + UUID.nameUUIDFromBytes(
                (vacancy.getEmployer() + "-" + vacancy.getTitle()).getBytes(StandardCharsets.UTF_8));
    }

    @Scheduled(cron = "0 00 10 * * ?")
    @Transactional
    public void sendDailyVacancies() {
        Map<Subscription, List<User>> subscriptionUserMap = subscriptionRepository.findAll()
                .stream()
                .collect(Collectors.groupingBy(
                        subscription -> subscription,
                        Collectors.mapping(Subscription::getUser, Collectors.toList())
                ));

        subscriptionUserMap.forEach((subscription, users) -> {
            try {
                String query = subscription.getQuery();

                List<Vacancy> freshVacancies = hhApiClient.searchVacancies(query, area);

                if (!freshVacancies.isEmpty()) {
                    freshVacancies.forEach(v -> v.setExternalId(generateExternalId(v)));

                    users.forEach(user -> {
                        List<Vacancy> userNewVacancies = filterNewVacancies(freshVacancies, user, subscription).
                                stream()
                                .limit(5)
                                .toList();

                        if (!userNewVacancies.isEmpty()) {
                            saveNewVacancies(userNewVacancies, user, subscription);
                            sendVacanciesToUsers(query, userNewVacancies, user);
                        }
                    });
                }
            } catch (Exception e) {
                log.error("Error processing subscription: {}", subscription.getId(), e);
                throw new RuntimeException("Error processing subscription", e);
            }
        });
    }

    private void sendVacanciesToUsers(String query, List<Vacancy> newVacancies, User user) {
            try {
                String header = "🔔 Новые вакансии по запросу \"" + query + "\":\n\n";
                String message = header + newVacancies.stream()
                        .map(Vacancy::toMessage)
                        .collect(Collectors.joining("\n\n"));

                messageInterface.sendMessage(user.getTelegramId(), message);
            } catch (Exception e) {
                log.error("Failed to send vacancies to User {}", user.getTelegramId(), e);
                throw new RuntimeException("Failed to send vacancies to User", e);
            }
    }

    private List<Vacancy> filterNewVacancies(List<Vacancy> vacancies, User user, Subscription subscription) {
        Set<String> existingVacancies = vacancyRepository.findExternalIdsByUserAndSubscription(user, subscription);

        return vacancies.stream()
                .filter(v -> !existingVacancies.contains(v.getExternalId()))
                .toList();
    }

}
