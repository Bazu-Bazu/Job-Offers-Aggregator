# Telegram-бот для поиска вакансий

Telegram-бот для поиска и фильтрации вакансий с HeadHunter API. Бот контейнеризирован с помощью Docker и использует PostgreSQL для хранения пользователей, их подписок и вакансий. 

## Возможности
- Поиск вакансий по ключевым словам.
- Фильтрация:
  - Только компании (не частные лица).
  - Только вакансии с зарплатой.
  - Премиальные вакансии.
  - За последние 7 дней.
- Ежедневная рассылка новых вакансий.

## Требования
- Установленные Docker и Docker Compose.
- Настроенная WSL2 (Windows Subsystem for Linux), если используется Windows.
- Установленная Java для локальной разработки (опционально).
- Работающие сервис PostgreSQL (через Docker или локально).

## Настройка

### 1. Клонирование репозитория
```
git clone https://github.com/Bazu-Bazu/Job-Offers-Aggregator
cd Job-Offers-Aggregator
```
### 2. Настройка переменных окружения

Создайте файл application.properties в корневой директории с следующими переменными:
```
# Токен Telegram-бота
telegram.bot.token=ваш_токен_telegram_бота

# PostgreSQL
spring.datasource.url=ваш_url_postgres
spring.datasource.username=ваш_пользователь_postgres
spring.datasource.password=ваш_пароль_postgres

```
-   Получите TELEGRAM_TOKEN, создав бота через [@BotFather](https://t.me/BotFather) в Telegram.
-   Настройте Postgres в зависимости от вашего окружения (например, используйте localhost для локальных тестов вне Docker).

### 3. Сборка и запуск с Docker

Убедитесь, что Docker и Docker Compose установлены и настроены с WSL2 (если на Windows).

#### Запуск сервисов

```
docker-compose up --build   
```

#### Остановка сервисов

```
docker-compose down
```

## Архитектура

### Компоненты

-   **Бот**: Обрабатывает обновления Telegram и взаимодействие с пользователями (src/main/java/com/example/Job/Offers/Aggregator/service/TelegramCommandService.java).
-   **Клиент API**: Взаимодействует с HeadHunter API для получения данных (src/main/java/com/example/Job/Offers/Aggregator/api/HhApiClient.java).
-   **Сервисы**: Логика бизнеса для пользователей, подписок и вакансий (src/main/java/com/example/Job/Offers/Aggregator/service).
-   **Репозитории**: Слои доступа к данным PostgreSQL (src/main/java/com/example/Job/Offers/Aggregator/repository).
-   **Сущности**: Контейнеры для данных (src/main/java/com/example/Job/Offers/Aggregator/model).

### Хранилище данных

-   **PostgreSQL**: Хранит данные пользователей, их подписки и вакансии.
