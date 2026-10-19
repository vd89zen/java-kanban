# java-kanban — контекст для продолжения работы

## Проект

Backend-сервис для управления задачами по методологии Kanban. Портфолио-проект на базе
учебного Яндекс Практикума. **Проект завершён**, версия `1.0.0-SNAPSHOT`, готовится
тег `v1.0-portfolio`.

Репозиторий: https://github.com/vd89zen/java-kanban
Активная ветка: `develop-stage-8-infra` (все этапы 0-8 закрыты, ждёт merge и финальный тег).

**Стек:** Java 17+, `com.sun.net.httpserver.HttpServer`, Gson 2.11.0, Maven, JUnit 5,
SLF4J + Logback, Checkstyle, SpotBugs, JaCoCo, GitHub Actions.

**Окружение:** Windows, IntelliJ IDEA, JDK 21 (компиляция под 17 через `maven.compiler.release`).

**Как собирать:**
- `./mvnw clean test` — тесты
- `./mvnw clean package` — fat-jar в `target/java-kanban.jar`
- `./mvnw clean verify` — тесты + Checkstyle + SpotBugs + JaCoCo
- `./mvnw exec:java` — запуск без сборки jar

**Как запускать:**
- `java -jar target/java-kanban.jar` (fat-jar)
- `./mvnw exec:java` (main-класс)

**Переменные окружения:**
- `SERVER_PORT` / `-Dserver.port` — порт, дефолт 8080.
- `DATA_FILE` / `-Ddata.file` — путь к CSV, дефолт `./tasks.csv`.

## Maven-конфигурация

- `groupId` — `ru.yandex.practicum`, `artifactId` — `java-kanban`
- `version` — `1.0.0-SNAPSHOT`
- `finalName` — `java-kanban`
- `main.class` — `ru.yandex.practicum.java.tasktracker.TaskTrackerApplication`
- `maven.compiler.release` — 17
- `<argLine></argLine>` в properties — для `@{argLine}` (JaCoCo + UTF-8)

**Зависимости:** gson 2.11.0, slf4j-api 2.0.13, logback-classic 1.5.6,
junit-jupiter 5.10.2 (test).

**Resource filtering:** `src/main/resources/` с `<filtering>true</filtering>`,
`maven-resources-plugin` с `<propertiesEncoding>UTF-8</propertiesEncoding>`.
`app.properties` с `app.version=${project.version}`.

**Плагины (все в `<plugins>`):**
- `maven-resources-plugin` 3.3.1 — filtering, propertiesEncoding
- `maven-compiler-plugin` 3.13.0
- `maven-checkstyle-plugin` 3.3.1 — `checkstyle.xml`, `failOnViolation=true`,
  `violationSeverity=error`, `includeTestSourceDirectory=true`, фаза `validate`
- `maven-surefire-plugin` 3.2.5 — `argLine=@{argLine} -Dfile.encoding=UTF-8 ...`
- `jacoco-maven-plugin` 0.8.12 — `prepare-agent` + `report` на `verify`
- `maven-shade-plugin` 3.5.3 — Services + Manifest transformers
- `spotbugs-maven-plugin` 4.8.6.4 — `effort=Max`, `threshold=Low`,
  `failOnError=true`, `spotbugs-exclude.xml`, фаза `verify`
- `exec-maven-plugin` 3.2.0

**Maven Wrapper:** `mvnw`, `mvnw.cmd`, `.mvn/wrapper/maven-wrapper.properties`.

**Файлы качества:** `checkstyle.xml`, `suppressions.xml`, `spotbugs-exclude.xml`,
`.github/workflows/ci.yml`.

## Архитектура

```
TaskTrackerApplication
  └── HttpTaskServer (com.sun.net.httpserver)  ← ExecutorService (fixed, 8)
        ├── /tasks /subtasks /epics /history /prioritized /health /version
                │
                ▼
        TaskManager (класс, потокобезопасный)
        ├── TaskViewHistory (synchronized, LinkedHashMapCustom)
        └── TaskStorage (интерфейс)
              ├── NoopStorage  (no-op)
              └── CsvStorage   (CSV-персистенция)
```

