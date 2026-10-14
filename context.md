# java-kanban — контекст для продолжения работы

## Проект

Backend-сервис для управления задачами по методологии Kanban. Портфолио-проект на базе
учебного Яндекс Практикума, в процессе доработки.

Репозиторий: https://github.com/vd89zen/java-kanban
Активная ветка: `develop-stage-0-1-2-fixes` (этап 0 завершён, этап 1 в работе).

**Стек:** Java 17+, `com.sun.net.httpserver.HttpServer`, Gson, Maven, JUnit 5,
SLF4J + Logback (логгер внедряется в этапе 1).

**Окружение:** Windows, IntelliJ IDEA, JDK 21 (компиляция под 17 через `maven.compiler.release`).

**Как собирать:**
- В bash: `./mvnw clean test`, `./mvnw clean package`
- В cmd: `mvnw.cmd clean test`
- В IDEA: панель Maven, настроена на **Use Maven wrapper**

**Как запускать:**
- `java -jar target/java-kanban.jar` (fat-jar через shade)
- `./mvnw exec:java` (запуск main-класса без сборки jar)

## Maven-конфигурация (pom.xml)

- `groupId` — `ru.yandex.practicum`
- `artifactId` — `java-kanban`
- `version` — `0.2.0-SNAPSHOT` (поднимется до `1.0.0` в конце рефакторинга)
- `finalName` — `java-kanban` (jar без версии: `target/java-kanban.jar`)
- `main.class` — `ru.yandex.practicum.java.tasktracker.TaskTrackerApplication`
- `maven.compiler.release` — 17 (НЕ `source`/`target`, это важно)

**Зависимости:**

| Артефакт | Версия |
|---|---|
| com.google.code.gson:gson | 2.11.0 |
| org.slf4j:slf4j-api | 2.0.13 |
| ch.qos.logback:logback-classic | 1.5.6 |
| org.junit.jupiter:junit-jupiter | 5.10.2 (test) |

**Плагины:**

| Плагин | Версия | Назначение |
|---|---|---|
| maven-compiler-plugin | 3.13.0 | компиляция |
| maven-surefire-plugin | 3.2.5 | тесты; `argLine` с UTF-8 для Windows |
| maven-shade-plugin | 3.5.3 | fat-jar; ServicesResourceTransformer + ManifestResourceTransformer |
| exec-maven-plugin | 3.2.0 | `mvn exec:java` для быстрого запуска |

**Maven Wrapper:** `mvnw`, `mvnw.cmd`, `.mvn/wrapper/maven-wrapper.properties`.
Сгенерирован bundled Maven из IDEA. IDEA переключена на Use Maven wrapper.

