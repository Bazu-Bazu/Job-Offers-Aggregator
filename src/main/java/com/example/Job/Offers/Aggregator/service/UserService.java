package com.example.Job.Offers.Aggregator.service;

import com.example.Job.Offers.Aggregator.model.User;
import com.example.Job.Offers.Aggregator.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


@Service
@Slf4j
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User saveNewUser(Long telegramId, org.telegram.telegrambots.meta.api.objects.User telegramUser) {
        try {
            User newUser = new User();
            newUser.setTelegramId(telegramId);
            newUser.setUsername(telegramUser.getUserName());
            userRepository.save(newUser);

            return newUser;
        } catch (Exception e) {
            log.error("Error saving a new user {}", telegramId ,e);
            throw new RuntimeException("Error saving a new user", e);
        }
    }

}