**Ключевые решения:**
- Интерфейсы `TaskManager` и `HistoryManager` удалены — реализация одна,
  полиморфизм на `TaskStorage`.
- `FileBackedTaskManager` удалён — композиция `TaskManager(new CsvStorage(file))`.
- Менеджер: concurrent-коллекции + единый лок на мутации.
- Генерация id — монотонный `nextId`, без переиспользования.
- Пагинация, фильтры, сортировка — на уровне хендлеров.
- Версия — единый источник `pom.xml`, в `app.properties` через resource filtering.
- Checkstyle и SpotBugs валят сборку. JaCoCo — отчёт, CI — verify на каждый push.

Пакеты:

| Пакет | Назначение |
|---|---|
| `config` | `AppConfig` (env/sysprop + версия из properties) |
| `http` | `HttpTaskServer` |
| `http.handler` | Хендлеры, `BaseHttpHandler`, `QueryParams` |
| `manage` | `ManagersUtil` |
| `service` | `TaskManager`, `TaskViewHistory` |
| `storage` | `TaskStorage`, `LoadedState`, `NoopStorage`, `CsvStorage` |
| `task` | `Task`, `Epic`, `Subtask` |
| `utils` | `AbstractTask`, `LinkedHashMapCustom`, `TextForGson`, `CommaSeparatedValuesCreateProcess` |
| `utils.enums` | `StatusProgress`, `TypesTasks`, `ResultOfOperation`, `HttpResponseCode` |
| `utils.gson` | `LocalDateTimeTypeAdapter`, `DurationTypeAdapter`, `AbstractTaskDeserializer` |
| `exceptions` | `BadRequestException`, `NotFoundException`, `ManagerSaveException` |

## Структура файлов

```
.
├── .gitattributes / .gitignore / README.md / context.md
├── .github/workflows/ci.yml
├── .mvn/wrapper/maven-wrapper.properties
├── mvnw / mvnw.cmd / pom.xml
├── checkstyle.xml / suppressions.xml / spotbugs-exclude.xml
├── docs/
│   ├── architecture.md / api.md / storage.md / errors.md
│   ├── configuration.md / testing.md
├── src/main/java/ru/yandex/practicum/java/tasktracker/
│   ├── TaskTrackerApplication.java
│   ├── config/AppConfig.java
│   ├── exceptions/...
│   ├── http/HttpTaskServer.java
│   ├── http/handler/  (Base + 7 хендлеров + QueryParams)
│   ├── manage/ManagersUtil.java
│   ├── service/  (TaskManager, TaskViewHistory)
│   ├── storage/  (TaskStorage, LoadedState, NoopStorage, CsvStorage)
│   ├── task/...
│   └── utils/...
├── src/main/resources/
│   ├── app.properties
│   └── logback.xml
└── src/test/java/ru/yandex/practicum/java/tasktracker/
    ├── config/AppConfigTest.java
    ├── http/HttpTaskServerTest.java
    ├── manage/ManagersUtilTest.java
    ├── service/  (TaskManagerTest, TaskViewHistoryTest, TaskManagerConcurrencyTest)
    ├── storage/CsvStorageTest.java
    ├── task/  (TaskTest, SubtaskTest, EpicTest)
    └── utils/  (LinkedHashMapCustomTest, CommaSeparatedValuesCreateProcessTest, gson/GsonAdaptersTest)
```

## Ключевые сигнатуры

### `service.TaskManager`

```
TaskManager()                      // NoopStorage
TaskManager(TaskStorage storage)
```

Поля: `int nextId`, `Set<Integer> allIdInWork` (concurrent),
`Map<Integer, Task> tasks`, `epics`, `subtasks` (concurrent),
`NavigableSet<AbstractTask> prioritizedTasks` (concurrent),
`TaskViewHistory historyManager`, `TaskStorage storage`, `Object lock`.

Приватные: `generateIdNumber()`, `addToPriorityList`, `initFromStorage()`,
`persist()`, `verifyIntegrity()`.