## Архитектура (текущая)

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
│   │   │   ├── exceptions/
│   │   │   │   ├── BadRequestException.java
│   │   │   │   ├── ManagerSaveException.java
│   │   │   │   └── NotFoundException.java
│   │   │   ├── http/
│   │   │   │   ├── HttpTaskServer.java
│   │   │   │   └── handler/
│   │   │   │       ├── BaseHttpHandler.java
│   │   │   │       ├── EpicsHandler.java
│   │   │   │       ├── HistoryHandler.java
│   │   │   │       ├── PrioritizedHandler.java
│   │   │   │       ├── SubtasksHandler.java
│   │   │   │       └── TasksHandler.java
│   │   │   ├── manage/
│   │   │   │   └── ManagersUtil.java
│   │   │   ├── service/
│   │   │   │   ├── FileBackedTaskManager.java
│   │   │   │   ├── InMemoryHistoryManager.java
│   │   │   │   ├── InMemoryTaskManager.java
│   │   │   │   └── interfaces/
│   │   │   │       ├── HistoryManager.java
│   │   │   │       └── TaskManager.java
│   │   │   ├── task/
│   │   │   │   ├── Epic.java
│   │   │   │   ├── Subtask.java
│   │   │   │   └── Task.java
│   │   │   └── utils/
│   │   │       ├── AbstractTask.java
│   │   │       ├── CommaSeparatedValuesCreateProcess.java
│   │   │       ├── DataForUserScenario.java
│   │   │       ├── LinkedHashMapCustom.java
│   │   │       ├── TextForGson.java
│   │   │       ├── enums/
│   │   │       │   ├── HttpResponseCode.java
│   │   │       │   ├── ResultOfOperation.java
│   │   │       │   ├── StatusProgress.java
│   │   │       │   └── TypesTasks.java
│   │   │       └── gson/
│   │   │           ├── AbstractTaskDeserializer.java
│   │   │           ├── DurationTypeAdapter.java
│   │   │           └── LocalDateTimeTypeAdapter.java
│   │   └── resources/
│   │       └── logback.xml
│   └── test/
│       └── java/ru/yandex/practicum/java/tasktracker/
│           ├── http/HttpTaskServerTest.java
│           ├── manage/
│           │   ├── FileBackedTaskManagerTest.java
│           │   ├── InMemoryHistoryManagerTest.java
│           │   ├── InMemoryTaskManagerTest.java
│           │   ├── LinkedHashMapCustomTest.java
│           │   ├── ManagersUtilTest.java
│           │   └── TaskManagerTest.java
│           └── task/
│               ├── EpicTest.java
│               ├── SubtaskTest.java
│               └── TaskTest.java
```

## Ключевые сигнатуры

Ниже — сигнатуры публичных методов из `javap`. Использовать при правках,
не выдумывать методы, которых нет.

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

Все методы — из `TaskManager`, плюс:

```
boolean isTimeIntersectBoth(AbstractTask, AbstractTask)  // public
```

Поля (protected, доступны подклассу `FileBackedTaskManager`):
`allIdInWork`, `tasks`, `epics`, `subtasks`, `prioritizedTasks`.

### `FileBackedTaskManager extends InMemoryTaskManager`

```
static FileBackedTaskManager loadFromFileUsingMethods(File)
static FileBackedTaskManager loadFromFileDirectlyToMap(File)
FileBackedTaskManager(File)
// переопределены все мутационные методы: addTask, removeAllTasks, updateTask,
// removeTaskByIdNumber, addEpic, updateEpicName, updateEpicDescription,
// removeAllEpics, removeEpicByIdNumber, addSubtask, removeAllSubtasks,
// removeSubtaskByIdNumber, updateSubtask
```

### `InMemoryHistoryManager implements HistoryManager`

```
InMemoryHistoryManager()
ResultOfOperation addRecord(AbstractTask)
ResultOfOperation removeRecord(int)
ArrayList<AbstractTask> getHistory()
```

### `ManagersUtil` (final)

```
static TaskManager getDefault()                 // → InMemoryTaskManager
static HistoryManager getDefaultHistory()       // → InMemoryHistoryManager
static FileBackedTaskManager getFileBackedTaskManager()  // путь "resources/tasks.csv"
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

Внутренний `NodeCustom` — двусвязный узел, поля `task`, `previousNode`, `nextNode`.
Внутренний `HashMap<Integer, NodeCustom> innerHashMap`.

### `AbstractTask` (абстрактный)

Поля: `idNumber`, `name`, `description`, `statusProgress`, `type`,
`durationInMinutes` (Duration), `startDateTime` (LocalDateTime).

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
Optional<LocalDateTime> getEndDateTime()   // start + duration, empty если start null
```

### `Task extends AbstractTask`

Конструкторы (10): без id / с id × (name, description, status) + опционально
startDateTime / durationInMinutes / оба.

Переопределены: `setStatusProgress`, `setDurationInMinutes`, `setStartDateTime`,
`resetStartDateTime`, `toString`, `equals`, `hashCode`.

### `Subtask extends AbstractTask`

То же + `parentEpicIdNumber` (int). Конструкторы аналогичны, но с параметром
`parentEpicIdNumber`.

```
ResultOfOperation setParentEpicIdNumber(int)
int getParentEpicIdNumber()
```

### `Epic extends AbstractTask`

Внутри — `HashMap<Integer, SubtaskFields> subtasksFields` (приватный вложенный класс).
Переопределён `getEndDateTime()` — берёт из пересчёта по подзадачам.

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
HttpTaskServer() throws IOException                       // ManagersUtil.getDefault()
HttpTaskServer(TaskManager) throws IOException
void start()
void stop()
```

