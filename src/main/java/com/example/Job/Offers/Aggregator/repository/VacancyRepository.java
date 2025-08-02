package com.example.Job.Offers.Aggregator.repository;

import com.example.Job.Offers.Aggregator.model.Subscription;
import com.example.Job.Offers.Aggregator.model.User;
import com.example.Job.Offers.Aggregator.model.Vacancy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;


public interface VacancyRepository extends JpaRepository<Vacancy, Long> {

    @Query("SELECT v.externalId FROM Vacancy v WHERE v.externalId IN :externalIds ")
    Set<String> findExistingIds(@Param("externalIds") Set<String> externalIds);

    Set<Vacancy> findByUserAndSubscription(User user, Subscription subscription);

    @Query("SELECT v.externalId FROM Vacancy v WHERE v.user = :user AND v.subscription = :subscription")
    Set<String> findExternalIdsByUserAndSubscription(@Param("user") User user,
                                                     @Param("subscription") Subscription subscription);

}
