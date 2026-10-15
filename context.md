# java-kanban — контекст для продолжения работы

## Проект

Backend-сервис для управления задачами по методологии Kanban. Портфолио-проект на базе
учебного Яндекс Практикума, в процессе доработки.

Репозиторий: https://github.com/vd89zen/java-kanban
Активная ветка: `develop-stage-4-storage` (этапы 0-1-2-3-4 закрыты, этап 5 следующий).

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
  └── HttpTaskServer (com.sun.net.httpserver)
        ├── /tasks        → TasksHandler
        ├── /subtasks     → SubtasksHandler
        ├── /epics        → EpicsHandler
        ├── /history      → HistoryHandler
        └── /prioritized  → PrioritizedHandler
                │
                ▼
        TaskManager (класс)
        ├── TaskViewHistory (LinkedHashMapCustom)
        └── TaskStorage (интерфейс)
              ├── NoopStorage  (no-op, для чистой in-memory)
              └── CsvStorage   (CSV-персистенция)
```

**Ключевые решения:**
- Интерфейсы `TaskManager` и `HistoryManager` **удалены** — реализация одна,
  полиморфизм переехал на уровень `TaskStorage`.
- `FileBackedTaskManager` **удалён** — заменён на композицию
  `TaskManager(new CsvStorage(file))`.
- Менеджер держит состояние в `HashMap`, после каждой успешной мутации вызывает
  `storage.save(...)`. При создании — `storage.load()`, `initFromStorage()`.

Пакеты:

| Пакет | Назначение |
|---|---|
| `config` | `AppConfig` — чтение env/sysprop |
| `http` | `HttpTaskServer` |
| `http.handler` | Хендлеры, `BaseHttpHandler` |
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
│   │   │   ├── http/handler/...  (Base + 5 хендлеров)
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
│       ├── service/TaskManagerTest.java
│       ├── service/TaskViewHistoryTest.java
│       └── task/...  (EpicTest, SubtaskTest, TaskTest)
```

## Ключевые сигнатуры

### `service.TaskManager` (класс; бывший `InMemoryTaskManager`)

Конструкторы:
```
TaskManager()                      // внутри new NoopStorage()
TaskManager(TaskStorage storage)   // storage != null
```

Приватные методы: `generateIdNumber()`, `addToPriorityList(AbstractTask)`,
`initFromStorage()`, `persist()`, `verifyIntegrity()`.

Публичные методы (реализация интерфейса, который удалён — но сигнатуры те же):

```
ArrayList<AbstractTask> getPrioritizedTasks()
boolean isTimeIntersectBoth(AbstractTask, AbstractTask)         // пока public, станет private в этапе 7
boolean isTimeIntersectWithOthers(AbstractTask)                 // пока public, станет private в этапе 7
ArrayList<AbstractTask> getHistory()
int getTotalOfIdInWork()
ResultOfOperation addTask(Task)
Task getTaskByIdNumber(int)                    // throws NotFoundException
ResultOfOperation removeTaskByIdNumber(int)    // throws NotFoundException
ResultOfOperation updateTask(Task)             // throws NotFoundException
ArrayList<Task> getAllTasks()                  // 200 + [] на пустом
ResultOfOperation removeAllTasks()
ResultOfOperation addEpic(Epic)
Epic getEpicByIdNumber(int)                    // throws NotFoundException
ResultOfOperation removeEpicByIdNumber(int)
ResultOfOperation updateEpicName(int, String)
ResultOfOperation updateEpicDescription(int, String)
ArrayList<Subtask> getAllSubtasksFromEpic(int) // throws NotFoundException если эпика нет; [] если подзадач нет
ArrayList<Epic> getAllEpics()                  // 200 + []
ResultOfOperation removeAllEpics()
ResultOfOperation addSubtask(Subtask)
Subtask getSubtaskByIdNumber(int)              // throws NotFoundException
ResultOfOperation removeSubtaskByIdNumber(int) // throws NotFoundException
ResultOfOperation updateSubtask(Subtask)       // throws NotFoundException
ArrayList<Subtask> getAllSubtasks()            // 200 + []
ResultOfOperation removeAllSubtasks()
```

Поля (protected): `allIdInWork`, `tasks`, `epics`, `subtasks`, `prioritizedTasks`.
Плюс `private final TaskStorage storage`, `private final TaskViewHistory historyManager`.

### `service.TaskViewHistory` (класс; бывший `InMemoryHistoryManager`)

```
TaskViewHistory()
ResultOfOperation addRecord(AbstractTask)
ResultOfOperation removeRecord(int)
ArrayList<AbstractTask> getHistory()
```

Поле `LinkedHashMapCustom history`.

### `storage.TaskStorage` (интерфейс)

