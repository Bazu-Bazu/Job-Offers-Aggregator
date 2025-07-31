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

    public User saveNewUser(Long chatId, org.telegram.telegrambots.meta.api.objects.User telegramUser) {
        try {
            User newUser = new User();
            newUser.setTelegramId(chatId);
            newUser.setUsername(telegramUser.getUserName());
            userRepository.save(newUser);

            return newUser;
        } catch (Exception e) {
            log.error("Failed saving a new user {}", chatId ,e);
            throw new RuntimeException("Failed saving a new user", e);
        }
    }

}
