package com.example.Job.Offers.Aggregator.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;


@Configuration
public class TelegramBotConfig {

    @Value("${telegram.bot.token}")
    private String botToken;

}
