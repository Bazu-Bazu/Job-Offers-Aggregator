# Базовый образ с JDK 17
FROM eclipse-temurin:17-jdk-jammy

# Рабочая директория
WORKDIR /app

# Копируем Maven wrapper and settings
COPY mvnw .
COPY .mvn .mvn
COPY . .

# Делаем mvnw исполняемым
RUN chmod +x ./mvnw

# Собираем приложение
RUN ./mvnw clean package -DskipTests

# Порт приложения
EXPOSE 8080

# Команда запуска
ENTRYPOINT ["java", "-jar", "target/Job-Offers-Aggregator-0.0.1-SNAPSHOT.jar"]