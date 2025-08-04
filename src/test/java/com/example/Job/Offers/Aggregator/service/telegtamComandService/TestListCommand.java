package com.example.Job.Offers.Aggregator.service.telegtamComandService;

import com.example.Job.Offers.Aggregator.api.MessageInterface;
import com.example.Job.Offers.Aggregator.service.SubscriptionService;
import com.example.Job.Offers.Aggregator.service.TelegramCommandService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.List;

import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class TestListCommand {

    @Mock
    private SubscriptionService subscriptionService;

    @Mock
    private MessageInterface messageInterface;

    @InjectMocks
    private TelegramCommandService telegramCommandService;

    private Long testTelegramId = 12345L;
    private String testUserName = "testUserName";
    private User testTelegramUser = new User();
    private String testQuery = "Golang Developer";
    Update update = createTextUpdate("/list", testTelegramId, testUserName);

    @Test
    void listCommand_shouldSendErrorMessageWhenUserNotNaveSubscriptions() {
        when(subscriptionService.getUserSubscriptions(eq(testTelegramUser.getId())))
                .thenReturn(List.of());

        telegramCommandService.handleUpdate(update);

        verify(messageInterface).sendMessage(eq(testTelegramId), contains("У вас пока нет активных подписок"));
    }

    @Test
    void listCommand_shouldSendSuccessMessageWhenUserHaveSubscriptions() {
        when(subscriptionService.getUserSubscriptions(eq(testTelegramUser.getId())))
                .thenReturn(List.of(testQuery));

        telegramCommandService.handleUpdate(update);

        verify(messageInterface).sendMessage(eq(testTelegramId), contains("Ваши подписки"));
        verify(messageInterface).sendMessage(eq(testTelegramId), contains(testQuery));
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
