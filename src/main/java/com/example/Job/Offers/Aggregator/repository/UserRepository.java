package com.example.Job.Offers.Aggregator.repository;

import com.example.Job.Offers.Aggregator.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByTelegramId(Long telegramId);

}
