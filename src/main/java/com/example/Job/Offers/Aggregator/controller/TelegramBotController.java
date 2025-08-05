package com.example.Job.Offers.Aggregator.controller;

import com.example.Job.Offers.Aggregator.api.MessageInterface;
import com.example.Job.Offers.Aggregator.service.TelegramCommandService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import javax.annotation.PostConstruct;
import java.util.List;


@Component
@Slf4j
public class TelegramBotController extends TelegramLongPollingBot implements MessageInterface {

    private final TelegramCommandService telegramCommandService;

    @Value("${telegram.bot.name}")
    private String botName;

    @Value("${telegram.bot.token}")
    private String botToken;

    @Autowired
    public TelegramBotController(@Lazy TelegramCommandService telegramCommandService) {
        this.telegramCommandService = telegramCommandService;
    }

    @Override
    public void onUpdateReceived(Update update) {
        try {
            telegramCommandService.handleUpdate(update);
        } catch (Exception e) {
            log.error("Error processing update: {}", update, e);
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

    @Override
    public void sendMessage(Long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(text);
        message.disableWebPagePreview();

        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Error sending message to chat {} : {}", chatId, text, e);
        }
    }

    @PostConstruct
    public void init() {
        try {
            List<BotCommand> commands = List.of(
                    new BotCommand("/start", "Начало работы"),
                    new BotCommand("/subscribe", "Подписаться на вакансию"),
                    new BotCommand("/unsubscribe", "Отписаться от вакансии"),
                    new BotCommand("/unsubscribe_all", "Отписаться от всех вакансий"),
                    new BotCommand("/list", "Список моих подписок")
            );

            execute(new SetMyCommands(commands, null, null));
        } catch (TelegramApiException e) {
            log.error("Error initializing bot commands", e);
        }
    }

}