Публичные: `getPrioritizedTasks`, `isTimeIntersectBoth`, `isTimeIntersectWithOthers`,
`getHistory`, `getTotalOfIdInWork`, `addTask`, `getTaskByIdNumber`,
`removeTaskByIdNumber`, `updateTask`, `getAllTasks`, `removeAllTasks`,
`addEpic`, `getEpicByIdNumber`, `removeEpicByIdNumber`, `updateEpicName`,
`updateEpicDescription`, `getAllSubtasksFromEpic`, `getAllEpics`, `removeAllEpics`,
`addSubtask`, `getSubtaskByIdNumber`, `removeSubtaskByIdNumber`, `updateSubtask`,
`getAllSubtasks`, `removeAllSubtasks`.

**Потокобезопасность:** все мутации под `synchronized (lock)`, `persist()` вне
лока. Порядок: `lock` → монитор `TaskViewHistory`.

### `service.TaskViewHistory`

```
synchronized ResultOfOperation addRecord(AbstractTask)
synchronized ResultOfOperation removeRecord(int)
synchronized ArrayList<AbstractTask> getHistory()
```

### `storage.TaskStorage` / `LoadedState`

```
void save(Collection<Task>, Collection<Epic>, Collection<Subtask>)
LoadedState load()

record LoadedState(Collection<Task> tasks, Collection<Epic> epics, Collection<Subtask> subtasks)
```

### `storage.CsvStorage`

`CsvStorage(File file)`. `save` сортирует по id, `load` пропускает битые строки
с warn-логом. Формат CSV — 10 колонок, плейсхолдеры `SKIP`/`EMPTY`.

### `config.AppConfig`

```
static int getServerPort()      // env SERVER_PORT / -Dserver.port / 8080
static String getDataFilePath() // env DATA_FILE / -Ddata.file / "tasks.csv"
static String getVersion()      // из app.properties (Maven resource filtering)
```

### `http.HttpTaskServer`

```
HttpTaskServer()                              // port из AppConfig
HttpTaskServer(TaskManager)                   // port из AppConfig
HttpTaskServer(TaskManager, int port)         // явный порт (0 — свободный)
void start() / void stop()                    // + ExecutorService (8), shutdownNow
int getPort()                                 // фактический порт
```

### `http.handler.QueryParams` (record)

```
record QueryParams(int from, int size, StatusProgress status, String sortBy)
static QueryParams defaults()  // 0, 20, null, "id"
```

### `http.handler.BaseHttpHandler`

Хелперы: `sendText` (200), `sendCreated` (201), `sendNoContent` (204),
`sendNotFound` (404), `sendBadRequest` (400), `sendConflict` (409),
`sendMethodNotAllowed` (405 + `Allow`), `sendUnsupportedMediaType` (415),
`sendError` (500).

`protected QueryParams parseQueryParams(HttpExchange)` — валидация query-параметров,
`BadRequestException` → 400. Не тримим — строгий контракт.

`protected Comparator<AbstractTask> comparatorFor(String sortBy)` — `id` или
`startDateTime` (без времени → в конец).

### `utils.AbstractTask`

Default-конструктор: `idNumber = -1`, `durationInMinutes = Duration.ZERO`.
Gson десериализует через no-arg конструктор + reflection. Хендлеры нормализуют
`idNumber` в `0` в ветке создания.

### Enums

- `StatusProgress`: NEW, IN_PROGRESS, DONE.
- `TypesTasks`: TASK, EPIC, SUBTASK.
- `HttpResponseCode`: 200, 201, 204, 400, 404, 405, 409, 415, 500.
- `ResultOfOperation`: 13 значений.

## Что уже сделано (все этапы закрыты)

### Этапы 0-2 (PR #8, `v0.2-fixes`)
Maven + wrapper, `Main` → `TaskTrackerApplication`, logback. Баг-фиксы (NPE в
`AbstractTask()`, `Epic.SubtaskFields` equals/hashCode, bounds-check в CSV,
null-check в десериализаторе, `toString`, `Subtask` dedup, SLF4J). Code smells
(`AppConfig`, убраны mutable-поля, сортировка CSV, удалён `DataForUserScenario`,
`getAllSubtasksFromEpic` через данные эпика). POST без `id` создаёт ресурс, NPE в
`save` для файла без родителя.

