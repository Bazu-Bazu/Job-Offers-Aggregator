package com.example.Job.Offers.Aggregator.repository;

import com.example.Job.Offers.Aggregator.model.Vacancy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;


public interface VacancyRepository extends JpaRepository<Vacancy, Long> {

    @Query("SELECT v.externalId FROM Vacancy v WHERE v.externalId IN :externalIds ")
    Set<String> findExistingIds(@Param("externalIds") Set<String> externalIds);

}