Константа `PORT = 8080` (пока хардкод — исправляется в этапе 2).

### `BaseHttpHandler`

```
BaseHttpHandler(TaskManager)
protected void sendText(HttpExchange, String)             // 200 + application/json
protected void sendCreated(HttpExchange, String)          // 201
protected void sendNotFound(HttpExchange, String)         // 404
protected void sendHasInteractions(HttpExchange, String)  // 406 (будет 409)
protected void sendBadRequest(HttpExchange, String)       // 400
protected void sendError(HttpExchange, String)            // 500
protected void writeResponse(HttpExchange, String, int)
protected Gson gson
protected TaskManager taskManager
```

Хендлеры (`TasksHandler`, `SubtasksHandler`, `EpicsHandler`, `HistoryHandler`,
`PrioritizedHandler`) реализуют `HttpHandler.handle(HttpExchange)`.

### `CommaSeparatedValuesCreateProcess`

```
String toString(Task) / toString(Subtask) / toString(Epic)
Task getTaskFromStrings(String[])
Subtask getSubtaskFromStrings(String[])
Epic getEpicFromStrings(String[])
String getHeadersForCSV()
```

Заголовок CSV: `id,type,name,description,statusProgress,parentEpic,childSubtasks,durationInMinutes,startDateTime,endDateTime`.

Плейсхолдеры: `SKIP` (поле неприменимо), `EMPTY` (нет значения).

### Gson-адаптеры

- `LocalDateTimeTypeAdapter` — формат `HH:mm, dd:MM:yyyy`, `"EMPTY"` для null.
- `DurationTypeAdapter` — число минут.
- `AbstractTaskDeserializer` — по полю `"type"` (`TASK`/`SUBTASK`/`EPIC`).

### Enums

- `StatusProgress`: `NEW`, `IN_PROGRESS`, `DONE`.
- `TypesTasks`: `TASK`, `EPIC`, `SUBTASK`.
- `HttpResponseCode`: `OK(200)`, `CREATED(201)`, `BAD_REQUEST(400)`,
  `NOT_FOUND(404)`, `NOT_ACCEPTABLE(406)`, `INTERNAL_SERVER_ERROR(500)`.
  (В этапе 3 добавим 204, 405, 409, 415.)
- `ResultOfOperation`: 13 значений — `SUCCESS`, `ERROR_ID_LESS_ZERO`,
  `ERROR_OBJECT_NULL`, `ERROR_OBJECT_FIELDS_NULL`, `ERROR_OBJECT_NOT_FOUND`,
  `ERROR_OBJECT_ALREADY_EXISTS`, `ERROR_OBJECT_ILLEGAL`, `ERROR_SUBTASKS_NOT_EMPTY`,
  `ERROR_MISMATCH_PARENT_ID`, `ERROR_NO_AVAILABLE_ID`, `ERROR_DURATION_LESS_ZERO`,
  `ERROR_START_NOT_SET`, `ERROR_INTERSECT_TIME`.

## Что уже сделано

- Учебный проект завершён и работает: HTTP-API, история, приоритеты, CSV-хранилище, тесты.
- Проведён аудит кода и тестов, составлен план доработки (в локальном PLAN.md, в .gitignore).
- На main стоит тег `v0.1-before-refactor` — точка старта до всех правок.
- **Этап 0 (ветка `develop-stage-0-1-2-fixes`, завершён):**
    - Добавлен Maven (`pom.xml`), Maven Wrapper (`mvnw`, `mvnw.cmd`, `.mvn/`).
    - Добавлен `.gitattributes` для нормализации переводов строк.
    - Исходники переложены в Maven-layout: `src/main/java/...`, `src/test/java/...`.
    - `Main` переименован в `TaskTrackerApplication`.
    - Добавлен `src/main/resources/logback.xml` (заготовка логирования).
    - Удалены `lib/` (ручные jar-ы) и `out/`.
    - `ServicesResourceTransformer` добавлен в shade-плагин.
    - Версия: 0.2.0-SNAPSHOT. Сборка зелёная: 200 тестов, BUILD SUCCESS.
    - Тег `v0.2-fixes` ещё НЕ ставился — по плану он после этапов 0-1-2.