### Этап 3 (PR #9, `v0.3-http`)
HTTP-семантика: 201+Location, 200 на update, 204 на delete, 409 на пересечение,
415 на Content-Type, 405 на метод, 200+[] на пустом списке.

### Этап 4 (PR #10, `v0.4-storage`)
`TaskStorage` + `LoadedState`, `NoopStorage` + `CsvStorage`. `InMemoryTaskManager` →
`TaskManager`, `InMemoryHistoryManager` → `TaskViewHistory`. `FileBackedTaskManager`
удалён. Интерфейсы `TaskManager`/`HistoryManager` удалены. `initFromStorage()` +
`verifyIntegrity()`.

### Этап 5 (PR #11, `v0.5-concurrency`)
Монотонный `nextId` без переиспользования. Concurrent-коллекции. Единый `Object lock`,
`persist()` вне лока. `TaskViewHistory` — `synchronized`. Javadoc на `TaskManager`.
`HttpTaskServer` — `ExecutorService` (8), `shutdownNow` в `stop()`.
`TaskManagerConcurrencyTest` (16×50 = 800 параллельных `addTask`).

### Этап 6 (PR #12, `v0.6-api`)
Пагинация `?from&size&status&sortBy` для `GET /tasks|/subtasks|/epics`.
`QueryParams` + `parseQueryParams` + `comparatorFor`. Невалидные параметры → 400.
`GET /health` → `{"status":"UP"}`, `GET /version` → `{"version": ...}`.

### Этап 7 (PR #13, `v0.7-tests`)
Переработка тестов: конкретные исключения, детерминированное время, без print'ов,
`@AfterEach` для temp-файлов, порт 0 для HTTP-тестов, один сервер на класс (54 s → 1.7 s),
`CsvStorageTest`, `CommaSeparatedValuesCreateProcessTest`, `GsonAdaptersTest`,
параметризованные тесты (`@ParameterizedTest` для статусов эпика и пересечений
интервалов). Удалены 6 дублирующих `epic_Check_Status_Progress_*`.

### Этап 8 (ветка `develop-stage-8-infra`, ждёт merge, тег `v1.0-portfolio`)
- **8.1 Checkstyle:** `checkstyle.xml` (из filmorate, адаптирован), `suppressions.xml`,
  `failOnViolation=true`, `violationSeverity=error`. 29 нарушений → 0 (переименование
  `@Nested`-классов, `static final` для констант, `prepareForTest`).
- **8.2 SpotBugs:** `spotbugs-exclude.xml`, `failOnError=true`. Найдено 39 → исправлены
  реальные (8×`DM_BOXED_PRIMITIVE_FOR_PARSING`, 2×`RC_REF_COMPARISON` в
  `LinkedHashMapCustom.NodeCustom.equals`, 2×`SF_SWITCH_NO_DEFAULT`, 1×`SIC_INNER_SHOULD_BE_STATIC`
  для `Epic.SubtaskFields`) → подавлены ложные (17×`CT_CONSTRUCTOR_THROW`, 6×`EI_EXPOSE_REP*`,
  1×`MC_OVERRIDABLE_METHOD_CALL_IN_CONSTRUCTOR`, 3×`URF_UNREAD_FIELD`) → 0.
- **8.3 JaCoCo:** отчёт в `target/site/jacoco/`. Покрытие 78% → 80% после добавления
  `AppConfigTest` и расширения `ManagersUtilTest`.
- **8.4 GitHub Actions:** `.github/workflows/ci.yml` — `mvnw clean verify` на push
  в `main`/`develop-*` и PR в `main`. JaCoCo-отчёт в артефактах. Зелёный.
