package com.example.Job.Offers.Aggregator.repository;

import com.example.Job.Offers.Aggregator.model.Subscription;
import com.example.Job.Offers.Aggregator.model.User;
import org.aspectj.apache.bcel.classfile.Module;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    List<Subscription> findByUser(User user);
    Optional<Subscription> findByUserAndQuery(User user, String query);
    void deleteByUserAndQuery(User user, String query);

}
