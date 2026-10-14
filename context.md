# java-kanban — контекст для продолжения работы

## Проект

Backend-сервис для управления задачами по методологии Kanban. Портфолио-проект на базе
учебного Яндекс Практикума, в процессе доработки.

Репозиторий: https://github.com/vd89zen/java-kanban
Активная ветка: `develop-stage-3-http` (этапы 0-1-2-3 закрыты, этап 4 следующий).

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
- `finalName` — `java-kanban` (`target/java-kanban.jar`)
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
ArrayList<Task> getAllTasks()                  // 200 + [] на пустом; без throws
ResultOfOperation removeAllTasks()
ResultOfOperation addEpic(Epic)
Epic getEpicByIdNumber(int)                    // throws NotFoundException
ResultOfOperation removeEpicByIdNumber(int)
ResultOfOperation updateEpicName(int, String)
ResultOfOperation updateEpicDescription(int, String)
ArrayList<Subtask> getAllSubtasksFromEpic(int) // throws NotFoundException если эпика нет
ArrayList<Epic> getAllEpics()                  // 200 + [] на пустом
ResultOfOperation removeAllEpics()
ResultOfOperation addSubtask(Subtask)
Subtask getSubtaskByIdNumber(int)              // throws NotFoundException
ResultOfOperation removeSubtaskByIdNumber(int) // throws NotFoundException
ResultOfOperation updateSubtask(Subtask)       // throws NotFoundException
ArrayList<Subtask> getAllSubtasks()            // 200 + [] на пустом
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
`subtasks`, `prioritizedTasks`.

`getAllTasks` / `getAllEpics` / `getAllSubtasks` **не бросают** `NotFoundException`
на пустой коллекции — возвращают пустой список. `getAllSubtasksFromEpic` бросает
`NotFoundException` только если эпик не существует; если эпик есть, но подзадач нет — `[]`.

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
Gson десериализует через этот конструктор, поэтому при POST без `id`
получаем `idNumber = -1` — хендлеры нормализуют в `0` перед `addXxx`.

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

10 конструкторов. Переопределены `setStatusProgress`, `setDurationInMinutes`,
`setStartDateTime`, `resetStartDateTime`, `toString`, `equals`, `hashCode`.

### `Subtask extends AbstractTask`

То же + `parentEpicIdNumber`. Валидация родителя — приватный статический
`validateParentEpicId(int)`, вызывается из всех 8 конструкторов.

### `Epic extends AbstractTask`

Приватный вложенный `SubtaskFields` (status, duration, start, end) с
`equals`/`hashCode`.

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

Порт из `AppConfig.getServerPort()`.

### `BaseHttpHandler`

Константа `CONTENT_TYPE_JSON = "application/json;charset=utf-8"`.

```
BaseHttpHandler(TaskManager)
protected void sendText(HttpExchange, String)                                       // 200
protected void sendCreated(HttpExchange, String)                                    // 201 (Location ставит хендлер)
protected void sendNoContent(HttpExchange)                                          // 204, без тела
protected void sendNotFound(HttpExchange, String)                                   // 404
protected void sendBadRequest(HttpExchange, String)                                 // 400
protected void sendHasInteractions(HttpExchange, String)                            // 406 (не используется)
protected void sendConflict(HttpExchange, String)                                   // 409
protected void sendMethodNotAllowed(HttpExchange, String, String allowedMethods)    // 405 + Allow
protected void sendUnsupportedMediaType(HttpExchange, String)                       // 415
protected void sendError(HttpExchange, String)                                      // 500
protected void writeResponse(HttpExchange, String, int)
```

### Хендлеры

Все наследуют `BaseHttpHandler`, реализуют `HttpHandler.handle(HttpExchange)`.

**Общее поведение POST:**
- `idNumber <= 0` → создание (нормализуют `setIdNumber(0)`, вызывают `addXxx`,
  при успехе отдают `201` + `Location: /{resource}/{id}`).
- `idNumber > 0` → обновление, при успехе отдают `200 OK` (кроме `EpicsHandler` —
  там update запрещён, `400`).

**DELETE** (Tasks / Subtasks / Epics): `204 No Content`.
**Неверный Content-Type:** `415` (в `Tasks`, `Subtasks`, `Epics`).
**Неподдерживаемый метод:** `405` + заголовок `Allow` (во всех пяти).
**Пересечение интервалов:** `409` (в `Tasks`, `Subtasks`).
**Пути GET:** неверный путь — `400` (в `History`, `Prioritized` тоже).

### `CommaSeparatedValuesCreateProcess`

```
String toString(Task) / toString(Subtask) / toString(Epic)
Task getTaskFromStrings(String[])
Subtask getSubtaskFromStrings(String[])
Epic getEpicFromStrings(String[])
String getHeadersForCSV()
```

