package com.example.Job.Offers.Aggregator.service.telegtamComandService;

import com.example.Job.Offers.Aggregator.api.MessageInterface;
import com.example.Job.Offers.Aggregator.model.Vacancy;
import com.example.Job.Offers.Aggregator.service.SubscriptionService;
import com.example.Job.Offers.Aggregator.service.TelegramCommandService;
import com.example.Job.Offers.Aggregator.service.VacancyService;
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
public class TestSubscribeCommand {

    @Mock
    private SubscriptionService subscriptionService;

    @Mock
    private VacancyService vacancyService;

    @Mock
    private MessageInterface messageInterface;

    @InjectMocks
    private TelegramCommandService telegramCommandService;

    private Long testTelegramId = 12345L;
    private String testUserName = "testUserName";
    private User testTelegramUser = new User();
    private String testQuery = "Java Developer";
    private Vacancy testVacancy = new Vacancy();
    Update update = createTextUpdate("/subscribe", testTelegramId, testUserName);

    @Test
    void subscribeCommand_shouldSendErrorMessageIfQueryIsEmpty() {
        telegramCommandService.handleUpdate(update);

        verify(messageInterface).sendMessage(eq(testTelegramId), contains("Укажите запрос для подписки"));
        verifyNoInteractions(subscriptionService);
        verifyNoInteractions(vacancyService);
    }

    @Test
    void subscribeCommand_shouldSaveSubscriptionAndSendSuccessMessageIfVacanciesExist() {
        update.getMessage().setText("/subscribe " + testQuery);

        when(subscriptionService.subscribe(eq(testTelegramUser.getId()), eq(testQuery)))
                .thenReturn(true);
        when(vacancyService.searchVacanciesAfterSubscribing(eq(testQuery), eq(testTelegramId)))
                .thenReturn(List.of(testVacancy));

        telegramCommandService.handleUpdate(update);

        verify(messageInterface).sendMessage(eq(testTelegramId), contains("Вы успешно подписались"));
        verify(messageInterface).sendMessage(eq(testTelegramId), contains("Вот что удалось найти по вашему запросу"));
    }

    @Test
    void subscribeCommand_shouldSaveSubscriptionAndSendErrorMessageIfVacanciesNotExist() {
        update.getMessage().setText("/subscribe " + testQuery);

        when(subscriptionService.subscribe(eq(testTelegramUser.getId()), eq(testQuery)))
                .thenReturn(true);
        when(vacancyService.searchVacanciesAfterSubscribing(eq(testQuery), eq(testTelegramId)))
                .thenReturn(List.of());

        telegramCommandService.handleUpdate(update);

        verify(messageInterface).sendMessage(eq(testTelegramId), contains("Вы успешно подписались"));
        verify(messageInterface).sendMessage(eq(testTelegramId), contains("По вашему запросу ничего не найдено"));
    }

    @Test
    void subscribeCommand_shouldNotSaveSubscriptionIfItExist() {
        update.getMessage().setText("/subscribe " + testQuery);

        when(subscriptionService.subscribe(eq(testTelegramUser.getId()), eq(testQuery)))
                .thenReturn(false);

        telegramCommandService.handleUpdate(update);

        verify(messageInterface).sendMessage(eq(testTelegramId), contains("Вы уже подписаны на эту вакансию"));
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
