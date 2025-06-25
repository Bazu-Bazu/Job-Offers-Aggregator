package com.example.Job.Offers.Aggregator.service;

import com.example.Job.Offers.Aggregator.api.MessageInterface;
import com.example.Job.Offers.Aggregator.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.List;


@Service
public class TelegramCommandService {

    private final MessageInterface messageInterface;
    private final UserRepository userRepository;
    private final SubscriptionService subscriptionService;

    @Autowired
    public TelegramCommandService(MessageInterface messageInterface, UserRepository userRepository,
                                  SubscriptionService subscriptionService) {
        this.messageInterface = messageInterface;
        this.userRepository = userRepository;
        this.subscriptionService = subscriptionService;
    }

    public void handleUpdate(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String text = update.getMessage().getText();
            Long chatId = update.getMessage().getChatId();
            User telegramUser = update.getMessage().getFrom();

            if (text.equals("/start")) {
                handleStartCommand(chatId, telegramUser);
            }

            else if (text.startsWith("/subscribe")) {
                handleSubscribeCommand(chatId, telegramUser, text);
            }

            else if (text.equals("/list")) {
                handleListCommand(chatId, telegramUser.getId());
            }

            else if (text.startsWith("/unsubscribe")) {
                handleUnsubscribeCommand(chatId, telegramUser, text);
            }
        }
    }

    private void handleStartCommand(Long chatId, User telegramUser) {
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
                /list - Показать мои подписки
                """, telegramUser.getUserName());

        messageInterface.sendMessage(chatId, welcomeText);
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

                return;
            }
            String message = """
                    ❗Вы уже подписаны на эту вакансию.
                    Укажите другой запрос для подписки.""";

            messageInterface.sendMessage(chatId, message);
        } catch (Exception e) {
            messageInterface.sendMessage(chatId, "‼\uFE0FОшибка при обработке подписки. Попробуйте позже.");
        }
    }

    private void handleListCommand(Long chatId, Long userId) {
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

        response.append("\nДля отписки используйте /unsubscribe <запрос>");
        messageInterface.sendMessage(chatId, response.toString());
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
            messageInterface.sendMessage(chatId, "‼\uFE0FОшибка при обработке отписки. Попробуйте позже.");
        }
    }
}