`setFieldsFromStrings` проверяет `fields.length >= 10` и ловит `NumberFormatException`
по каждому числовому полю.

Заголовок CSV: `id,type,name,description,statusProgress,parentEpic,childSubtasks,durationInMinutes,startDateTime,endDateTime`.
Плейсхолдеры: `SKIP`, `EMPTY`.

### Enums

- `StatusProgress`: `NEW`, `IN_PROGRESS`, `DONE`.
- `TypesTasks`: `TASK`, `EPIC`, `SUBTASK`.
- `HttpResponseCode`: `OK(200)`, `CREATED(201)`, `NO_CONTENT(204)`,
  `BAD_REQUEST(400)`, `NOT_FOUND(404)`, `METHOD_NOT_ALLOWED(405)`,
  `NOT_ACCEPTABLE(406)`, `CONFLICT(409)`, `UNSUPPORTED_MEDIA_TYPE(415)`,
  `INTERNAL_SERVER_ERROR(500)`.
  `NOT_ACCEPTABLE` пока остаётся в enum, но `sendHasInteractions` не вызывается —
  чистится в этапе 4.
- `ResultOfOperation`: 13 значений.

## Что уже сделано

### Этапы 0-1-2 закрыты (merge PR #8, тег `v0.2-fixes`)

**Этап 0 — Maven:**
- `pom.xml`, Maven Wrapper, `.gitattributes`.
- Исходники в Maven-layout.
- `Main` → `TaskTrackerApplication`.
- `logback.xml`.
- Удалены `lib/`, `out/`.

**Этап 1 — баг-фиксы:**
- `AbstractTask()`: `durationInMinutes = Duration.ZERO` (было `null` → NPE).
- `Epic.SubtaskFields` — `equals`/`hashCode`.
- `CommaSeparatedValuesCreateProcess.setFieldsFromStrings` — bounds-check.
- `AbstractTaskDeserializer` — `JsonParseException` при отсутствии `type`.
- `LinkedHashMapCustom.createCopy` — убрана мёртвая проверка.
- `toString` в `Task`/`Epic`/`Subtask`/`TextForGson` — закрыта скобка.
- `Subtask` — дедупликация валидации родителя.
- `System.out` → SLF4J.

**Этап 2 — code smells:**
- `AppConfig` — env/sysprop.
- `InMemoryTaskManager` — убраны mutable-поля `taskForWork`/`epicForWork`/`subtaskForWork`.
- `FileBackedTaskManager.save` — сортировка по id.
- `DataForUserScenario` — удалён.
- `getAllSubtasksFromEpic` — через `epic.getSubtasksIdNumber()`.
- `ManagersUtil` — перегруженный `getFileBackedTaskManager(File)`.

**Бонус (из ручных проверок):**
- POST без `id` создаёт ресурс.
- NPE в `FileBackedTaskManager.save` для файла без родителя.

### Этап 3 закрыт (ветка `develop-stage-3-http`, ждёт merge)

**Коды и хелперы:**
- `HttpResponseCode`: добавлены `NO_CONTENT(204)`, `METHOD_NOT_ALLOWED(405)`,
  `CONFLICT(409)`, `UNSUPPORTED_MEDIA_TYPE(415)`.
- `BaseHttpHandler`: константа `CONTENT_TYPE_JSON`, новые методы
  `sendNoContent`, `sendConflict`, `sendMethodNotAllowed` (с `Allow`),
  `sendUnsupportedMediaType`. Content-Type ошибок переведён с
  `application/problem+json` на `application/json`.

**Семантика:**
- `POST` создание → `201 Created` + `Location: /{resource}/{id}`.
- `POST` обновление → `200 OK` (в `Tasks` и `Subtasks`).
- `DELETE` → `204 No Content` (без тела).
- Пересечение интервалов → `409 Conflict` (было `406`).
- Неверный `Content-Type` → `415 Unsupported Media Type` (было `400`).
- Неподдерживаемый метод → `405 Method Not Allowed` + `Allow` (было `400`).
- `GET /tasks | /subtasks | /epics` на пустом списке → `200 OK` + `[]`
  (было `404`). `NotFoundException` остаётся только для «по id не найдено».
- `getAllSubtasksFromEpic` — пустой эпик → `[]`, несуществующий → `404`.

**Тесты:** синхронизированы под новые коды и поведение.

**Проверено:** `./mvnw clean test` — BUILD SUCCESS. Ручные curl-проверки через
два терминала — POST/GET/DELETE работают, файл `tasks.csv` создаётся.

## Что делаем сейчас

**Активный этап:** Этап 4 — рефакторинг хранилища.
**Ветка:** будет `develop-stage-4-storage` (создать от main после merge `v0.3-http`).

### Этап 4 — план