## Что делаем сейчас

**Активный этап:** Этап 1 — баг-фиксы.
**Следующий шаг:** правки в коде (список из 8 пунктов ниже).
**Ветка:** `develop-stage-0-1-2-fixes` (та же, что и для этапа 0).

### Этап 1 — список правок

1. `AbstractTask` default-конструктор: `durationInMinutes = Duration.ZERO` вместо `null`
   (сейчас NPE в `getDurationInMinutes()` и `toString()`).
2. `Epic.SubtaskFields`: добавить `equals`/`hashCode` по 4 полям
   (чинит `Epic.equals`/`hashCode` на непустом наборе подзадач).
3. `CommaSeparatedValuesCreateProcess.setFieldsFromStrings`: проверять `fields.length`,
   ловить `NumberFormatException`, бросать осмысленное исключение.
4. `AbstractTaskDeserializer`: если `jsonObject.get("type")` == null или `isJsonNull()` —
   бросать `JsonParseException("Field 'type' is required")`.
5. `LinkedHashMapCustom.createCopy`: убрать мёртвую проверку `if (this == null)`.
6. `toString` в `Task`/`Epic`/`Subtask`: закрыть фигурную скобку в шаблоне.
7. Заменить `System.out` в `HttpTaskServer` на SLF4J.
8. Заменить `System.out` в `TaskTrackerApplication` на SLF4J.

После завершения этапов 1 и 2 (code smells, HTTP-семантика частично) — merge в main,
тег `v0.2-fixes`, следующая ветка `develop-stage-3-http`.

### Этап 2 — code smells

1. `InMemoryTaskManager`: убрать поля `taskForWork`/`epicForWork`/`subtaskForWork`
   (сделать локальными).
2. Конфигурация через env/sysprop: `SERVER_PORT`/`server.port` (по умолчанию 8080),
   `DATA_FILE`/`data.file` (по умолчанию `./tasks.csv`).
3. `FileBackedTaskManager.save`: сортировать записи по `idNumber` для стабильного diff.
4. `DataForUserScenario` — удалить.
5. `getAllSubtasksFromEpic` — переписать через `epic.getSubtasksIdNumber()`.
6. `ManagersUtil`: фабрики принимают параметры, а не хардкодят.
7. Проверить все места с `"resources/tasks.csv"` — путь из config.

### Дальнейшие этапы (вкратце)

- **Этап 3 (HTTP-семантика):** 204 на DELETE, 409 на пересечение, 415 на неверный
  Content-Type, 405 на неподдерживаемый метод, `application/json` для ошибок,
  200 + `[]` для пустых списков, `Location` при 201.
- **Этап 4 (хранилище):** `TaskStorage` + `CsvStorage`, `InMemoryStorage`,
  `FileBackedTaskManager` удаляется, композиция вместо наследования.
- **Этап 5 (потокобезопасность):** `ConcurrentHashMap` + точечные `synchronized`,
  `ExecutorService` в `HttpTaskServer`.
- **Этап 6 (API, опционально):** пагинация, `/health`, `/version`, OpenAPI-спека.
- **Этап 7 (тесты):** переработка — см. отдельный план.
- **Этап 8 (инфраструктура, опционально):** Checkstyle, SpotBugs, JaCoCo,
  GitHub Actions, Dockerfile, docker-compose.

## Принятые решения (по плану)

