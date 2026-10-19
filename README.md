# java-kanban

[![CI](https://github.com/vd89zen/java-kanban/actions/workflows/ci.yml/badge.svg)](https://github.com/vd89zen/java-kanban/actions/workflows/ci.yml)
![Coverage](https://img.shields.io/badge/coverage-80%25-green)
![Java](https://img.shields.io/badge/Java-17-blue)

Бэкенд-сервис для управления задачами по методологии Kanban. REST API построен на
стандартном `com.sun.net.httpserver.HttpServer` без веб-фреймворков, JSON — через Gson.
Позволяет вести задачи, эпики и подзадачи, отслеживать историю просмотров, приоритеты
по времени старта и пересечения интервалов. Данные хранятся в памяти и персистятся
в CSV, потокобезопасность обеспечена на уровне менеджера.

## Возможности

- **Задачи, эпики, подзадачи:** CRUD, пагинация, фильтр по статусу, сортировка.
- **Эпики:** автоматический пересчёт статуса, длительности и границ по подзадачам.
- **История просмотров:** собственная двусвязная структура, O(1) на операции.
- **Приоритеты:** `ConcurrentSkipListSet` по времени старта, отдельный эндпоинт.
- **Пересечение интервалов:** `409 Conflict` при попытке занять один момент времени.
- **Персистенция:** `CsvStorage` — сохранение в CSV и восстановление при старте.
- **HTTP API:** корректные коды (`201 + Location` при создании, `204` на удаление,
  `415` на неверный `Content-Type`, `405` на неподдерживаемый метод).
- **Потокобезопасность:** `ConcurrentHashMap` + единый лок на мутации, `ExecutorService`
  в сервере (8 потоков).

## Стек

| Слой | Технология |
|---|---|
| Язык | Java 17 |
| HTTP-сервер | `com.sun.net.httpserver.HttpServer` (JDK) |
| Сериализация | Gson 2.11.0 + кастомные адаптеры |
| Хранение | In-memory + CSV, интерфейс `TaskStorage` |
| Логирование | SLF4J 2.0.13 + Logback 1.5.6 |
| Тесты | JUnit 5.10.2 (включая параметризованные) |
| Качество кода | Checkstyle, SpotBugs, JaCoCo |
| Сборка | Maven + Maven Wrapper, GitHub Actions |

## Быстрый старт

```bash
git clone https://github.com/vd89zen/java-kanban.git
cd java-kanban
./mvnw clean package
java -jar target/java-kanban.jar
```

Приложение поднимется на `http://localhost:8080`. Файл `tasks.csv` появится в текущей
директории после первой мутации.

```bash
curl http://localhost:8080/health
curl http://localhost:8080/tasks
```

Создание задачи:

```bash
curl -X POST http://localhost:8080/tasks \
  -H "Content-Type: application/json" \
  -d '{
        "name": "Сделать ревью",
        "description": "Проверить PR",
        "statusProgress": "NEW",
        "durationInMinutes": 60,
        "startDateTime": "10:00, 15:10:2026"
      }'
```

## Требования

| Компонент | Версия |
|---|---|
| JDK | 17+ (проверено на 17 и 21) |
| Maven | не нужен — используется Maven Wrapper (`./mvnw`) |
| Свободный порт | 8080 |

**Переменные окружения:** `SERVER_PORT` / `-Dserver.port` (дефолт 8080),
`DATA_FILE` / `-Ddata.file` (дефолт `./tasks.csv`).

## Тестирование

```bash
./mvnw clean test      # 200+ тестов, ~80% покрытие
./mvnw clean verify    # Checkstyle + тесты + SpotBugs + JaCoCo
```

Подробности — в [`docs/testing.md`](docs/testing.md).

## Документация

| Раздел | Файл |
|---|---|
| Архитектура и структура пакетов | [`docs/architecture.md`](docs/architecture.md) |
| API (эндпоинты, примеры) | [`docs/api.md`](docs/api.md) |
| Хранение и CSV-формат | [`docs/storage.md`](docs/storage.md) |
| Обработка ошибок | [`docs/errors.md`](docs/errors.md) |
| Конфигурация | [`docs/configuration.md`](docs/configuration.md) |
| Тестирование и качество кода | [`docs/testing.md`](docs/testing.md) |
| Полный контекст проекта | [`context.md`](context.md) |

---
Проект разработан в рамках программы по Java-разработке на платформе Яндекс Практикум.
Реализация и дальнейшие доработки выполнены автором.