```
void save(Collection<Task> tasks, Collection<Epic> epics, Collection<Subtask> subtasks)
LoadedState load()
```

### `storage.LoadedState` (record)

```
record LoadedState(Collection<Task> tasks, Collection<Epic> epics, Collection<Subtask> subtasks)
```

### `storage.NoopStorage`

`save()` — пустой, `load()` — пустой `LoadedState`.

### `storage.CsvStorage`

```
CsvStorage(File file)
void save(...)     // сортировка по id, createDirectories если есть родитель
LoadedState load() // парсит CSV, пропускает битые строки с warn-логом
```

Заголовок CSV: `id,type,name,description,statusProgress,parentEpic,childSubtasks,durationInMinutes,startDateTime,endDateTime`.
Плейсхолдеры: `SKIP`, `EMPTY`.

### `manage.ManagersUtil`

```
static TaskManager getDefault()                     // new TaskManager()
static TaskViewHistory getDefaultHistory()          // new TaskViewHistory()
static TaskManager getFileBacked()                  // TaskManager(new CsvStorage(AppConfig.getDataFilePath()))
static TaskManager getFileBacked(File)              // TaskManager(new CsvStorage(file))
```

### `config.AppConfig`

```
static int getServerPort()      // env SERVER_PORT / -Dserver.port / 8080
static String getDataFilePath() // env DATA_FILE / -Ddata.file / "tasks.csv"
```

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

### `AbstractTask` (абстрактный)

Поля: `idNumber`, `name`, `description`, `statusProgress`, `type`,
`durationInMinutes` (Duration), `startDateTime` (LocalDateTime).

Default-конструктор: `idNumber = -1`, `durationInMinutes = Duration.ZERO`.
**Gson десериализует через no-arg конструктор + reflection.** При POST без `id`
получаем `idNumber = -1` — хендлеры нормализуют в `0` в ветке создания.

Публичные методы:
```
Optional<String> getName()
ResultOfOperation setName(String)
Optional<String> getDescription()
ResultOfOperation setDescription(String)
ResultOfOperation setIdNumber(int)
int getIdNumber()
StatusProgress getStatusProgress()
TypesTasks getTypeTask()
long getDurationInMinutes()
Optional<LocalDateTime> getStartDateTime()
Optional<LocalDateTime> getEndDateTime()
```

### `Task` / `Subtask` / `Epic`

Как раньше. `Subtask` — валидация родителя в приватном
`validateParentEpicId(int)`. `Epic` — вложенный `SubtaskFields` с
`equals`/`hashCode`.

### `HttpTaskServer`

```
HttpTaskServer() throws IOException                    // TaskManager()
HttpTaskServer(TaskManager) throws IOException
void start()   // log.info("HTTP server started on port {}", port)
void stop()    // log.info("HTTP server stopped")
```

Порт из `AppConfig.getServerPort()`.

### `BaseHttpHandler`

Константа `CONTENT_TYPE_JSON = "application/json;charset=utf-8"`.

```
BaseHttpHandler(TaskManager)
protected void sendText(HttpExchange, String)                                       // 200
protected void sendCreated(HttpExchange, String)                                    // 201
protected void sendNoContent(HttpExchange)                                          // 204, без тела
protected void sendNotFound(HttpExchange, String)                                   // 404
protected void sendBadRequest(HttpExchange, String)                                 // 400
protected void sendConflict(HttpExchange, String)                                   // 409
protected void sendMethodNotAllowed(HttpExchange, String, String allowedMethods)    // 405 + Allow
protected void sendUnsupportedMediaType(HttpExchange, String)                       // 415
protected void sendError(HttpExchange, String)                                      // 500
protected void writeResponse(HttpExchange, String, int)
```

`sendHasInteractions` и `NOT_ACCEPTABLE` удалены в этапе 4.

### Хендлеры

**Общее:**
- `POST`: `idNumber <= 0` → создание (`setIdNumber(0)`, `addXxx`, `201` + `Location`).
  `idNumber > 0` → update, `200 OK` (кроме `EpicsHandler` — `400`).
- `DELETE` → `204 No Content`.
- Неверный `Content-Type` → `415`.
- Неподдерживаемый метод → `405` + `Allow`.
- Пересечение интервалов → `409`.

### `CommaSeparatedValuesCreateProcess`

```
String toString(Task) / toString(Subtask) / toString(Epic)
Task getTaskFromStrings(String[])
Subtask getSubtaskFromStrings(String[])
Epic getEpicFromStrings(String[])
String getHeadersForCSV()
```

`setFieldsFromStrings` проверяет `fields.length >= 10` и ловит
`NumberFormatException`.

### Enums