- **8.5 Dockerfile / 8.7 docker-compose:** не делаем (решение этапа 8).
- **8.6 Синхронизация версии:** `app.properties` с `app.version=${project.version}`,
  resource filtering, `AppConfig.getVersion()` читает properties. Версия `pom.xml`
  поднята до `1.0.0-SNAPSHOT`. Тест `endpointVersion_Get_Test` использует
  `AppConfig.getVersion()`, не хардкод.
- **8.8 README + docs/:** README сокращён, документация в `docs/` (architecture,
  api, storage, errors, configuration, testing). Бейджи: CI, Coverage (80%),
  Java 17.

**Тестов:** 200+ (точное — `./mvnw test` покажет `Tests run: N`).
**Покрытие:** ~80%.
**Все проверки зелёные:** `./mvnw clean verify` + CI.

## Что дальше

Проект завершён. Осталось:
1. Merge `develop-stage-8-infra` в `main` через PR.
2. Тег `v1.0-portfolio` на merge-коммит.
3. (Опционально) Убрать `-SNAPSHOT` из версии → `1.0.0`, пересобрать, обновить
   `app.properties` через rebuild.

## Принятые решения

| № | Решение |
|---|---|
| 1 | Система сборки: Maven, минимальный `pom.xml` |
| 2 | Логирование: SLF4J + Logback |
| 3 | Путь к данным: `./tasks.csv` через env/sysprop |
| 4 | `DataForUserScenario` — удалён |
| 5 | `DELETE` → 204 No Content |
| 6 | Пересечение интервалов → 409 Conflict |
| 7 | Неверный `Content-Type` → 415 |
| 8 | `Content-Type` ошибок → `application/json` |
| 9 | Пустой список → 200 + `[]` |
| 10 | Хранилище: `TaskStorage` + композиция |
| 11 | Потокобезопасность: `ConcurrentHashMap` + единый `synchronized (lock)` |
| 12 | Менеджер без интерфейса; полиморфизм на `TaskStorage` |
| 13 | Генерация id: монотонный счётчик, без переиспользования |
| 14 | Пагинация на уровне хендлеров, не в сервисе |
| 15 | `QueryParams` — record (4 параметра) |
| 16 | Контракт API строгий: не тримим параметры |
| 17 | OpenAPI-спека и версионирование `/api/v1/` — не делаем |
| 18 | Read-only тест для `CsvStorage` — не пишем (нестабилен на Windows) |
| 19 | Один `HttpServer` на тестовый класс, порт 0 |
| 20 | Параметризованные тесты через `@MethodSource` |
| 21 | Checkstyle: конфиг из filmorate (MethodName с подчёркиваниями, звёздочные импорты разрешены) |
| 22 | SpotBugs: `failOnError=true`, exclude для ложных срабатываний |
| 23 | JaCoCo: отчёт на `verify`, без codecov (статический бейдж 80%) |
| 24 | GitHub Actions: `mvnw clean verify` + артефакт JaCoCo |
| 25 | Версия: единый источник `pom.xml` через resource filtering |
| 26 | Dockerfile / docker-compose — не делаем |

## Модель ветвления и тегов

- Префикс веток: `develop-`.
- Каждый этап — своя ветка `develop-stage-X-...`.
- После этапа: PR на GitHub (base `main`), merge commit, тег на merge-коммит.

**Теги:** `v0.1-before-refactor`, `v0.2-fixes`, `v0.3-http`, `v0.4-storage`,
`v0.5-concurrency`, `v0.6-api`, `v0.7-tests` — на main.
`v1.0-portfolio` — после merge текущей ветки (финальный).

## Соглашения по коду и тестам

- Отвечать по-русски.
- **Тесты:** `@Nested`, `@DisplayName`, имена `methodName_Should_ExpectedBehavior_Test`.
  `assertThrows` — конкретные типы. Без `System.out.println`. Детерминированное время
  (`LocalDateTime.of`). HTTP-тесты на порту 0, один сервер на класс. Параметризованные
  — `@MethodSource` для сложных данных.
- **Checkstyle валит сборку** — методы разворачивать, `{` с новой строки, отделять
  `METHOD_DEF` пустой строкой, `@Nested`-классы с заглавной буквы, константы
  `static final`.
