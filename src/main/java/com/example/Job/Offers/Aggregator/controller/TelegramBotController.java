package com.example.Job.Offers.Aggregator.controller;

import com.example.Job.Offers.Aggregator.model.User;
import com.example.Job.Offers.Aggregator.repository.UserRepository;
import com.example.Job.Offers.Aggregator.service.SubscruptionService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;


@Component
public class TelegramBotController extends TelegramLongPollingBot {

    private final SubscruptionService subscruptionService;
    private final UserRepository userRepository;
    @Value("${telegram.bot.name}")
    private String botName;
    @Value("${telegram.bot.token}")
    private String botToken;

    public TelegramBotController(SubscruptionService subscruptionService, UserRepository userRepository) {
        this.subscruptionService = subscruptionService;
        this.userRepository = userRepository;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String text = update.getMessage().getText();
            Long chatId = update.getMessage().getChatId();
            org.telegram.telegrambots.meta.api.objects.User telegramUser = update.getMessage().getFrom();

            if (text.equals("/start")) {
                User user = userRepository.findByTelegramId(chatId)
                        .orElseGet(() -> {
                            User newUser = new User();
                            newUser.setTelegramId(chatId);
                            newUser.setUsername(update.getMessage().getFrom().getUserName());
                            return userRepository.save(newUser);
                        });

                sendWelcomeMessage(update);
            }

            else if (text.startsWith("/subscribe")) {
                handleSubscribeCommand(chatId, telegramUser, text);
            }

            else if (text.equals("/list")) {
                handleListCommand(chatId, telegramUser.getId());
            }
        }
    }

    @Override
    public String getBotUsername() {
        return botName;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    private void sendMessage(Long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(text);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendWelcomeMessage(Update update) {
        Long chatId = update.getMessage().getChatId();
        String username = update.getMessage().getFrom().getFirstName();

        String welcomeText = String.format("""
                😀Привет, %s! Я бот для поиска вакансий. Вот что я умею:
                
                /subscribe <запрос> - Подписаться на вакансии
                /unsubscribe <запрос> - Отписаться от вакансии
                /list - Показать мои подписки
                """, username);

        sendMessage(chatId, welcomeText);
    }

    private void handleSubscribeCommand(Long chatId, org.telegram.telegrambots.meta.api.objects.User telegramUser,
                                        String command) {
        try {
            String query = command.substring("/subscribe".length()).trim();

            if (query.isEmpty()) {
                sendMessage(chatId, """
                        ❗Укажите запрос для подписки.
                        Например: /subscribe Java developer.
                        """);

                return;
            }

            if (subscruptionService.subscribe(telegramUser.getId(), query)) {

                String message = String.format("""
                        ✅Вы успешно подписались на вакансию по запросу:
                        *%s*.
                        """, query);

                sendMessage(chatId, message);
            }
            else {
                String message = """
                        ❗Вы уже подписаны на эту вакансию.
                        Укажите другой запрос для подписки.""";

                sendMessage(chatId, message);
            }
        } catch (Exception e) {
            sendMessage(chatId, "‼\uFE0FОшибка при обработке подписки. Попробуйте позже.");
        }
    }

    private void handleListCommand(Long chatId, Long userId) {
        List<String> subscriptions = subscruptionService.getUserSubscriptions(userId);

        if (subscriptions.isEmpty()) {
            String message = "😔У вас пока нет активных подписок.";

            sendMessage(chatId, message);
            return;
        }

        StringBuilder response = new StringBuilder("\uD83D\uDCDDВаши подписки:\n\n");
        int i = 1;
        for (String sub : subscriptions) {
            response.append(i).append(")").append(" ").append(sub).append("\n");
            i++;
        }

        response.append("\nДля отписки используйте /unsubscribe <запрос>");
        sendMessage(chatId, response.toString());
    }

}