1. Ввести интерфейс `TaskStorage`:
   ```
   void save(Collection<Task> tasks, Collection<Epic> epics, Collection<Subtask> subtasks)
   LoadedState load()
   ```
   `LoadedState` — record с тремя коллекциями (или отдельные `loadTasks`/`loadEpics`/
   `loadSubtasks` — решаем по ходу).
2. Реализации:
    - `InMemoryStorage` — no-op.
    - `CsvStorage` — перенос логики CSV из `FileBackedTaskManager`.
3. `InMemoryTaskManager` принимает `TaskStorage` в конструкторе (может быть `null`
   или `InMemoryStorage`). После каждой успешной мутации — `storage.save(...)`.
4. `FileBackedTaskManager` удаляется как класс.
   Фабрика: `ManagersUtil.getFileBacked(File)` → `new InMemoryTaskManager(new CsvStorage(file))`.
5. Восстановление из файла: в конструкторе (или отдельным `init()`) вызывается
   `storage.load()`, коллекции наполняются.

### Дальнейшие этапы

- **Этап 5 (потокобезопасность):** `ConcurrentHashMap` + точечные `synchronized`,
  `ExecutorService` в `HttpTaskServer`.
- **Этап 6 (API, опционально):** пагинация, `/health`, `/version`, OpenAPI-спека.
- **Этап 7 (тесты):** переработка — см. отдельный план.
- **Этап 8 (инфраструктура, опционально):** Checkstyle, SpotBugs, JaCoCo,
  GitHub Actions, Dockerfile, docker-compose.

### Открытые мелочи

- `BaseHttpHandler.sendHasInteractions` и `HttpResponseCode.NOT_ACCEPTABLE` больше
  не используются — удалить.
- `HistoryHandler` и `PrioritizedHandler` используют `sendBadRequest` на неверный
  путь — это ок, оставляем 400.
- В тестах всё ещё есть `System.out.println` — убираются в этапе 7.

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
| 10 | Хранилище: `TaskStorage` + композиция (этап 4) |
| 11 | Потокобезопасность: `ConcurrentHashMap` + `synchronized` (этап 5) |

## Модель ветвления и тегов

- Префикс веток: `develop-`.
- Каждый этап — своя ветка `develop-stage-X-...`.
- После этапа: PR на GitHub (base `main`), merge commit, тег на merge-коммит.

**Теги:**
- `v0.1-before-refactor`, `v0.2-fixes` — на main.
- `v0.3-http` — после merge текущей ветки.
- `v0.4-storage`, `v0.5-concurrency`, `v0.6-api`, `v0.7-tests`, `v1.0-portfolio` — в будущем.

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
- Файлы присылаются целиком; не выдумывать методы/поля, которых нет в разделе
  «Ключевые сигнатуры».
- **Gson работает через no-arg конструктор + reflection.** Поля из JSON без явного
  значения остаются такими, какими их сделал no-arg конструктор (`idNumber = -1`,
  `durationInMinutes = Duration.ZERO`). Валидации из конструкторов с аргументами
  при десериализации не срабатывают.

## Окружение и известные грабли

- Windows, IDEA, JDK 21 (собираем под 17).
- Maven не в PATH системного bash. Используем `./mvnw ...` из корня.
- Пути в bash — только прямые слэши: `git add src/main`, не `src\main`.
- В Surefire прописан `argLine` с UTF-8 — избавляет от кракозябр в форке.
  При запуске из IDEA-панели Maven — `JAVA_TOOL_OPTIONS` в
  `Settings → Build → Build Tools → Maven → Runner → Environment variables`.
- `catch_Exception_When_Save_File_Read_Only_Test` нестабилен на Windows —
  судьба решается в этапе 7.
- `.gitattributes` нормализует `.java` в LF. Warning про CRLF — нормально.
- `idNumber` по умолчанию `-1` (маркер «пустышка»). Gson даёт `-1` при POST без `id`.
  Хендлеры нормализуют в `0` в ветке создания.
- `mvn clean` может падать с `Failed to delete target` — файл-лок на Windows
  (IDEA, антивирус, проводник). Лечится закрытием IDEA и `rm -rf target`.

## Ожидаемое действие от ассистента в новом чате

1. **Прочитать `context.md` целиком.** Понять: проект, стек, активный этап.
2. **Подтвердить понимание в одном-двух предложениях:** какой этап активен,
   что следующий, в какой ветке.
3. **Задать уточняющий вопрос:** «На какой стадии этапа мы сейчас? Что уже
   закоммичено, что ещё нет?» — пользователь мог перейти в новый чат посреди работы.
4. **Дождаться ответа.** Не начинать правки сразу.
5. **Проверить актуальность.** Если сомнения — попросить текущие версии файлов,
   которые предстоит править.
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
- **PLAN.md** — локальный, в `.gitignore`, в репо не публикуется.