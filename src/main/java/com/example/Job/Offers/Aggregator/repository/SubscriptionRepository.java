package com.example.Job.Offers.Aggregator.repository;

import com.example.Job.Offers.Aggregator.model.Subscription;
import com.example.Job.Offers.Aggregator.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;


public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    List<Subscription> findAll();
    List<Subscription> findByUser(User user);
    Optional<Subscription> findByUserAndQuery(User user, String query);

    @Modifying
    @Query("DELETE FROM Subscription s WHERE s.user.id = :userId AND s.query = :query")
    void deleteByUserAndQuery(@Param("userId") Long userId, @Param("query") String query);

    @Modifying
    @Query("DELETE FROM Subscription s WHERE s.user.id = :userId")
    void deleteByUser(@Param("userId") Long userId);

}
