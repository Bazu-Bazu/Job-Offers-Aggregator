package com.example.Job.Offers.Aggregator.repository;

import com.example.Job.Offers.Aggregator.model.Vacancy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;


public interface VacancyRepository extends JpaRepository<Vacancy, Long> {

    Optional<Vacancy> findByExternalId(String externalId);
    @Query(value = "SELECT * FROM vacancies WHERE user_id = :userId ORDER BY published_at DESC", nativeQuery = true)
    List<Vacancy> findByUserId(Long userId);
    boolean existsByExternalId(String externalId);

}
