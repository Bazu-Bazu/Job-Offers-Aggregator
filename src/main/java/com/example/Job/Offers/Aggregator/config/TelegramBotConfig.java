package com.example.Job.Offers.Aggregator.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;


@Configuration
public class TelegramBotConfig {

    @Value("${telegram.bot.token}")
    private String botToken;

    @Value("${telegram.bot.name}")
    private String botName;

    public String getBotToken() {
        return botToken;
    }

    public String getBotName() {
        return botName;
    }

}
