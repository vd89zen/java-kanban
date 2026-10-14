# java-kanban — контекст для продолжения работы

## Проект

Backend-сервис для управления задачами по методологии Kanban. Портфолио-проект на базе
учебного Яндекс Практикума, в процессе доработки.

Репозиторий: https://github.com/vd89zen/java-kanban
Активная ветка: `develop-stage-3-http` (этапы 0-1-2 закрыты, этап 3 в работе).

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

## Maven-конфигурация (pom.xml)

- `groupId` — `ru.yandex.practicum`
- `artifactId` — `java-kanban`
- `version` — `0.2.0-SNAPSHOT`
- `finalName` — `java-kanban` (jar без версии: `target/java-kanban.jar`)
- `main.class` — `ru.yandex.practicum.java.tasktracker.TaskTrackerApplication`
- `maven.compiler.release` — 17 (НЕ `source`/`target`)

**Зависимости:**

| Артефакт | Версия |
|---|---|
| com.google.code.gson:gson | 2.11.0 |
| org.slf4j:slf4j-api | 2.0.13 |
| ch.qos.logback:logback-classic | 1.5.6 |
| org.junit.jupiter:junit-jupiter | 5.10.2 (test) |

**Плагины:** maven-compiler 3.13.0, maven-surefire 3.2.5 (argLine с UTF-8),
maven-shade 3.5.3 (ServicesResourceTransformer + ManifestResourceTransformer),
exec-maven-plugin 3.2.0.

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
        TaskManager (interface)
        ├── InMemoryTaskManager
        │      └── InMemoryHistoryManager (LinkedHashMapCustom)
        └── FileBackedTaskManager (CSV поверх InMemory)
