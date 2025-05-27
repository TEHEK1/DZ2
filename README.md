# Система проверки текстов на плагиат

Система для загрузки, хранения и анализа текстовых файлов на предмет плагиата. Проект построен на микросервисной архитектуре с использованием Spring Boot.

## Архитектура

Система состоит из следующих микросервисов:

### 1. API Gateway (`api-gateway`)
- Единая точка входа для всех клиентских запросов
- Маршрутизация запросов к соответствующим сервисам
- Документация API через Swagger UI
- Обработка ошибок и логирование

### 2. File Storing Service (`file-storing-service`)
- Управление хранением файлов
- Загрузка и получение текстовых файлов
- Проверка плагиата между файлами
- Хранение метаданных файлов в PostgreSQL

### 3. File Analysis Service (`file-analysis-service`)
- Анализ содержимого файлов
- Извлечение метаданных
- Генерация отчетов об анализе
- Хранение результатов анализа в PostgreSQL

### 4. Common DTO (`common-dto`)
- Общие DTO классы для обмена данными между сервисами
- Модели данных и интерфейсы

## Технологии

- Java 17
- Spring Boot 3.2.5
- Spring Data JPA
- PostgreSQL
- Docker & Docker Compose
- Maven
- Lombok
- Swagger/OpenAPI

## Требования

- JDK 17 или выше
- Maven 3.8 или выше
- Docker и Docker Compose
- PostgreSQL (если запуск без Docker)

## Установка и запуск

### 1. Сборка проекта

```bash
mvn clean install
```

### 2. Запуск через Docker Compose

```bash
# Запуск всех сервисов
docker-compose up --build

# Запуск в фоновом режиме
docker-compose up -d --build

# Остановка сервисов
docker-compose down
```

## API Endpoints

После запуска API документация доступна по адресу: `http://localhost:8080/swagger-ui.html`
## Конфигурация

Основные настройки находятся в файлах `application.properties` каждого сервиса:

- `api-gateway/src/main/resources/application.properties`
- `file-storing-service/src/main/resources/application.properties`
- `file-analysis-service/src/main/resources/application.properties`