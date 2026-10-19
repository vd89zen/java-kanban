# Тестирование и качество кода

## Запуск

```bash
./mvnw clean test       # только тесты
./mvnw clean verify     # тесты + Checkstyle + SpotBugs + JaCoCo
```

## Покрытие

| Пакет | Покрытие | Комментарий |
|---|---|---|
| `utils.gson` | 100% | Все адаптеры |
| `utils.enums` | 100% | Тривиальные enum'ы |
| `manage` | 100% | `ManagersUtil` |
| `storage` | 91% | `CsvStorage`, `NoopStorage` |
| `http` | 87% | `HttpTaskServer` |
| `http.handler` | 81% | Хендлеры + `BaseHttpHandler` + `QueryParams` |
| `utils` | 80% | `AbstractTask`, `LinkedHashMapCustom`, CSV-парсер |
| `service` | 80% | `TaskManager`, `TaskViewHistory` |
| `task` | 69% | Много конструкторов `Task`/`Subtask`/`Epic` |
| `config` | 72% | `AppConfig` |

**Total: ~80% инструкций, ~65% ветвей.**

`TaskTrackerApplication` (main) — 0%, не тестируется.

## Принципы

- **Конкретные исключения.** `assertThrows(NotFoundException.class, ...)`, а не
  `RuntimeException`.
- **Детерминированное время.** `LocalDateTime.of(2026, 1, 1, 10, 0)` вместо `now()`.
- **Без `System.out.println`.**
- **`@AfterEach` для temp-файлов.**
- **HTTP-тесты на порту 0** (ОС выбирает свободный), один сервер на класс
  (`@BeforeAll`/`@AfterAll`), `@BeforeEach` чистит данные.
- **Проверка тела ответа**, где осмысленно, а не только кода.
- **Параметризованные тесты** через `@MethodSource` для сложных данных.

## Структура тестов

| Файл | Что покрывает |
|---|---|
| `service/TaskManagerTest` | CRUD задач/эпиков/подзадач, приоритеты, пересечения, пересчёт полей эпика, `hashCode` |
| `service/TaskViewHistoryTest` | История, порядок после удаления, копии |
| `service/TaskManagerConcurrencyTest` | Параллельные `addTask` (16×50 = 800) |
| `http/HttpTaskServerTest` | Все эндпоинты, коды, тела, пагинация, `/health`, `/version` |
| `storage/CsvStorageTest` | `save`/`load`, круговой прогон, битые строки |
| `utils/CommaSeparatedValuesCreateProcessTest` | `toString`, парсинг, битые строки |
| `utils/LinkedHashMapCustomTest` | Порядок, удаление, копии, независимость |
| `utils/gson/GsonAdaptersTest` | Все три адаптера + ошибки |
| `task/TaskTest`, `SubtaskTest`, `EpicTest` | `equals`/`hashCode`, параметризованный статус эпика |
| `manage/ManagersUtilTest` | Фабрики + персистенция через `CsvStorage` |
| `config/AppConfigTest` | env/sysprop/дефолты, версия |

## Checkstyle

`checkstyle.xml` (взят из filmorate, адаптирован). `failOnViolation=true`,
`violationSeverity=error`. Сборка валится при нарушении.

Основные правила: `LineLength` 120, `UnusedImports`, `LeftCurly`/`RightCurly`,
`WhitespaceAround`, `EmptyLineSeparator`, `MethodName` (с подчёркиваниями —
для тестов), `TypeName`, `MemberName` (статические константы не проверяются).

`suppressions.xml` — общий файл подавлений (сейчас пустой, только `target/generated-sources/`).

## SpotBugs

`spotbugs-exclude.xml` подавляет ложные срабатывания:
- `CT_CONSTRUCTOR_THROW` — «finalizer attack», неактуально в Java 17+.
- `EI_EXPOSE_REP` / `EI_EXPOSE_REP2` для `LoadedState` (record, data-carrier)
  и `BaseHttpHandler` (TaskManager шарится по дизайну).
- `MC_OVERRIDABLE_METHOD_CALL_IN_CONSTRUCTOR` — false positive (`TaskManager`
  не наследует `AbstractTask`).
- `URF_UNREAD_FIELD` для полей-индексов `CommaSeparatedValuesCreateProcess`
  (документация 10 колонок CSV).

`failOnError=true`. Новый реальный баг валит сборку.

## CI

GitHub Actions (`.github/workflows/ci.yml`):
- `push` в `main`, `develop-*`.
- `pull_request` в `main`.
- Шаги: checkout → JDK 17 → `./mvnw clean verify` → загрузка JaCoCo-отчёта
  в артефакты (хранится 7 дней).