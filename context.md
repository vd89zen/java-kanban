# java-kanban — контекст для продолжения работы

## Проект

Backend-сервис для управления задачами по методологии Kanban. Портфолио-проект на базе
учебного Яндекс Практикума, в процессе доработки.

Репозиторий: https://github.com/vd89zen/java-kanban
Активная ветка: `develop-stage-6-api` (этапы 0-1-2-3-4-5-6 закрыты, этап 7 следующий).

**Стек:** Java 17+, `com.sun.net.httpserver.HttpServer`, Gson, Maven, JUnit 5,
SLF4J + Logback.

**Окружение:** Windows, IntelliJ IDEA, JDK 21 (компиляция под 17 через `maven.compiler.release`).

**Как собирать:**
- В bash: `./mvnw clean test`, `./mvnw clean package`
- В cmd: `mvnw.cmd clean test`
- В IDEA: панель Maven, настроена на **Use Maven wrapper**

**Как запускать:**
- `java -jar target/java-kanban.jar` (fat-jar через shade)
- `./mvnw exec:java` (запуск main-класса без сборки jar)

**Переменные окружения:**
- `SERVER_PORT` / `-Dserver.port` — порт сервера, дефолт 8080.
- `DATA_FILE` / `-Ddata.file` — путь к CSV, дефолт `tasks.csv` в CWD.

## Maven-конфигурация

- `groupId` — `ru.yandex.practicum`, `artifactId` — `java-kanban`
- `version` — `0.2.0-SNAPSHOT`
- `finalName` — `java-kanban` (`target/java-kanban.jar`)
- `main.class` — `ru.yandex.practicum.java.tasktracker.TaskTrackerApplication`
- `maven.compiler.release` — 17 (НЕ `source`/`target`)

**Зависимости:** gson 2.11.0, slf4j-api 2.0.13, logback-classic 1.5.6,
junit-jupiter 5.10.2 (test).

**Плагины:** maven-compiler 3.13.0, maven-surefire 3.2.5 (argLine с UTF-8),
maven-shade 3.5.3 (Services + Manifest transformers), exec-maven-plugin 3.2.0.

**Maven Wrapper:** `mvnw`, `mvnw.cmd`, `.mvn/wrapper/maven-wrapper.properties`.

## Архитектура

```
TaskTrackerApplication
  └── HttpTaskServer (com.sun.net.httpserver)  ← ExecutorService (fixed pool, 8)
        ├── /tasks        → TasksHandler
        ├── /subtasks     → SubtasksHandler
        ├── /epics        → EpicsHandler
        ├── /history      → HistoryHandler
        ├── /prioritized  → PrioritizedHandler
        ├── /health       → HealthHandler
        └── /version      → VersionHandler
                │
                ▼
        TaskManager (класс, потокобезопасный)
        ├── TaskViewHistory (потокобезопасный через synchronized)
        └── TaskStorage (интерфейс)
              ├── NoopStorage  (no-op)
              └── CsvStorage   (CSV-персистенция)
```

**Ключевые решения:**
- Интерфейсы `TaskManager` и `HistoryManager` удалены — реализация одна,
  полиморфизм переехал на `TaskStorage`.
- `FileBackedTaskManager` удалён — заменён композицией `TaskManager(new CsvStorage(file))`.
- Менеджер держит состояние в concurrent-коллекциях, все мутации — под единым локом.
- Генерация id — монотонный счётчик `nextId`, без переиспользования удалённых id.
- Пагинация, фильтры, сортировка — на уровне хендлеров, не в сервисе.

Пакеты:

