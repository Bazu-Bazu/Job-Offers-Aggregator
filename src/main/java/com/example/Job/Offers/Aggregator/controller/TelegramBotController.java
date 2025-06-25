package com.example.Job.Offers.Aggregator.controller;

import com.example.Job.Offers.Aggregator.api.MessageInterface;
import com.example.Job.Offers.Aggregator.service.TelegramCommandService;
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
        telegramCommandService.handleUpdate(update);
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

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @PostConstruct
    public void init() {
        try {
            List<BotCommand> commands = List.of(
                    new BotCommand("/start", "Начало работы"),
                    new BotCommand("/subscribe", "Подписаться на вакансию"),
                    new BotCommand("/unsubscribe", "Отписаться от вакансии"),
                    new BotCommand("/list", "Список моих подписок")
            );

            execute(new SetMyCommands(commands, null, null));
        } catch (TelegramApiException e) {
            System.out.println();
        }
    }

}