```

Пакеты:

| Пакет | Назначение |
|---|---|
| `config` | `AppConfig` — чтение env/sysprop |
| `http` | `HttpTaskServer` |
| `http.handler` | Хендлеры, `BaseHttpHandler` |
| `manage` | `ManagersUtil` |
| `service` | `InMemoryTaskManager`, `FileBackedTaskManager`, `InMemoryHistoryManager` |
| `service.interfaces` | `TaskManager`, `HistoryManager` |
| `task` | `Task`, `Epic`, `Subtask` |
| `utils` | `AbstractTask`, `LinkedHashMapCustom`, `TextForGson`, `CommaSeparatedValuesCreateProcess` |
| `utils.enums` | `StatusProgress`, `TypesTasks`, `ResultOfOperation`, `HttpResponseCode` |
| `utils.gson` | `LocalDateTimeTypeAdapter`, `DurationTypeAdapter`, `AbstractTaskDeserializer` |
| `exceptions` | `BadRequestException`, `NotFoundException`, `ManagerSaveException` |

## Структура файлов

```
.
├── .gitattributes
├── .gitignore
├── .mvn/wrapper/maven-wrapper.properties
├── README.md
├── context.md
├── mvnw
├── mvnw.cmd
├── pom.xml
├── src/
│   ├── main/
│   │   ├── java/ru/yandex/practicum/java/tasktracker/
│   │   │   ├── TaskTrackerApplication.java
│   │   │   ├── config/AppConfig.java
│   │   │   ├── exceptions/...
│   │   │   ├── http/
│   │   │   │   ├── HttpTaskServer.java
│   │   │   │   └── handler/...  (Base + 5 хендлеров)
│   │   │   ├── manage/ManagersUtil.java
│   │   │   ├── service/...
│   │   │   ├── task/...
│   │   │   └── utils/...
│   │   └── resources/logback.xml
│   └── test/java/...  (12 тестовых классов)
```

## Ключевые сигнатуры

### `TaskManager` (интерфейс)

```
ArrayList<AbstractTask> getPrioritizedTasks()
boolean isTimeIntersectBoth(AbstractTask, AbstractTask)
boolean isTimeIntersectWithOthers(AbstractTask)
ArrayList<AbstractTask> getHistory()
int getTotalOfIdInWork()
ResultOfOperation addTask(Task)
Task getTaskByIdNumber(int)                    // throws NotFoundException
ResultOfOperation removeTaskByIdNumber(int)    // throws NotFoundException
ResultOfOperation updateTask(Task)             // throws NotFoundException
ArrayList<Task> getAllTasks()                  // throws NotFoundException
ResultOfOperation removeAllTasks()
ResultOfOperation addEpic(Epic)
Epic getEpicByIdNumber(int)                    // throws NotFoundException
ResultOfOperation removeEpicByIdNumber(int)
ResultOfOperation updateEpicName(int, String)
ResultOfOperation updateEpicDescription(int, String)
ArrayList<Subtask> getAllSubtasksFromEpic(int) // throws NotFoundException
ArrayList<Epic> getAllEpics()                  // throws NotFoundException
ResultOfOperation removeAllEpics()
ResultOfOperation addSubtask(Subtask)
Subtask getSubtaskByIdNumber(int)              // throws NotFoundException
ResultOfOperation removeSubtaskByIdNumber(int) // throws NotFoundException
ResultOfOperation updateSubtask(Subtask)       // throws NotFoundException
ArrayList<Subtask> getAllSubtasks()            // throws NotFoundException
ResultOfOperation removeAllSubtasks()
```

### `HistoryManager` (интерфейс)

```
ResultOfOperation addRecord(AbstractTask)
ResultOfOperation removeRecord(int)
ArrayList<AbstractTask> getHistory()
```

### `InMemoryTaskManager`

Все методы — из `TaskManager`. Поля (protected): `allIdInWork`, `tasks`, `epics`,
`subtasks`, `prioritizedTasks`. Поля `taskForWork`/`epicForWork`/`subtaskForWork`
удалены в этапе 2.

### `FileBackedTaskManager extends InMemoryTaskManager`

```
static FileBackedTaskManager loadFromFileUsingMethods(File)
static FileBackedTaskManager loadFromFileDirectlyToMap(File)
FileBackedTaskManager(File)
// переопределены все мутационные методы
```

`save()` сортирует записи по id, `Files.createDirectories` вызывается только если
у файла есть родитель (иначе NPE для `tasks.csv` в CWD).

### `ManagersUtil`

```
static TaskManager getDefault()                 // → InMemoryTaskManager
static HistoryManager getDefaultHistory()       // → InMemoryHistoryManager
static FileBackedTaskManager getFileBackedTaskManager()  // путь из AppConfig
static FileBackedTaskManager getFileBackedTaskManager(File)  // явный путь
```

### `AppConfig`

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
`Gson` десериализует через этот конструктор, поэтому при POST без `id`
получаем `idNumber = -1` — хендлеры нормализуют его в `0` перед `addXxx`.

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

### `Task extends AbstractTask`

10 конструкторов: без id / с id × (name, description, status) + опционально
startDateTime / durationInMinutes / оба. Переопределены `setStatusProgress`,
`setDurationInMinutes`, `setStartDateTime`, `resetStartDateTime`, `toString`,
`equals`, `hashCode`.

### `Subtask extends AbstractTask`

То же + `parentEpicIdNumber`. Валидация родителя вынесена в приватный
статический `validateParentEpicId(int)`, который вызывается из всех 8 конструкторов.

### `Epic extends AbstractTask`

Приватный вложенный `SubtaskFields` (4 поля: status, duration, start, end).
`SubtaskFields` имеет `equals`/`hashCode` (добавлено в этапе 1).

```
ResultOfOperation addSubtask(Subtask)
ResultOfOperation updateSubtask(Subtask)
ResultOfOperation removeSubtask(int)
Optional<ArrayList<Integer>> getSubtasksIdNumber()
void removeAllSubtasks()
Optional<LocalDateTime> getEndDateTime()
```

### `HttpTaskServer`

```
HttpTaskServer() throws IOException
HttpTaskServer(TaskManager) throws IOException
void start()   // log.info("HTTP server started on port {}", port)
void stop()    // log.info("HTTP server stopped")
```

Порт читается из `AppConfig.getServerPort()`.

### `BaseHttpHandler`

```
BaseHttpHandler(TaskManager)
protected void sendText(HttpExchange, String)             // 200 + application/json
protected void sendCreated(HttpExchange, String)          // 201
protected void sendNotFound(HttpExchange, String)         // 404
protected void sendHasInteractions(HttpExchange, String)  // 406 (в этапе 3 → 409)
protected void sendBadRequest(HttpExchange, String)       // 400
protected void sendError(HttpExchange, String)            // 500
protected void writeResponse(HttpExchange, String, int)
```

### Хендлеры

Все наследуют `BaseHttpHandler`, реализуют `HttpHandler.handle(HttpExchange)`.
В ветке создания (`id <= 0`) нормализуют `taskFromJson.setIdNumber(0)` перед `addXxx`.

### `CommaSeparatedValuesCreateProcess`

```
String toString(Task) / toString(Subtask) / toString(Epic)
Task getTaskFromStrings(String[])
Subtask getSubtaskFromStrings(String[])
Epic getEpicFromStrings(String[])
String getHeadersForCSV()
```

`setFieldsFromStrings` проверяет `fields.length >= 10` и ловит `NumberFormatException`
по каждому числовому полю (добавлено в этапе 1).

Заголовок CSV: `id,type,name,description,statusProgress,parentEpic,childSubtasks,durationInMinutes,startDateTime,endDateTime`.
Плейсхолдеры: `SKIP` (поле неприменимо), `EMPTY` (нет значения).

### Enums

- `StatusProgress`: `NEW`, `IN_PROGRESS`, `DONE`.
- `TypesTasks`: `TASK`, `EPIC`, `SUBTASK`.
- `HttpResponseCode`: `OK(200)`, `CREATED(201)`, `BAD_REQUEST(400)`,
  `NOT_FOUND(404)`, `NOT_ACCEPTABLE(406)`, `INTERNAL_SERVER_ERROR(500)`.
  В этапе 3 добавим 204, 405, 409, 415.
- `ResultOfOperation`: 13 значений — `SUCCESS`, `ERROR_ID_LESS_ZERO`,
  `ERROR_OBJECT_NULL`, `ERROR_OBJECT_FIELDS_NULL`, `ERROR_OBJECT_NOT_FOUND`,
  `ERROR_OBJECT_ALREADY_EXISTS`, `ERROR_OBJECT_ILLEGAL`, `ERROR_SUBTASKS_NOT_EMPTY`,
  `ERROR_MISMATCH_PARENT_ID`, `ERROR_NO_AVAILABLE_ID`, `ERROR_DURATION_LESS_ZERO`,
  `ERROR_START_NOT_SET`, `ERROR_INTERSECT_TIME`.

## Что уже сделано

### Этапы 0-1-2 закрыты (merge PR #8, тег `v0.2-fixes`)

**Этап 0 — Maven:**
- Добавлен `pom.xml`, Maven Wrapper, `.gitattributes`.
- Исходники в Maven-layout: `src/main/java/...`, `src/test/java/...`.
- `Main` → `TaskTrackerApplication`.
- `logback.xml` (заготовка логирования).
- Удалены `lib/`, `out/`.

**Этап 1 — баг-фиксы:**
- `AbstractTask()` — `durationInMinutes = Duration.ZERO` (было `null` → NPE).
- `Epic.SubtaskFields` — добавлены `equals`/`hashCode`.
- `CommaSeparatedValuesCreateProcess.setFieldsFromStrings` — bounds-check + обработка
  `NumberFormatException`.
- `AbstractTaskDeserializer` — `JsonParseException` при отсутствии `type`.
- `LinkedHashMapCustom.createCopy` — убрана мёртвая проверка `if (this == null)`.
- `toString` в `Task`/`Epic`/`Subtask`/`TextForGson` — закрыта фигурная скобка.
- `Subtask` — дедупликация валидации родителя (`validateParentEpicId`).
- `System.out` → SLF4J (`HttpTaskServer`, `TaskTrackerApplication`).

**Этап 2 — code smells:**
- `AppConfig` — чтение порта и пути к данным из env/sysprop.
- `InMemoryTaskManager` — убраны поля `taskForWork`/`epicForWork`/`subtaskForWork`.
- `FileBackedTaskManager.save` — сортировка по id.
- `DataForUserScenario` — удалён.
- `getAllSubtasksFromEpic` — через `epic.getSubtasksIdNumber()`, проверка существования эпика.
- `ManagersUtil` — перегруженный `getFileBackedTaskManager(File)`.

**Бонус (нашли в ручных проверках):**
- POST без `id` (или с `id <= 0`) создаёт ресурс, а не падает в update.
  В хендлерах три места с `if (idNumber <= 0) { taskFromJson.setIdNumber(0); addXxx(...); }`.
- NPE в `FileBackedTaskManager.save` для файла без родителя (`tasks.csv` в CWD).
  Теперь `Files.createDirectories` вызывается только если `parentPath != null`.

**Проверено:**
- `./mvnw clean test` — 200 tests, BUILD SUCCESS.
- `java -jar target/java-kanban.jar` — сервер стартует, POST/GET работают, `tasks.csv` создаётся.
- `SERVER_PORT=9090` и `DATA_FILE=./custom.csv` — читаются из env.

## Что делаем сейчас

**Активный этап:** Этап 3 — HTTP-семантика.
**Ветка:** `develop-stage-3-http`.

### Этап 3 — список правок

1. `HttpResponseCode`: добавить `NO_CONTENT(204)`, `METHOD_NOT_ALLOWED(405)`,
   `CONFLICT(409)`, `UNSUPPORTED_MEDIA_TYPE(415)`. Убрать `NOT_ACCEPTABLE(406)`,
   если больше не используется.
2. `BaseHttpHandler`: новые `sendNoContent`, `sendMethodNotAllowed` (с заголовком `Allow`),
   `sendConflict`, `sendUnsupportedMediaType`.
3. `Content-Type` всех ответов об ошибках → `application/json` (не `application/problem+json`).
4. `POST` создание → `201 Created` + заголовок `Location: /<resource>/{id}`.
   `POST` обновление (id > 0) → `200 OK` (сейчас 201 на оба случая).
5. `DELETE` → `204 No Content`, пустое тело (сейчас 200 + тело).
6. Пересечение интервалов → `409 Conflict` (сейчас `406 Not Acceptable`).
7. Неверный `Content-Type` → `415 Unsupported Media Type` (сейчас 400).
8. Неподдерживаемый метод → `405 Method Not Allowed` + `Allow` (сейчас 400).
9. Пустой список `GET /tasks|/subtasks|/epics` → `200 OK` + `[]`
   (сейчас 404). `NotFoundException` остаётся только для «по id не найдено».

### Дальнейшие этапы (вкратце)

- **Этап 4 (хранилище):** `TaskStorage` + `CsvStorage`, `InMemoryStorage`,
  композиция вместо наследования, `FileBackedTaskManager` удаляется.
- **Этап 5 (потокобезопасность):** `ConcurrentHashMap` + точечные `synchronized`,
  `ExecutorService` в `HttpTaskServer`.
- **Этап 6 (API, опционально):** пагинация, `/health`, `/version`, OpenAPI-спека.
- **Этап 7 (тесты):** переработка — см. отдельный план.
- **Этап 8 (инфраструктура, опционально):** Checkstyle, SpotBugs, JaCoCo,
  GitHub Actions, Dockerfile, docker-compose.

## Принятые решения

| № | Решение |
|---|---|
| 1 | Система сборки: Maven, минимальный `pom.xml` |
| 2 | Логирование: SLF4J + Logback |
| 3 | Путь к данным: `./tasks.csv` через env/sysprop |
| 4 | `DataForUserScenario` — удалён (этап 2) |
| 5 | `DELETE` → 204 No Content (этап 3) |
| 6 | Пересечение интервалов → 409 Conflict (этап 3) |
| 7 | Неверный `Content-Type` → 415 (этап 3) |
| 8 | `Content-Type` ошибок → `application/json` (этап 3) |
| 9 | Пустой список → 200 + `[]` (этап 3) |
| 10 | Хранилище: `TaskStorage` + композиция (этап 4) |
| 11 | Потокобезопасность: `ConcurrentHashMap` + `synchronized` (этап 5) |

## Модель ветвления и тегов

- Префикс веток: **`develop-`**.
- Каждый этап — своя ветка `develop-stage-X-...`.
- После этапа: PR на GitHub (base `main`), merge commit, тег на merge-коммит.

**Теги:**
- `v0.1-before-refactor` — на main (точка старта).
- `v0.2-fixes` — на main (этапы 0-1-2).
- `v0.3-http` — после этапа 3.
- `v0.4-storage`, `v0.5-concurrency`, `v0.6-api`, `v0.7-tests`, `v1.0-portfolio` — в будущем.

## Соглашения по коду и тестам

- Отвечать по-русски.
- **Тесты:**
    - `@Nested`, `@DisplayName`, имена `methodName_Should_ExpectedBehavior_Test`.
    - `assertThrows` — только с конкретными типами исключений, не `RuntimeException`.
    - Без `System.out.println` (в этапе 7 убираем отладочные print-ы).
    - Детерминированное время (`LocalDateTime.of`), не `now()`.
    - `@AfterEach` для очистки temp-файлов.
    - HTTP-тесты — на порту 0, проверять тело ответа.
- **Code fence:** для вложенных — четыре бэктика снаружи.
- Файлы присылаются целиком; не выдумывать методы/поля, которых нет в разделе
  «Ключевые сигнатуры».
- **Gson работает через no-arg конструктор + reflection.** Поля, не указанные
  в JSON, остаются такими, какими их сделал no-arg конструктор (`idNumber = -1`,
  `durationInMinutes = Duration.ZERO`). Валидации из конструкторов с аргументами
  при десериализации не срабатывают — это надо учитывать.

## Окружение и известные грабли

- Windows, IDEA, JDK 21 (собираем под 17).
- Maven не в PATH системного bash. Используем `./mvnw ...` из корня.
- Пути в bash — только прямые слэши: `git add src/main`, не `src\main`.
- В Surefire прописан `argLine` с UTF-8 для stdout/stderr — иначе на Windows
  русские сообщения превращаются в кракозябры.
- `catch_Exception_When_Save_File_Read_Only_Test` нестабилен на Windows —
  судьба решается в этапе 7.
- `.gitattributes` нормализует `.java` в LF. IDEA на Windows может сохранять CRLF,
  Git конвертирует при коммите. Warning про CRLF — нормально, содержимое не портится.
- `idNumber` по умолчанию `-1` (маркер «пустышка»). Gson даёт `-1` при POST без `id`.
  Хендлеры нормализуют в `0` в ветке создания.

## Ожидаемое действие от ассистента в новом чате

1. **Прочитать `context.md` целиком.** Понять: проект, стек, активный этап.
2. **Подтвердить понимание в одном-двух предложениях:** какой этап активен,
   что следующий, в какой ветке.
3. **Задать уточняющий вопрос:** «На какой стадии этапа мы сейчас? Что уже
   закоммичено, что ещё нет?» — пользователь мог перейти в новый чат посреди работы.
4. **Дождаться ответа.** Не начинать правки сразу.
5. **Проверить актуальность.** Если сомнения — попросить пользователя прислать
   текущие версии файлов, которые предстоит править.
6. **Не выдумывать методы и поля.** Если нужен метод, а его нет в разделе
   «Ключевые сигнатуры» — попросить файл.

## Как со мной работать

- Присылаю файлы целиком — читать внимательно, построчно.
- Не выдумывать методы и поля, которых нет в файлах.
- Если что-то не видно — просить прислать.
- Не редактировать вслепую — просить актуальную версию.
- **Не выкатывать сразу большой план на много шагов вперёд, если следующий шаг
  зависит от результата предыдущего.** Останавливаться и ждать ответа после
  каждого шага, когда есть развилка или неопределённость.
- Отвечать по-русски.
- Для ответов с вложенными code fence использовать четыре бэктика снаружи.
- Коммиты — лаконичные, но информативные.
- **Обновление `context.md` в конце каждого этапа:** новая версия подробнее
  предыдущей, заменяет старую (git-история сохранит ревизии).
- **PLAN.md** — локальный, в `.gitignore`, в репо не публикуется. При
  необходимости пользователь присылает его отдельно.