package com.example.Job.Offers.Aggregator.service.telegtamComandService;

import com.example.Job.Offers.Aggregator.api.MessageInterface;
import com.example.Job.Offers.Aggregator.repository.UserRepository;
import com.example.Job.Offers.Aggregator.service.TelegramCommandService;
import com.example.Job.Offers.Aggregator.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class TestStartCommand {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserService userService;

    @Mock
    private MessageInterface messageInterface;

    @InjectMocks
    private TelegramCommandService telegramCommandService;

    private Long testTelegramId = 12345L;
    private String testUserName = "testUserName";
    private User testTelegramUser = new User();
    Update update = createTextUpdate("/start", testTelegramId, testUserName);

    @Test
    void handleStartCommand_shouldCallsStartCorrectly() {
        testTelegramUser.setUserName(testUserName);

        when(userRepository.findByTelegramId(testTelegramId))
                .thenReturn(Optional.empty());
        when(userService.saveNewUser(eq(testTelegramId), any(User.class)))
                .thenReturn(new com.example.Job.Offers.Aggregator.model.User());

        telegramCommandService.handleUpdate(update);

        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(messageInterface).sendMessage(eq(testTelegramId), messageCaptor.capture());

        String sentMessage = messageCaptor.getValue();
        assertTrue(sentMessage.contains("Привет, testUserName!"));
        assertTrue(sentMessage.contains("/subscribe"));
        assertTrue(sentMessage.contains("/unsubscribe"));
        assertTrue(sentMessage.contains("/unsubscribe_all"));
        assertTrue(sentMessage.contains("/list"));

        verify(userService).saveNewUser(eq(testTelegramId), any(User.class));
    }

    @Test
    void handleStartCommand_shouldNotSaveUserWhenHeExist() {
        when(userRepository.findByTelegramId(testTelegramId))
                .thenReturn(Optional.of(new com.example.Job.Offers.Aggregator.model.User()));

        telegramCommandService.handleUpdate(update);

        verify(userService, never()).saveNewUser(anyLong(), any());
    }

    private Update createTextUpdate(String text, Long telegramId, String username) {
        Update update = new Update();
        Message message = new Message();
        Chat chat = new Chat();
        chat.setId(telegramId);
        message.setChat(chat);
        message.setText(text);

        User from = new User();
        from.setUserName(username);
        message.setFrom(from);

        update.setMessage(message);
        return update;
    }

}