- `StatusProgress`: `NEW`, `IN_PROGRESS`, `DONE`.
- `TypesTasks`: `TASK`, `EPIC`, `SUBTASK`.
- `HttpResponseCode`: `OK(200)`, `CREATED(201)`, `NO_CONTENT(204)`,
  `BAD_REQUEST(400)`, `NOT_FOUND(404)`, `METHOD_NOT_ALLOWED(405)`,
  `CONFLICT(409)`, `UNSUPPORTED_MEDIA_TYPE(415)`, `INTERNAL_SERVER_ERROR(500)`.
- `ResultOfOperation`: 13 значений.

## Что уже сделано

### Этапы 0-1-2 закрыты (PR #8, тег `v0.2-fixes`)

**Этап 0 — Maven:** pom.xml, wrapper, `.gitattributes`, Maven-layout,
`Main` → `TaskTrackerApplication`, logback.xml, удалены lib/ и out/.

**Этап 1 — баг-фиксы:** NPE в `AbstractTask()`, `Epic.SubtaskFields`
equals/hashCode, bounds-check в CSV, null-check в десериализаторе,
`toString` в 4 классах, дедупликация валидации в `Subtask`, SLF4J.

**Этап 2 — code smells:** `AppConfig`, убраны mutable-поля
`taskForWork`/`epicForWork`/`subtaskForWork`, сортировка CSV, удалён
`DataForUserScenario`, `getAllSubtasksFromEpic` через данные эпика,
перегруженный `getFileBackedTaskManager(File)`. Плюс:
- POST без `id` создаёт ресурс.
- NPE в `FileBackedTaskManager.save` для файла без родителя.

### Этап 3 закрыт (PR #9, тег `v0.3-http`)

- `HttpResponseCode`: +204, 405, 409, 415.
- `BaseHttpHandler`: константа `CONTENT_TYPE_JSON`, хелперы
  `sendNoContent`, `sendConflict`, `sendMethodNotAllowed`,
  `sendUnsupportedMediaType`. Content-Type ошибок → `application/json`.
- `POST` создание → `201` + `Location`. Обновление → `200`.
- `DELETE` → `204`.
- Пересечение → `409` (было `406`).
- Неверный `Content-Type` → `415`.
- Неподдерживаемый метод → `405` + `Allow`.
- Пустой список `GET /tasks|/subtasks|/epics` → `200 + []`.

### Этап 4 закрыт (ветка `develop-stage-4-storage`, ждёт merge)

- **Интерфейс `TaskStorage`** + record `LoadedState`.
- **Реализации:** `NoopStorage` (no-op), `CsvStorage` (CSV).
- **`TaskManager`** — бывший `InMemoryTaskManager`, теперь принимает
  `TaskStorage` в конструкторе, вызывает `storage.save(...)` после
  каждой мутации, `initFromStorage()` — при создании, `verifyIntegrity()` —
  удаляет orphan-подзадачи (ссылающиеся на несуществующий эпик) с warn-логом.
- **`FileBackedTaskManager` удалён.** Логика перенесена в `CsvStorage`.
- **`TaskViewHistory`** — бывший `InMemoryHistoryManager`.
- **Интерфейсы `TaskManager` и `HistoryManager` удалены.** Полиморфизм
  переехал на `TaskStorage`.
- **`sendHasInteractions` и `NOT_ACCEPTABLE` удалены.**
- **Тесты:** `FileBackedTaskManagerTest` и `InMemoryTaskManagerTest` удалены;
  `TaskManagerTest` и `TaskViewHistoryTest` переехали в пакет `service`
  (первый — конкретный, второй — самостоятельный, без наследования от
  `TaskManagerTest`).
- **Количество тестов:** 102 (было 200 — ушли дублирующиеся прогоны
  `TaskManagerTest` через `InMemoryHistoryManagerTest` и удалённый
  `FileBackedTaskManagerTest`).

## Что делаем сейчас

**Активный этап:** Этап 5 — потокобезопасность.
**Ветка:** будет `develop-stage-5-concurrency` (создать от main после merge).

### Этап 5 — план

1. Заменить `HashMap` на `ConcurrentHashMap` для `tasks`, `epics`, `subtasks`.
   `HashSet` → `ConcurrentHashMap.newKeySet()` для `allIdInWork`.
   `TreeSet` → `ConcurrentSkipListSet` для `prioritizedTasks`.
2. Точечные `synchronized`:
    - `generateIdNumber` + добавление в `allIdInWork` + `put` в мапу —
      один synchronized-блок (чтобы не было двух задач с одним id).
    - Проверка пересечения + добавление в `prioritizedTasks` — тоже synchronized.
    - `persist()` — вне synchronized, но с копией коллекций (snapshot),
      чтобы не держать блокировку при I/O.
    - Чтения (`getXxx`) — без synchronized, `ConcurrentHashMap` сам атомарен.
