# java-kanban — контекст для продолжения работы

## Проект

Backend-сервис для управления задачами по методологии Kanban. Портфолио-проект на базе
учебного Яндекс Практикума, в процессе доработки.

Репозиторий: https://github.com/vd89zen/java-kanban

**Стек (текущий):** Java 17+, `com.sun.net.httpserver.HttpServer`, Gson, JUnit 5.
**Стек (целевой после доработки):** + Maven, SLF4J + Logback, без смены остального.

**Окружение:** Windows, IntelliJ IDEA, JDK 17+.

## Архитектура (текущая)

```
Main
  └── HttpTaskServer (com.sun.net.httpserver)
        ├── /tasks        → TasksHandler
        ├── /subtasks     → SubtasksHandler
        ├── /epics        → EpicsHandler
        ├── /history      → HistoryHandler
        └── /prioritized  → PrioritizedHandler
                │
                ▼
        TaskManager (interface)
        ├── InMemoryTaskManager
        │      └── InMemoryHistoryManager (LinkedHashMapCustom)
        └── FileBackedTaskManager (CSV поверх InMemory)
```

Пакеты:

| Пакет | Назначение |
|---|---|
| `http` | `HttpTaskServer` — регистрация контекстов |
| `http.handler` | Хендлеры, `BaseHttpHandler` |
| `manage` | `ManagersUtil` — фабрика менеджеров |
| `service` | `InMemoryTaskManager`, `FileBackedTaskManager`, `InMemoryHistoryManager` |
| `service.interfaces` | `TaskManager`, `HistoryManager` |
| `task` | `Task`, `Epic`, `Subtask` |
| `utils` | `AbstractTask`, `LinkedHashMapCustom`, `TextForGson`, `CommaSeparatedValuesCreateProcess`, `DataForUserScenario` |
| `utils.enums` | `StatusProgress`, `TypesTasks`, `ResultOfOperation`, `HttpResponseCode` |
| `utils.gson` | `LocalDateTimeTypeAdapter`, `DurationTypeAdapter`, `AbstractTaskDeserializer` |
| `exceptions` | `BadRequestException`, `NotFoundException`, `ManagerSaveException` |

Сущности: `Task`, `Epic`, `Subtask` (наследники `AbstractTask`), enum-ы `StatusProgress`,
`TypesTasks`, `ResultOfOperation`, `HttpResponseCode`.

## Что уже сделано

- Учебный проект завершён и работает: HTTP-API, история, приоритеты, CSV-хранилище, тесты.
- Проведён аудит кода и тестов, составлен план доработки (см. раздел «Что делаем сейчас»).
- Рефакторинг кода **не начинался**. Все пункты плана — в статусе «запланировано».

## Что делаем сейчас

**Активный этап:** Этап 0 (подготовка) — ещё не начат.
**Следующий шаг:** создать `pom.xml`, переложить исходники в Maven-структуру, обновить `.gitignore`.

Полный план доработки — в отдельном документе (обсуждается в чате, будет в репозитории
как `PLAN.md` или `docs/plan.md`).

## Принятые решения (по плану)

Зафиксировано для доработки:

| № | Решение |
|---|---|
| 1 | Система сборки: **Maven**, минимально необходимый `pom.xml` |
| 2 | Логирование: **SLF4J + Logback** |
| 3 | Путь к данным по умолчанию: **`./tasks.csv`** (через env `DATA_FILE` / sysprop `data.file`) |
| 4 | `DataForUserScenario` — **удалить** (наполнение в тестах делается явно) |
| 5 | `DELETE` → **204 No Content** |
| 6 | Пересечение интервалов → **409 Conflict** (вместо 406) |
| 7 | Неверный `Content-Type` → **415 Unsupported Media Type** |
| 8 | `Content-Type` ответов об ошибках → **`application/json`** (не problem+json) |
| 9 | Пустой список `GET /tasks` → **200 OK + `[]`** (согласовать со всеми ресурсами) |
| 10 | Хранилище: **`TaskStorage` + композиция** вместо наследования `FileBackedTaskManager` |
| 11 | Потокобезопасность: **`ConcurrentHashMap` + точечные `synchronized`** |

## Модель ветвления и тегов

- Префикс веток: **`develop-`** (не `feature-`).
- Этапы 0-1-2 (подготовка и баг-фиксы) — одной веткой `develop-stage-0-1-2-fixes`.
- Каждый следующий этап — своя ветка `develop-stage-X-...`.
- После этапа: merge в `main` через `--no-ff`, ветку удалить.
- Теги на merge-коммит в `main`:
    - `v0.1-before-refactor` (до этапа 0)
    - `v0.2-fixes` (после этапов 0-1-2)
    - `v0.3-http`, `v0.4-storage`, `v0.5-concurrency`, `v0.6-api`, `v0.7-tests`, `v1.0-portfolio`

## Соглашения по коду и тестам

- Отвечать по-русски.
- **Тесты:**
    - `@Nested`, `@DisplayName`, имена `methodName_Should_ExpectedBehavior_Test`.
    - `assertThrows` — только с конкретными типами исключений, не `RuntimeException`.
    - Без `System.out.println`.
    - Детерминированное время (`LocalDateTime.of`), не `now()`.
    - `@AfterEach` для очистки temp-файлов.
    - HTTP-тесты — на порту 0, проверять тело ответа.
- **Code fence:** для вложенных code fence использовать четыре бэктика снаружи.
- Файлы присылаются целиком; не выдумывать методы/поля, которых нет в файлах.

## Окружение и известные грабли

- Windows, IDEA.
- Тесты могут содержать print-ы (отладочные) — убрать в этапе 7.
- `catch_Exception_When_Save_File_Read_Only_Test` нестабилен на Windows — судьба
  решается в этапе 7.
- `ManagersUtil.getFileBackedTaskManager()` использует относительный путь
  `"resources/tasks.csv"` — исправляется в этапе 2.
- В проекте есть `lib/` с ручными jar-ами: gson 2.13.2, junit-jupiter 5.8.1.
  Удаляется в этапе 0 (заменяется на Maven-зависимости).

## Как со мной работать

- Присылаю файлы целиком — читать внимательно, построчно, не пропускать семантические
  ошибки (типы, сигнатуры, nullable-поля).
- Не выдумывать методы и поля, которых нет в файлах.
- Если что-то не видно — просить прислать.
- Не редактировать вслепую — просить актуальную версию.
- Отвечать по-русски.
- Для ответов с вложенными code fence использовать четыре бэктика снаружи.
- Коммиты — лаконичные, но информативные.
- **Обновление context.md в конце каждого этапа:** новая версия подробнее предыдущей,
  заменяет старую (git-история сохранит все ревизии файла).