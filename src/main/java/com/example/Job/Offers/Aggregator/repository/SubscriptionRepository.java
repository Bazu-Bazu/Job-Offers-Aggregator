package com.example.Job.Offers.Aggregator.repository;

import com.example.Job.Offers.Aggregator.model.Subscription;
import com.example.Job.Offers.Aggregator.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;


public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    List<Subscription> findByUser(User user);
    Optional<Subscription> findByUserAndQuery(User user, String query);
    @Modifying
    @Query(value = "DELETE FROM subscriptions WHERE user_id = :userId AND search_query = :query", nativeQuery = true)
    void deleteByUserIdAndQuery(Long userId, String query);
    @Modifying
    @Query(value = "DELETE FROM subscriptions WHERE user_id = :userId", nativeQuery = true)
    void deleteByUserId(Long userId);

}