| № | Решение |
|---|---|
| 1 | Система сборки: **Maven**, минимально необходимый `pom.xml` |
| 2 | Логирование: **SLF4J + Logback** |
| 3 | Путь к данным: **`./tasks.csv`** (env `DATA_FILE` / sysprop `data.file`) |
| 4 | `DataForUserScenario` — **удалить** |
| 5 | `DELETE` → **204 No Content** |
| 6 | Пересечение интервалов → **409 Conflict** (вместо 406) |
| 7 | Неверный `Content-Type` → **415 Unsupported Media Type** |
| 8 | `Content-Type` ответов об ошибках → **`application/json`** |
| 9 | Пустой список `GET /tasks` → **200 OK + `[]`** |
| 10 | Хранилище: **`TaskStorage` + композиция** |
| 11 | Потокобезопасность: **`ConcurrentHashMap` + точечные `synchronized`** |

## Модель ветвления и тегов

- Префикс веток: **`develop-`**.
- Этапы 0-1-2 — одной веткой `develop-stage-0-1-2-fixes`.
- Каждый следующий этап — своя ветка `develop-stage-X-...`.
- После этапа: merge в `main` через `--no-ff`, ветку удалить, тег на merge-коммит.

Теги:
- `v0.1-before-refactor` — уже стоит на main.
- `v0.2-fixes` — после этапов 0-1-2 (ещё не поставлен).
- `v0.3-http`, `v0.4-storage`, `v0.5-concurrency`, `v0.6-api`, `v0.7-tests`,
  `v1.0-portfolio` — в будущем.

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
- Файлы присылаются целиком; не выдумывать методы/поля, которых нет в списке
  сигнатур выше.

## Окружение и известные грабли

- Windows, IDEA, JDK 21 (собираем под 17).
- **Maven не в PATH системного bash.** В IDEA Maven home path переключён на
  «Use Maven wrapper». Из bash запускаем через `./mvnw ...`.
- Пути в bash — только прямые слэши: `git add src/main`, не `src\main`.
- В Surefire прописан `argLine` с UTF-8 для stdout/stderr — иначе на Windows
  русские сообщения превращаются в кракозябры.
- `catch_Exception_When_Save_File_Read_Only_Test` нестабилен на Windows —
  судьба решается в этапе 7.
- `ManagersUtil.getFileBackedTaskManager()` использует относительный путь
  `"resources/tasks.csv"` — исправляется в этапе 2.
- `DataForUserScenario` в `utils/` — не продуктовый код, удаляется в этапе 2.
- В `pom.xml` используется `maven.compiler.release` (не `source`/`target`) —
  это устраняет warning «system modules path not set» и гарантирует API Java 17.

## Ожидаемое действие от ассистента в новом чате

Если пользователь прислал этот файл без дополнительных указаний:

1. **Прочитать `context.md` целиком.** Понять: проект, стек, текущий этап.
2. **Подтвердить понимание в одном-двух предложениях:** какой этап активен,
   какой следующий шаг, в какой ветке работаем.
3. **Задать уточняющий вопрос:** «На какой стадии этапа мы сейчас? Что уже
   закоммичено, что ещё нет? Нужно ли сначала проверить текущее состояние
   файлов?» — потому что пользователь мог перейти в новый чат посреди работы.
4. **Дождаться ответа.** Не начинать правки сразу — сначала выяснить точку.
5. **Проверить актуальность.** Если есть сомнения — попросить пользователя
   прислать текущие версии файлов, которые предстоит править (не выдумывать
   содержимое по памяти из этого контекста).
6. **Не выдумывать методы и поля, которых нет в разделе «Ключевые сигнатуры».**
   Если нужен метод, а его нет в списке — попросить файл.

## Как со мной работать

- Присылаю файлы целиком — читать внимательно, построчно, не пропускать
  семантические ошибки (типы, сигнатуры, nullable-поля).
- Не выдумывать методы и поля, которых нет в файлах.
- Если что-то не видно — просить прислать.
- Не редактировать вслепую — просить актуальную версию.
- Отвечать по-русски.
- Для ответов с вложенными code fence использовать четыре бэктика снаружи.
- Коммиты — лаконичные, но информативные.
- **Обновление `context.md` в конце каждого этапа:** новая версия подробнее
  предыдущей, заменяет старую (git-история сохранит ревизии).
- **PLAN.md** — локальный, в `.gitignore`, в репо не публикуется. При
  необходимости пользователь присылает его отдельно.