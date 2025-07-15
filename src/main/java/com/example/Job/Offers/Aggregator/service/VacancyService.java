package com.example.Job.Offers.Aggregator.service;

import com.example.Job.Offers.Aggregator.controller.TelegramBotController;
import com.example.Job.Offers.Aggregator.model.Vacancy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


@Service
@Slf4j
public class VacancyService {

    private final HhIntegrationService hhIntegrationService;
    private final TelegramBotController telegramBotController;

    public VacancyService(HhIntegrationService hhIntegrationService, TelegramBotController telegramBotController) {
        this.hhIntegrationService = hhIntegrationService;
        this.telegramBotController = telegramBotController;
    }

    public void processVacancySearch(Long chatId, String query) {
        hhIntegrationService.fetchAndSaveVacancies(query)
                .subscribe(vacancy -> {
                    String message = formatVacancyMessage(vacancy);
                    telegramBotController.sendMessage(chatId, message);
                }, error -> {
                    telegramBotController.sendMessage(chatId, "❗Ошибка при поиске вакансий");
                    log.error("Search error", error);
                });
    }

    private String formatVacancyMessage(Vacancy vacancy) {
        return String.format(
                "🏢 %s\n💰 %s\n🏭 %s\n🔗 https://hh.ru/vacancy/%s",
                vacancy.getTitle(),
                vacancy.getSalary(),
                vacancy.getCompany(),
                vacancy.getExternalId()
        );
    }
}
