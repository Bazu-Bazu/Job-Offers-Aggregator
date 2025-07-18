package com.example.Job.Offers.Aggregator.service;

import com.example.Job.Offers.Aggregator.api.HhApiClient;
import com.example.Job.Offers.Aggregator.api.MessageInterface;
import com.example.Job.Offers.Aggregator.model.Vacancy;
import com.example.Job.Offers.Aggregator.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.List;


@Service
@Slf4j
public class TelegramCommandService {

    private final MessageInterface messageInterface;
    private final UserRepository userRepository;
    private final SubscriptionService subscriptionService;
    private final HhApiClient hhApiClient;

    @Autowired
    public TelegramCommandService(MessageInterface messageInterface, UserRepository userRepository,
                                  SubscriptionService subscriptionService, HhApiClient hhApiClient) {
        this.messageInterface = messageInterface;
        this.userRepository = userRepository;
        this.subscriptionService = subscriptionService;
        this.hhApiClient = hhApiClient;
    }

    public void handleUpdate(Update update) {
        try {
            if (update.hasMessage() && update.getMessage().hasText()) {
                String text = update.getMessage().getText();
                Long chatId = update.getMessage().getChatId();
                User telegramUser = update.getMessage().getFrom();

                if (text.equals("/start")) {
                    handleStartCommand(chatId, telegramUser);
                } else if (text.startsWith("/subscribe")) {
                    handleSubscribeCommand(chatId, telegramUser, text);
                } else if (text.equals("/list")) {
                    handleListCommand(chatId, telegramUser.getId());
                } else if (text.equals("/unsubscribe_all")) {
                    handleUnsubscribeAllCommand(chatId, telegramUser);
                } else if (text.startsWith("/unsubscribe")) {
                    handleUnsubscribeCommand(chatId, telegramUser, text);
                }
            }
        } catch (Exception e) {
            log.error("Error processing update: {}", update, e);
            if (update.hasMessage()) {
                messageInterface.sendMessage(update.getMessage().getChatId(), "‼\uFE0FПроизошла ошибка при " +
                        "обработке команды. Пожалуйста, попробуйте позже.");
            }
        }
    }

    private void handleStartCommand(Long chatId, User telegramUser) {
        try {
            userRepository.findByTelegramId(chatId).orElseGet(() -> {
                com.example.Job.Offers.Aggregator.model.User newUser = new com.example.Job.Offers.Aggregator.model.User();
                newUser.setTelegramId(chatId);
                newUser.setUsername(telegramUser.getUserName());
                return userRepository.save(newUser);
            });

            String welcomeText = String.format("""
                    😀Привет, %s! Я бот для поиска вакансий. Вот что я умею:
                    
                    /subscribe <запрос> - Подписаться на вакансии
                    /unsubscribe <запрос> - Отписаться от вакансии
                    /unsubscribe_all - Отписаться от всех вакансий
                    /list - Показать мои подписки
                    """, telegramUser.getUserName());

            messageInterface.sendMessage(chatId, welcomeText);
        } catch (Exception e) {
            log.error("Error handling /start command for chatId {}", chatId, e);
            throw new RuntimeException("Error handling /start command", e);
        }
    }

    private void handleSubscribeCommand(Long chatId, org.telegram.telegrambots.meta.api.objects.User telegramUser,
                                        String command) {
        try {
            String query = command.substring("/subscribe".length()).trim();

            if (query.isEmpty()) {
                String message = """
                        ❗Укажите запрос для подписки.
                        Например: /subscribe Java developer.
                        """;

                messageInterface.sendMessage(chatId, message);

                return;
            }

            if (subscriptionService.subscribe(telegramUser.getId(), query)) {

                String message = String.format("""
                        ✅Вы успешно подписались на вакансию по запросу:
                        *%s*.
                        """, query);

                messageInterface.sendMessage(chatId, message);

                searchVacancy(chatId, query);

                return;
            }
            String message = """
                    ❗Вы уже подписаны на эту вакансию.
                    Укажите другой запрос для подписки.""";

            messageInterface.sendMessage(chatId, message);
        } catch (Exception e) {
            log.error("Error handling /subscribe command for chatId {}", chatId, e);
            throw new RuntimeException("Error handling /subscribe command", e);
        }
    }

    private void searchVacancy(Long chatId, String query) {
        String area = "1";

        List<Vacancy> vacancies = hhApiClient.searchVacancies(query, area);
        if (vacancies.isEmpty()) {
            messageInterface.sendMessage(chatId, "По вашему запросу ничего не найдено.");
        }
        else {
            vacancies.stream()
                    .limit(5)
                    .forEach(vacancy -> messageInterface.sendMessage(chatId, vacancy.toMessage()));
        }
    }

    private void handleListCommand(Long chatId, Long userId) {
        try {
            List<String> subscriptions = subscriptionService.getUserSubscriptions(userId);

            if (subscriptions.isEmpty()) {
                String message = "😔У вас пока нет активных подписок.";

                messageInterface.sendMessage(chatId, message);
                return;
            }

            StringBuilder response = new StringBuilder("\uD83D\uDCDDВаши подписки:\n\n");
            int i = 1;
            for (String sub : subscriptions) {
                response.append(i).append(")").append(" ").append(sub).append("\n");
                i++;
            }

            response.append("\nДля отписки от конкретной вакансии используйте /unsubscribe <запрос>\n");
            response.append("\nДля отписки от всех вакансий используйте /unsubscribe_all");
            messageInterface.sendMessage(chatId, response.toString());
        } catch (Exception e) {
            log.error("Error handling /list command for chatId {}", chatId, e);
            throw new RuntimeException("Error handling /list command", e);
        }
    }

    private void handleUnsubscribeCommand(Long chatId, org.telegram.telegrambots.meta.api.objects.User telegramUser,
                                          String command) {
        try {
            String query = command.substring("/unsubscribe".length()).trim();

            if (query.isEmpty()) {
                String message = """
                        ❗Укажите запрос для отписки.
                        Например /unsubscribe Golang developer.
                        """;

                messageInterface.sendMessage(chatId, message);

                return;
            }

            if (subscriptionService.unsubscribe(telegramUser.getId(), query)) {
                String message = String.format("""
                    ✅Вы успешно отписались от вакансии по запросу:
                    *%s*.
                    """, query);

                messageInterface.sendMessage(chatId, message);

                return;
            }

            messageInterface.sendMessage(chatId, "❗У вас нет такой подписки.");

        } catch (Exception e) {
            log.error("Error handling /unsubscribe command for chatId {}", chatId, e);
            throw new RuntimeException("Error handling /unsubscribe command", e);
        }
    }

    private void handleUnsubscribeAllCommand(Long chatId,
                                             org.telegram.telegrambots.meta.api.objects.User telegramUser) {
        try {
            if (subscriptionService.unsubscribeAll(telegramUser.getId())) {
                messageInterface.sendMessage(chatId, "✅Вы успешно отписались от всех вакансий.");

                return;
            }

            messageInterface.sendMessage(chatId, "❗У вас нет подписок.");
        } catch (Exception e) {
            log.error("Error handling /unsubscribeAll command for chatId {}", chatId, e);
            throw new RuntimeException("Error handling /unsubscribeAll command", e);
        }
    }
}
