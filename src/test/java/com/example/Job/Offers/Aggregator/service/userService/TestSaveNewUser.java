package com.example.Job.Offers.Aggregator.service.userService;

import com.example.Job.Offers.Aggregator.model.User;
import com.example.Job.Offers.Aggregator.repository.UserRepository;
import com.example.Job.Offers.Aggregator.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class TestSaveNewUser {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private final Long testTelegramId = 123456789L;
    private final org.telegram.telegrambots.meta.api.objects.User testTelegramUser =
            new org.telegram.telegrambots.meta.api.objects.User(123456789L, "testUser", false);

    @Test
    void saveNewUser_shouldSaveAndReturnNewUser() {
        User newUser = new User();
        newUser.setTelegramId(testTelegramId);
        newUser.setUsername(testTelegramUser.getUserName());

        when(userRepository.save(any(User.class)))
                .thenReturn(newUser);

        User savedNewUser = userService.saveNewUser(testTelegramId, testTelegramUser);

        assertNotNull(savedNewUser);
        assertEquals(testTelegramId, savedNewUser.getTelegramId());
        assertEquals(testTelegramUser.getUserName(), savedNewUser.getUsername());
        verify(userRepository).save(any(User.class));
    }

}