- **SpotBugs валит сборку.** Новые реальные баги не подавляем — правим. Если
  ложное срабатывание — точечно в `spotbugs-exclude.xml` с комментарием.
- **Code fence:** для вложенных — четыре бэктика снаружи.
- Файлы присылаются целиком; не выдумывать методы/поля.
- **Gson** работает через no-arg конструктор + reflection. При создании `Gson`
  вручную обязательно регистрировать все три адаптера (`LocalDateTime`, `Duration`,
  `AbstractTask`) — иначе `Duration` падает на reflection в Java 17+.

## Окружение и известные грабли

- Windows, IDEA, JDK 21 (собираем под 17).
- Maven не в PATH системного bash. Используем `./mvnw ...`.
- Пути в bash — прямые слэши: `git add src/main`, не `src\main`.
- В Surefire: `argLine=@{argLine} -Dfile.encoding=UTF-8 ...` — `@{argLine}`
  подставляет JaCoCo agent, остальное — UTF-8 для кириллицы на Windows.
- `mvn clean` может падать с `Failed to delete target` — файл-лок на Windows.
  Лечится закрытием IDEA и `rm -rf target`.
- `.gitattributes` нормализует `.java` в LF. Warning про CRLF — нормально.
- `idNumber = -1` — маркер «пустышка». Gson даёт `-1` при POST без `id`;
  хендлеры нормализуют в `0` в ветке создания.
- **Пересечение интервалов включает касание границ.** `t1.end == t2.start`
  считается пересечением. В тестах с массовым добавлением задач нужен
  достаточный шаг между `startDateTime`.
- **IDEA: тесты не запускаются через кнопку Run, а `./mvnw test` работает.**
  Причина: IDEA использует свой native build system, не Maven. Решение:
  Maven-панель → правый клик по корню → **Unlink Maven Projects**, затем
  правый клик по `pom.xml` → **Add as Maven Project**.
- **IDEA: Compact Middle Packages** — `File → Settings → Advanced Settings →
  Project View → Compact Middle Packages`.
- **Gson + Duration в Java 17+:** при создании `Gson` без `DurationTypeAdapter`
  десериализация `Task`/`Subtask` через reflection упадёт с
  `InaccessibleObjectException`. Всегда регистрировать все три адаптера.
- **Resource filtering в IDEA:** при прямом запуске из IDEA (не через Maven)
  `app.properties` может содержать `${project.version}` как есть. Запускать
  через `./mvnw exec:java` или `java -jar target/java-kanban.jar`, либо включить
  `Delegate IDE build/run actions to Maven` в настройках Maven.
- **SpotBugs:** `threshold=Low` + `effort=Max` — максимум предупреждений. Ложные
  подавлены в `spotbugs-exclude.xml` с комментариями. Новые реальные баги — правим,
  не подавляем.

## Ожидаемое действие от ассистента в новом чате

1. **Прочитать `context.md` целиком.**
2. **Подтвердить понимание в одном-двух предложениях:** статус проекта, следующий шаг.
3. **Задать уточняющий вопрос:** «На каком этапе мы сейчас? Что уже закоммичено,
   что ещё нет?»
4. **Дождаться ответа.**
5. **Проверить актуальность.** При сомнениях — попросить текущие версии файлов.
6. **Не выдумывать методы и поля.** Если нужен метод, а его нет в «Ключевых
   сигнатурах» — попросить файл.

## Как со мной работать

- Присылаю файлы целиком — читать внимательно, построчно.
- Не выдумывать методы и поля, которых нет в файлах.
- Если что-то не видно — просить прислать.
- Не редактировать вслепую — просить актуальную версию.
- **Не выкатывать сразу большой план на много шагов вперёд, если следующий шаг
  зависит от результата предыдущего.** Останавливаться и ждать ответа после
  каждого шага.
- Отвечать по-русски.
- Для ответов с вложенными code fence использовать четыре бэктика снаружи.
- Коммиты — лаконичные, но информативные.
- **Обновление `context.md` в конце каждого этапа.**
- Общий **PLAN** — локальный, в репо не публикуется.