3. `HttpTaskServer` — явный `ExecutorService`:
   `httpServer.setExecutor(Executors.newFixedThreadPool(8))`.
   Пул создаётся в конструкторе, закрывается в `stop()`.
4. Javadoc на `TaskManager` — какие операции атомарны, какие нет.

### Открытые мелочи

- `isTimeIntersectBoth` и `isTimeIntersectWithOthers` пока public (нужны
  тестам). Станут private в этапе 7, вместе с переработкой тестов.
- `getTotalOfIdInWork` — public, нужен одному тесту. Возможно, станет
  package-private в этапе 7, если тесты переедут в `service`.
- `service/interfaces/` — пакет опустел, можно удалить директорию.
- В тестах всё ещё есть `System.out.println` — убираются в этапе 7.

### Дальнейшие этапы

- **Этап 6 (API, опционально):** пагинация, `/health`, `/version`, OpenAPI.
- **Этап 7 (тесты):** переработка — конкретные исключения, детерминированное
  время, порт 0, `@AfterEach`, тела ответов, разделение на contract/integration.
- **Этап 8 (инфраструктура, опционально):** Checkstyle, SpotBugs, JaCoCo,
  GitHub Actions, Dockerfile, docker-compose.

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
| 10 | Хранилище: `TaskStorage` + композиция (этап 4 — сделано) |
| 11 | Потокобезопасность: `ConcurrentHashMap` + `synchronized` (этап 5) |
| 12 | Менеджер без интерфейса; полиморфизм на `TaskStorage` (этап 4 — сделано) |

## Модель ветвления и тегов

- Префикс веток: `develop-`.
- Каждый этап — своя ветка `develop-stage-X-...`.
- После этапа: PR на GitHub (base `main`), merge commit, тег на merge-коммит.

**Теги:**
- `v0.1-before-refactor`, `v0.2-fixes`, `v0.3-http` — на main.
- `v0.4-storage` — после merge текущей ветки.
- `v0.5-concurrency`, `v0.6-api`, `v0.7-tests`, `v1.0-portfolio` — в будущем.

## Соглашения по коду и тестам

- Отвечать по-русски.
- **Тесты:**
    - `@Nested`, `@DisplayName`, имена `methodName_Should_ExpectedBehavior_Test`.
    - `assertThrows` — только с конкретными типами исключений.
    - Без `System.out.println` (в этапе 7 убираем).
    - Детерминированное время, не `now()` (в этапе 7).
    - `@AfterEach` для temp-файлов (в этапе 7).
    - HTTP-тесты — на порту 0 (в этапе 7).
- **Code fence:** для вложенных — четыре бэктика снаружи.
- Файлы присылаются целиком; не выдумывать методы/поля.
- **Gson** работает через no-arg конструктор + reflection.

## Окружение и известные грабли

- Windows, IDEA, JDK 21 (собираем под 17).
- Maven не в PATH системного bash. Используем `./mvnw ...`.
- Пути в bash — прямые слэши: `git add src/main`, не `src\main`.
- В Surefire прописан `argLine` с UTF-8. При запуске из IDEA-панели —
  `JAVA_TOOL_OPTIONS` в `Settings → Build → Build Tools → Maven → Runner`.
- `mvn clean` может падать с `Failed to delete target` — файл-лок на Windows.
  Лечится закрытием IDEA и `rm -rf target`.
- `.gitattributes` нормализует `.java` в LF. Warning про CRLF — нормально.
- `idNumber = -1` — маркер «пустышка». Gson даёт `-1` при POST без `id`;
  хендлеры нормализуют в `0` в ветке создания.

## Ожидаемое действие от ассистента в новом чате

1. **Прочитать `context.md` целиком.** Понять: проект, стек, активный этап.
2. **Подтвердить понимание в одном-двух предложениях:** какой этап активен,
   что следующий, в какой ветке.
3. **Задать уточняющий вопрос:** «На какой стадии этапа мы сейчас? Что уже
   закоммичено, что ещё нет?» — пользователь мог перейти в новый чат посреди работы.
4. **Дождаться ответа.** Не начинать правки сразу.
5. **Проверить актуальность.** Если сомнения — попросить текущие версии файлов.
6. **Не выдумывать методы и поля.** Если нужен метод, а его нет в разделе
   «Ключевые сигнатуры» — попросить файл.

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
- **Обновление `context.md` в конце каждого этапа:** новая версия подробнее
  предыдущей, заменяет старую.
- **PLAN.md** — локальный, в `.gitignore`, в репо не публикуется.