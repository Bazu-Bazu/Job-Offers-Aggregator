package com.example.Job.Offers.Aggregator.service;

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
    private final UserService userService;
    private final VacancyService vacancyService;

    @Autowired
    public TelegramCommandService(MessageInterface messageInterface, UserRepository userRepository,
                                  SubscriptionService subscriptionService, VacancyService vacancyService,
                                  UserService userService) {
        this.messageInterface = messageInterface;
        this.userRepository = userRepository;
        this.subscriptionService = subscriptionService;
        this.vacancyService = vacancyService;
        this.userService = userService;
    }

    public void handleUpdate(Update update) {
        try {
            if (update.hasMessage() && update.getMessage().hasText()) {
                String text = update.getMessage().getText();
                Long telegramId = update.getMessage().getChatId();
                User telegramUser = update.getMessage().getFrom();

                if (text.equals("/start")) {
                    handleStartCommand(telegramId, telegramUser);
                } else if (text.startsWith("/subscribe")) {
                    handleSubscribeCommand(telegramId, telegramUser, text);
                } else if (text.equals("/list")) {
                    handleListCommand(telegramId, telegramUser.getId());
                } else if (text.equals("/unsubscribe_all")) {
                    handleUnsubscribeAllCommand(telegramId, telegramUser);
                } else if (text.startsWith("/unsubscribe")) {
                    handleUnsubscribeCommand(telegramId, telegramUser, text);
                } else {
                    String message = """
                            🤨К сожалению, я не понял ваш запрос. Для взаимодействия со мной 
                            воспользуйтесь одной из следующих команд:
                            
                            /subscribe <запрос> - Подписаться на вакансии
                            /unsubscribe <запрос> - Отписаться от вакансии
                            /unsubscribe_all - Отписаться от всех вакансий
                            /list - Показать мои подписки
                            """;
                    messageInterface.sendMessage(telegramId, message);
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

    private void handleStartCommand(Long telegramId, User telegramUser) {
        try {
            userRepository.findByTelegramId(telegramId)
                    .orElseGet(() -> userService.saveNewUser(telegramId, telegramUser));

            String welcomeText = String.format("""
                    😀Привет, %s! Я бот для поиска вакансий. Вот что я умею:
                    
                    /subscribe <запрос> - Подписаться на вакансии
                    /unsubscribe <запрос> - Отписаться от вакансии
                    /unsubscribe_all - Отписаться от всех вакансий
                    /list - Показать мои подписки
                    """, telegramUser.getUserName());

            messageInterface.sendMessage(telegramId, welcomeText);
        } catch (Exception e) {
            log.error("Error handling /start command for chatId {}", telegramId, e);
            throw new RuntimeException("Error handling /start command", e);
        }
    }

    private void handleSubscribeCommand(Long telegramId, org.telegram.telegrambots.meta.api.objects.User telegramUser,
                                        String command) {
        try {
            String query = command.substring("/subscribe".length()).trim();

            if (query.isEmpty()) {
                String message = """
                        ❗Укажите запрос для подписки.
                        Например: /subscribe Java developer.
                        """;

                messageInterface.sendMessage(telegramId, message);

                return;
            }

            if (subscriptionService.subscribe(telegramUser.getId(), query)) {

                String message = String.format("""
                        ✅Вы успешно подписались на вакансию по запросу:
                        *%s*.
                        """, query);

                messageInterface.sendMessage(telegramId, message);

                List<Vacancy> vacancies = vacancyService.searchVacanciesAfterSubscribing(query, telegramId)
                        .stream()
                        .limit(5)
                        .toList();

                if (vacancies.isEmpty()) {
                    messageInterface.sendMessage(telegramId, "😔По вашему запросу ничего не найдено.");
                }
                else {
                    StringBuilder response =
                            new StringBuilder("\uD83D\uDE04Вот что удалось найти по вашему запросу:\n\n");
                    int index = 1;
                    for (Vacancy vacancy : vacancies) {
                        response.append(index).append(") ").append(vacancy.toMessage()).append("\n\n");
                        index++;
                    }

                    messageInterface.sendMessage(telegramId, response.toString());
                }

                return;
            }
            String message = """
                    ❗Вы уже подписаны на эту вакансию.
                    Укажите другой запрос для подписки.""";

            messageInterface.sendMessage(telegramId, message);
        } catch (Exception e) {
            log.error("Error handling /subscribe command for chatId {}", telegramId, e);
            throw new RuntimeException("Error handling /subscribe command", e);
        }
    }

    private void handleListCommand(Long telegramId, Long userId) {
        try {
            List<String> subscriptions = subscriptionService.getUserSubscriptions(userId);

            if (subscriptions.isEmpty()) {
                String message = "😔У вас пока нет активных подписок.";

                messageInterface.sendMessage(telegramId, message);
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
            messageInterface.sendMessage(telegramId, response.toString());
        } catch (Exception e) {
            log.error("Error handling /list command for chatId {}", telegramId, e);
            throw new RuntimeException("Error handling /list command", e);
        }
    }

    private void handleUnsubscribeCommand(Long telegramId, org.telegram.telegrambots.meta.api.objects.User telegramUser,
                                          String command) {
        try {
            String query = command.substring("/unsubscribe".length()).trim();

            if (query.isEmpty()) {
                String message = """
                        ❗Укажите запрос для отписки.
                        Например /unsubscribe Golang developer.
                        """;

                messageInterface.sendMessage(telegramId, message);

                return;
            }

            if (subscriptionService.unsubscribe(telegramUser.getId(), query)) {
                String message = String.format("""
                    ✅Вы успешно отписались от вакансии по запросу:
                    *%s*.
                    """, query);

                messageInterface.sendMessage(telegramId, message);

                return;
            }

            messageInterface.sendMessage(telegramId, "❗У вас нет такой подписки.");

        } catch (Exception e) {
            log.error("Error handling /unsubscribe command for chatId {}", telegramId, e);
            throw new RuntimeException("Error handling /unsubscribe command", e);
        }
    }

    private void handleUnsubscribeAllCommand(Long telegramId,
                                             org.telegram.telegrambots.meta.api.objects.User telegramUser) {
        try {
            if (subscriptionService.unsubscribeAll(telegramUser.getId())) {
                messageInterface.sendMessage(telegramId, "✅Вы успешно отписались от всех вакансий.");

                return;
            }

            messageInterface.sendMessage(telegramId, "❗У вас нет подписок.");
        } catch (Exception e) {
            log.error("Error handling /unsubscribeAll command for chatId {}", telegramId, e);
            throw new RuntimeException("Error handling /unsubscribeAll command", e);
        }
    }

}
