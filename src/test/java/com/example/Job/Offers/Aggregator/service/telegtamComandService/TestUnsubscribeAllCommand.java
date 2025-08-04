package com.example.Job.Offers.Aggregator.service.telegtamComandService;

import com.example.Job.Offers.Aggregator.api.MessageInterface;
import com.example.Job.Offers.Aggregator.service.SubscriptionService;
import com.example.Job.Offers.Aggregator.service.TelegramCommandService;
import com.example.Job.Offers.Aggregator.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class TestUnsubscribeAllCommand {

    @Mock
    private SubscriptionService subscriptionService;

    @Mock
    private UserService userService;

    @Mock
    private MessageInterface messageInterface;

    @InjectMocks
    private TelegramCommandService telegramCommandService;

    private Long testTelegramId = 12345L;
    private String testUserName = "testUserName";
    private User testTelegramUser = new User();
    Update update = createTextUpdate("/unsubscribe_all", testTelegramId, testUserName);

    @Test
    void unsubscribeAllCommand_shouldSendErrorMessageIfUserNotHaveSubscriptions() {
        when(subscriptionService.unsubscribeAll(eq(testTelegramUser.getId())))
                .thenReturn(false);

        telegramCommandService.handleUpdate(update);

        verify(messageInterface).sendMessage(eq(testTelegramId), contains("У вас нет подписок"));
    }

    @Test
    void unsubscribeAllCommand_shouldUnsubscribeAllSubscriptionsAndSendSuccessMessage() {
        when(subscriptionService.unsubscribeAll(eq(testTelegramUser.getId())))
                .thenReturn(true);

        telegramCommandService.handleUpdate(update);

        verify(messageInterface).sendMessage(eq(testTelegramId), contains("ы успешно отписались от всех вакансий"));
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