| Пакет | Назначение |
|---|---|
| `config` | `AppConfig` — чтение env/sysprop + версия |
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
├── .mvn/wrapper/maven-wrapper.properties
├── mvnw / mvnw.cmd / pom.xml
├── src/
│   ├── main/
│   │   ├── java/ru/yandex/practicum/java/tasktracker/
│   │   │   ├── TaskTrackerApplication.java
│   │   │   ├── config/AppConfig.java
│   │   │   ├── exceptions/...
│   │   │   ├── http/HttpTaskServer.java
│   │   │   ├── http/handler/
│   │   │   │   ├── BaseHttpHandler.java
│   │   │   │   ├── QueryParams.java
│   │   │   │   ├── TasksHandler.java
│   │   │   │   ├── SubtasksHandler.java
│   │   │   │   ├── EpicsHandler.java
│   │   │   │   ├── HistoryHandler.java
│   │   │   │   ├── PrioritizedHandler.java
│   │   │   │   ├── HealthHandler.java
│   │   │   │   └── VersionHandler.java
│   │   │   ├── manage/ManagersUtil.java
│   │   │   ├── service/
│   │   │   │   ├── TaskManager.java
│   │   │   │   └── TaskViewHistory.java
│   │   │   ├── storage/
│   │   │   │   ├── TaskStorage.java
│   │   │   │   ├── LoadedState.java
│   │   │   │   ├── NoopStorage.java
│   │   │   │   └── CsvStorage.java
│   │   │   ├── task/...
│   │   │   └── utils/...
│   │   └── resources/logback.xml
│   └── test/java/ru/yandex/practicum/java/tasktracker/
│       ├── http/HttpTaskServerTest.java
│       ├── manage/ManagersUtilTest.java
│       ├── manage/LinkedHashMapCustomTest.java
│       ├── service/
│       │   ├── TaskManagerTest.java
│       │   ├── TaskViewHistoryTest.java
│       │   └── TaskManagerConcurrencyTest.java
│       └── task/...  (EpicTest, SubtaskTest, TaskTest)
```

## Ключевые сигнатуры

### `service.TaskManager`

Конструкторы:
```
TaskManager()                      // внутри new NoopStorage()
TaskManager(TaskStorage storage)   // storage != null
```

Приватные поля: `int nextId`, `final Set<Integer> allIdInWork` (concurrent),
`final Map<Integer, Task> tasks`, `epics`, `subtasks` (concurrent),
`final NavigableSet<AbstractTask> prioritizedTasks` (concurrent),
`final TaskViewHistory historyManager`, `final TaskStorage storage`,
`final Object lock`.

Приватные методы: `generateIdNumber()`, `addToPriorityList(AbstractTask)`,
`initFromStorage()`, `persist()`, `verifyIntegrity()`.

Публичные методы: `getPrioritizedTasks`, `isTimeIntersectBoth`,
`isTimeIntersectWithOthers`, `getHistory`, `getTotalOfIdInWork`,
`addTask`, `getTaskByIdNumber`, `removeTaskByIdNumber`, `updateTask`,
`getAllTasks`, `removeAllTasks`, `addEpic`, `getEpicByIdNumber`,
`removeEpicByIdNumber`, `updateEpicName`, `updateEpicDescription`,
`getAllSubtasksFromEpic`, `getAllEpics`, `removeAllEpics`, `addSubtask`,
`getSubtaskByIdNumber`, `removeSubtaskByIdNumber`, `updateSubtask`,
`getAllSubtasks`, `removeAllSubtasks`.

**Потокобезопасность:** все мутации — под `synchronized (lock)`, `persist()`
вне лока. Порядок захвата: `lock` → монитор `TaskViewHistory`. Deadlock исключён.
Javadoc на классе описывает атомарность и персистенцию.

### `service.TaskViewHistory`

```
TaskViewHistory()
synchronized ResultOfOperation addRecord(AbstractTask)
synchronized ResultOfOperation removeRecord(int)
synchronized ArrayList<AbstractTask> getHistory()
```

### `storage.TaskStorage` (интерфейс)

```
void save(Collection<Task> tasks, Collection<Epic> epics, Collection<Subtask> subtasks)
LoadedState load()
```

### `storage.LoadedState` (record)

```
record LoadedState(Collection<Task> tasks, Collection<Epic> epics, Collection<Subtask> subtasks)
```

### `storage.NoopStorage` / `storage.CsvStorage`

`CsvStorage.save` сортирует по id, `load` пропускает битые строки с warn-логом.
Заголовок CSV: `id,type,name,description,statusProgress,parentEpic,childSubtasks,durationInMinutes,startDateTime,endDateTime`.
Плейсхолдеры: `SKIP`, `EMPTY`.

### `manage.ManagersUtil`

```
static TaskManager getDefault()
static TaskViewHistory getDefaultHistory()
static TaskManager getFileBacked()
static TaskManager getFileBacked(File)
```

### `config.AppConfig`

```
static int getServerPort()      // env SERVER_PORT / -Dserver.port / 8080
static String getDataFilePath() // env DATA_FILE / -Ddata.file / "tasks.csv"
static String getVersion()      // "0.2.0"
```

Версия — хардкод, в этапе 8 (инфраструктура) при подключении Maven resource
filtering будет читаться из `pom.xml`.

### `LinkedHashMapCustom`

```
LinkedHashMapCustom()
LinkedHashMapCustom createCopy()
ResultOfOperation addLast(AbstractTask)
ResultOfOperation removeEntry(int)
ArrayList<AbstractTask> getListAllItem()
Integer getSize()
boolean equals(Object)
int hashCode()
```

### `AbstractTask`

Default-конструктор: `idNumber = -1`, `durationInMinutes = Duration.ZERO`.
**Gson десериализует через no-arg конструктор + reflection.** При POST без `id`
получаем `idNumber = -1` — хендлеры нормализуют в `0` в ветке создания.

Публичные методы: `getName`, `setName`, `getDescription`, `setDescription`,
`setIdNumber`, `getIdNumber`, `getStatusProgress`, `getTypeTask`,
`getDurationInMinutes`, `getStartDateTime`, `getEndDateTime`.

### `Task` / `Subtask` / `Epic`

Как раньше. `Subtask` — валидация родителя в приватном `validateParentEpicId(int)`.
`Epic` — вложенный `SubtaskFields` с `equals`/`hashCode`.

### `HttpTaskServer`

```
HttpTaskServer() throws IOException
HttpTaskServer(TaskManager) throws IOException
void start()   // log.info("HTTP server started on port {}", port)
void stop()    // httpServer.stop(1); executor.shutdownNow() + awaitTermination(1s)
```

`THREAD_POOL_SIZE = 8`, `SHUTDOWN_TIMEOUT_SECONDS = 1`.
Контексты: `/tasks`, `/subtasks`, `/epics`, `/history`, `/prioritized`,
`/health`, `/version`.

### `QueryParams` (record)

```
record QueryParams(int from, int size, StatusProgress status, String sortBy)
static QueryParams defaults()  // from=0, size=20, status=null, sortBy="id"
```

### `BaseHttpHandler`

Константа `CONTENT_TYPE_JSON`. Хелперы: `sendText` (200), `sendCreated` (201),
`sendNoContent` (204), `sendNotFound` (404), `sendBadRequest` (400),
`sendConflict` (409), `sendMethodNotAllowed` (405 + `Allow`),
`sendUnsupportedMediaType` (415), `sendError` (500).

**Пагинация:**
```
protected QueryParams parseQueryParams(HttpExchange httpExchange)
protected Comparator<AbstractTask> comparatorFor(String sortBy)
```

`parseQueryParams` бросает `BadRequestException` (→ 400) при невалидных
параметрах. Неизвестные параметры игнорируются. Не тримим — строгий контракт.
`comparatorFor`: `"id"` → по `getIdNumber()`, `"startDateTime"` → по
`getStartDateTime().orElse(LocalDateTime.MAX)` (задачи без времени в конце).

### Хендлеры

**Общее:** POST создание → 201 + Location, обновление → 200 (кроме
`EpicsHandler` — 400), DELETE → 204, неверный `Content-Type` → 415,
неподдерживаемый метод → 405 + `Allow`, пересечение интервалов → 409,
пустой список → 200 + [].

**`GET /tasks`, `GET /subtasks`, `GET /epics`:** пагинация через
`parseQueryParams`, фильтр по `status`, сортировка через `comparatorFor`,
`.skip(from).limit(size)`.

**`GET /health`:** `200 {"status":"UP"}`. Только GET.
**`GET /version`:** `200 {"version":"0.2.0"}`. Только GET.

### `CommaSeparatedValuesCreateProcess`

Как в этапе 4. `setFieldsFromStrings` проверяет `fields.length >= 10` и ловит
`NumberFormatException`.

### Enums

- `StatusProgress`: NEW, IN_PROGRESS, DONE.
- `TypesTasks`: TASK, EPIC, SUBTASK.
- `HttpResponseCode`: 200, 201, 204, 400, 404, 405, 409, 415, 500.
- `ResultOfOperation`: 13 значений.

## Что уже сделано

### Этапы 0-1-2 закрыты (PR #8, тег `v0.2-fixes`)

Maven + wrapper, Maven-layout, `Main` → `TaskTrackerApplication`, logback.
Баг-фиксы (NPE в `AbstractTask()`, `Epic.SubtaskFields` equals/hashCode,
bounds-check в CSV, null-check в десериализаторе, `toString` в 4 классах,
дедупликация `Subtask`, SLF4J). Code smells (`AppConfig`, убраны mutable-поля,
сортировка CSV, удалён `DataForUserScenario`, `getAllSubtasksFromEpic` через
данные эпика). Плюс: POST без `id` создаёт ресурс, NPE в `save` для файла без
родителя.

### Этап 3 закрыт (PR #9, тег `v0.3-http`)

HTTP-семантика: 201+Location на создание, 200 на update, 204 на delete,
409 на пересечение, 415 на неверный Content-Type, 405 на метод, 200+[] на
пустом списке.

### Этап 4 закрыт (PR #10, тег `v0.4-storage`)

`TaskStorage` + `LoadedState`, `NoopStorage` + `CsvStorage`.
`InMemoryTaskManager` → `TaskManager`, `InMemoryHistoryManager` → `TaskViewHistory`.
`FileBackedTaskManager` удалён. Интерфейсы `TaskManager` и `HistoryManager` удалены.
`initFromStorage()` + `verifyIntegrity()`.

### Этап 5 закрыт (PR #11, тег `v0.5-concurrency`)

Генерация id: монотонный `nextId`, без переиспользования. Concurrent-коллекции.
Единый лок `Object lock` для всех мутаций, `persist()` вне лока. `TaskViewHistory`
— методы `synchronized`. Javadoc на `TaskManager`. `HttpTaskServer` —
`ExecutorService` (8 потоков), `shutdownNow` в `stop()`. Тест
`TaskManagerConcurrencyTest` (16×50 = 800 параллельных `addTask`).

### Этап 6 закрыт (ветка `develop-stage-6-api`, ждёт merge)

- **Пагинация** для `GET /tasks | /subtasks | /epics`:
  `?from=0&size=20&status=NEW&sortBy=startDateTime`.
  `from ≥ 0`, `size 1..100` (дефолт 20), `status` — enum или null,
  `sortBy` — `id` (дефолт) или `startDateTime`.
- **`QueryParams`** record + `parseQueryParams` + `comparatorFor` в `BaseHttpHandler`.
- **Невалидные параметры** → 400 с сообщением про конкретный параметр.
  Неизвестные параметры игнорируются. Не тримим — строгий контракт.
- **`GET /health`** → `{"status":"UP"}`.
- **`GET /version`** → `{"version":"0.2.0"}`. Версия — хардкод в `AppConfig.getVersion()`.
- **Тесты:** 12 тестов пагинации (по 3 на ресурс: happy path / фильтр / сортировка / дефолт;
  6 тестов валидации на `/tasks`; +4 теста на `/health` и `/version`).
- **`docs/openapi.yaml`** — решили не делать. Спецификация вручную, быстро устаревает.
- **Версионирование `/api/v1/...`** — не делаем.

**Тестов:** 119 (BUILD SUCCESS).

## Что делаем сейчас

**Активный этап:** Этап 7 — переработка тестов.
**Ветка:** будет `develop-stage-7-tests` (создать от main после merge этапа 6).

### Этап 7 — план

**Общие принципы:**
- `assertThrows` — только с конкретными типами исключений, не `RuntimeException`.
- Убрать все `System.out.println` из тестов.
- Детерминированное время — `LocalDateTime.of(...)` вместо `now()`.
- `@AfterEach` для очистки temp-файлов.
- HTTP-тесты — на порту 0 (свободный от ОС), не хардкод 8080.
- Проверять тело ответа, а не только статус-код, где осмысленно.

**Конкретные задачи:**
1. **`TaskManagerTest`:**
    - `assertThrows(NotFoundException.class, ...)` вместо `RuntimeException`.
    - Убрать `System.out.println`.
    - `LocalDateTime.of` вместо `now()` в тестах приоритетов и пересечений.
    - Добавить тесты на пересчёт полей `Epic` (duration/start/end/status) —
      сейчас нет или слабо покрыто.
    - Тесты на `hashCode` везде, где есть `equals`.
2. **`TaskViewHistoryTest`** — уже отвязан от `TaskManagerTest`, но добавить
   проверки фактического порядка после удаления (`removeRecord_First/Middle/Last`
   сейчас проверяют только `SUCCESS`).
3. **`LinkedHashMapCustomTest`** — проверить возврат копий, а не ссылок.
4. **`HttpTaskServerTest`:**
    - Порт 0 (`InetSocketAddress(0)` + `getAddress().getPort()`), не хардкод 8080.
    - Один `HttpServer` на класс, не на тест (сейчас `start()`/`stop()` на каждом).
    - `@AfterEach` — закрывать executor.
    - Проверять тела ответов в тестах ошибок (сейчас часто только код).
    - Заменить `.replace("NEW", "something")` на явный невалидный JSON —
      `replace` может заменить лишнее.
5. **Новые тесты:**
    - `CsvStorageTest` — прямое тестирование save/load, битые строки, отсутствие
      файла, read-only. Взамен удалённого `FileBackedTaskManagerTest`.
    - Прямые тесты CSV-парсера (`CommaSeparatedValuesCreateProcess.toString` /
      `getXFromStrings`).
    - Тесты Gson-адаптеров (`LocalDateTimeTypeAdapter`, `DurationTypeAdapter`,
      `AbstractTaskDeserializer` — в т.ч. отсутствующий `type`).
    - Тесты на потерянные ветки `ResultOfOperation`.
6. **Параметризованные тесты:**
    - Эпик: набор статусов подзадач → статус эпика (7+ кейсов в один
      `@ParameterizedTest`).
    - Пересечение интервалов: 8–10 наборов (касание, вложение, полное совпадение).
7. **`catch_Exception_When_Save_File_Read_Only_Test`** — судьба (нестабилен
   на Windows): либо `@DisabledOnOs(OS.WINDOWS)`, либо удалить, либо заменить
   другим способом эмулировать read-only.

### Открытые мелочи

- `isTimeIntersectBoth` и `isTimeIntersectWithOthers` в `TaskManager` — public,
  нужны тестам. В этапе 7 — либо оставить (тесты), либо package-private +
  перенести тесты в `service`.
- `getTotalOfIdInWork` — public, нужен одному тесту.
- `service/interfaces/` — директория опустела, можно удалить.
- В `pom.xml` версия `0.2.0-SNAPSHOT` и `AppConfig.getVersion()` `"0.2.0"` —
  две правды. Синхронизировать в этапе 8 (через Maven resource filtering).

### Дальнейшие этапы

- **Этап 8 (инфраструктура, опционально):** Checkstyle, SpotBugs, JaCoCo,
  GitHub Actions, Dockerfile, docker-compose, синхронизация версии через
  Maven resource filtering.

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

## Модель ветвления и тегов

- Префикс веток: `develop-`.
- Каждый этап — своя ветка `develop-stage-X-...`.
- После этапа: PR на GitHub (base `main`), merge commit, тег на merge-коммит.

**Теги:** `v0.1-before-refactor`, `v0.2-fixes`, `v0.3-http`, `v0.4-storage`,
`v0.5-concurrency` — на main. `v0.6-api` — после merge текущей ветки.
`v0.7-tests`, `v1.0-portfolio` — в будущем.

## Соглашения по коду и тестам

- Отвечать по-русски.
- **Тесты:** `@Nested`, `@DisplayName`, имена `methodName_Should_ExpectedBehavior_Test`.
  `assertThrows` — только с конкретными типами исключений. Без `System.out.println`
  (в этапе 7). Детерминированное время, не `now()` (в этапе 7). `@AfterEach` для
  temp-файлов (в этапе 7). HTTP-тесты на порту 0 (в этапе 7).
- **Code fence:** для вложенных — четыре бэктика снаружи.
- Файлы присылаются целиком; не выдумывать методы/поля.
- **Gson** работает через no-arg конструктор + reflection.

## Окружение и известные грабли

- Windows, IDEA, JDK 21 (собираем под 17).
- Maven не в PATH системного bash. Используем `./mvnw ...`.
- Пути в bash — прямые слэши: `git add src/main`, не `src\main`.
- В Surefire прописан `argLine` с UTF-8. При запуске из IDEA-панели —
  `JAVA_TOOL_OPTIONS` в `Settings → Build → Build Tools → Maven → Runner`.
- `mvn clean` может падать с `Failed to delete target` — файл-лок на Windows
  (IDEA, антивирус, проводник). Лечится закрытием IDEA и `rm -rf target`.
- `.gitattributes` нормализует `.java` в LF. Warning про CRLF — нормально.
- `idNumber = -1` — маркер «пустышка». Gson даёт `-1` при POST без `id`;
  хендлеры нормализуют в `0` в ветке создания.
- **Пересечение интервалов включает касание границ.** `t1.end == t2.start`
  считается пересечением. В тестах с массовым добавлением задач нужен
  достаточный шаг между `startDateTime` (например, шаг 20 мин при duration 10).
- **IDEA: тесты не запускаются через кнопку Run, а `./mvnw test` работает.**
  Симптом: `package org.junit.jupiter.api does not exist`. Причина: IDEA
  использует свой native build system, не Maven. Решение: Maven-панель →
  правый клик по корню → **Unlink Maven Projects**, затем правый клик по
  `pom.xml` → **Add as Maven Project**.
- **IDEA: Compact Middle Packages** — включить, чтобы дерево не разъезжалось.
  Путь: `File → Settings → Advanced Settings → Project View → Compact Middle Packages`.
- **Бэкап проекта:** PowerShell-скрипт `backup-javaProject.ps1` — зеркалирование
  `G:\javaProject` на флешку с меткой `BACKUP` через `robocopy /MIR`. Скрипт
  требует **UTF-8 with BOM** (иначе PowerShell 5.1 ломает кириллицу).
  Автоопределение флешки через `Get-Volume | Where FileSystemLabel -eq 'BACKUP'`.

## Ожидаемое действие от ассистента в новом чате

1. **Прочитать `context.md` целиком.**
2. **Подтвердить понимание в одном-двух предложениях:** активный этап, следующий
   шаг, ветка.
3. **Задать уточняющий вопрос:** «На какой стадии этапа мы сейчас? Что уже
   закоммичено, что ещё нет?»
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
- **PLAN.md** — локальный, в `.gitignore`, в репо не публикуется.