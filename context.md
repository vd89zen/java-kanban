# java-kanban — контекст для продолжения работы

## Проект

Backend-сервис для управления задачами по методологии Kanban. Портфолио-проект на базе
учебного Яндекс Практикума, в процессе доработки.

Репозиторий: https://github.com/vd89zen/java-kanban
Активная ветка: `develop-stage-5-concurrency` (этапы 0-1-2-3-4-5 закрыты, этап 6 следующий).

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
  └── HttpTaskServer (com.sun.net.httpserver)  ← ExecutorService (fixed pool, 8 потоков)
        ├── /tasks        → TasksHandler
        ├── /subtasks     → SubtasksHandler
        ├── /epics        → EpicsHandler
        ├── /history      → HistoryHandler
        └── /prioritized  → PrioritizedHandler
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

Приватные поля:
- `int nextId` — монотонный счётчик id.
- `final Set<Integer> allIdInWork` — `ConcurrentHashMap.newKeySet()`.
- `final Map<Integer, Task> tasks`, `epics`, `subtasks` — `ConcurrentHashMap`.
- `final NavigableSet<AbstractTask> prioritizedTasks` — `ConcurrentSkipListSet`.
- `final TaskViewHistory historyManager`.
- `final TaskStorage storage`.
- `final Object lock` — единый лок для мутаций.

Приватные методы: `generateIdNumber()` (возвращает `int` через `nextId`),
`addToPriorityList(AbstractTask)`, `initFromStorage()`, `persist()`,
`verifyIntegrity()`.

Публичные методы: те же сигнатуры, что в этапе 4 (см. git history).

**Потокобезопасность:**
- Все мутации — под `synchronized (lock)`.
- Проверки, зависящие от общего состояния (уникальность id, пересечение
  интервалов, наличие родителя), внутри лока.
- Генерация id, изменения коллекций — внутри лока.
- `persist()` — вне лока (I/O не держит блокировку).
- Читающие методы — без лока (concurrent-коллекции).
- Порядок захвата: сначала `lock`, затем монитор `TaskViewHistory`. Обратного
  пути нет — deadlock исключён.

**Javadoc на классе** описывает атомарность, персистенцию и порядок локов.

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

См. этап 4. `CsvStorage.save` сортирует по id, `load` пропускает битые
строки с warn-логом.

### `manage.ManagersUtil`

```
static TaskManager getDefault()
static TaskViewHistory getDefaultHistory()
static TaskManager getFileBacked()
static TaskManager getFileBacked(File)
```

### `config.AppConfig`

```
static int getServerPort()
static String getDataFilePath()
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

Поля: `HttpServer`, `TaskManager`, `int port`, `ExecutorService executor` (fixed, 8).
`THREAD_POOL_SIZE = 8`, `SHUTDOWN_TIMEOUT_SECONDS = 1`.

### `BaseHttpHandler`

Константа `CONTENT_TYPE_JSON`. Хелперы: `sendText` (200), `sendCreated` (201),
`sendNoContent` (204), `sendNotFound` (404), `sendBadRequest` (400),
`sendConflict` (409), `sendMethodNotAllowed` (405 + `Allow`),
`sendUnsupportedMediaType` (415), `sendError` (500).

### Хендлеры

**Общее:** POST создание → 201 + Location, обновление → 200 (кроме `EpicsHandler`
— 400), DELETE → 204, неверный `Content-Type` → 415, неподдерживаемый метод → 405,
пересечение интервалов → 409, пустой список → 200 + [].

### `CommaSeparatedValuesCreateProcess`

Как в этапе 4.

### Enums

- `StatusProgress`: NEW, IN_PROGRESS, DONE.
- `TypesTasks`: TASK, EPIC, SUBTASK.
- `HttpResponseCode`: 200, 201, 204, 400, 404, 405, 409, 415, 500.
- `ResultOfOperation`: 13 значений.

## Что уже сделано

### Этапы 0-1-2 закрыты (PR #8, тег `v0.2-fixes`)

Maven + wrapper, Maven-layout, `Main` → `TaskTrackerApplication`, logback.
Баг-фиксы: NPE в `AbstractTask()`, `Epic.SubtaskFields` equals/hashCode,
bounds-check в CSV, null-check в десериализаторе, `toString` в 4 классах,
дедупликация `Subtask`, SLF4J. Code smells: `AppConfig`, убраны mutable-поля,
сортировка CSV, удалён `DataForUserScenario`, `getAllSubtasksFromEpic` через данные
эпика. Плюс: POST без `id` создаёт ресурс, NPE в `save` для файла без родителя.

### Этап 3 закрыт (PR #9, тег `v0.3-http`)

HTTP-семантика: 201+Location на создание, 200 на update, 204 на delete,
409 на пересечение, 415 на неверный Content-Type, 405 на метод, 200+[] на пустом
списке. `sendHasInteractions` и `NOT_ACCEPTABLE` удалены в этапе 4.

### Этап 4 закрыт (PR #10, тег `v0.4-storage`)

`TaskStorage` + `LoadedState`, `NoopStorage` + `CsvStorage`.
`InMemoryTaskManager` → `TaskManager`, `InMemoryHistoryManager` → `TaskViewHistory`.
`FileBackedTaskManager` удалён. Интерфейсы `TaskManager` и `HistoryManager` удалены.
`initFromStorage()` + `verifyIntegrity()` (warn + удаление orphan-подзадач).
Тесты: `FileBackedTaskManagerTest` и `InMemoryTaskManagerTest` удалены,
`TaskManagerTest` и `TaskViewHistoryTest` в `service`.

### Этап 5 закрыт (ветка `develop-stage-5-concurrency`, ждёт merge)

- **Генерация id:** монотонный `nextId`, без переиспользования. Убраны
  `counterForIdNumber` и `isRestarted`. `initFromStorage` восстанавливает
  `nextId = max(existing ids)`. Пользовательские id (заданные вручную) —
  `nextId = max(nextId, id)`.
- **Concurrent-коллекции:** `ConcurrentHashMap` для `tasks`/`epics`/`subtasks`,
  `ConcurrentHashMap.newKeySet()` для `allIdInWork`, `ConcurrentSkipListSet` для
  `prioritizedTasks`.
- **Единый лок `private final Object lock`:** все мутационные методы обёрнуты
  в `synchronized (lock)`. Проверки, зависящие от общего состояния, — под локом.
  `persist()` — вне лока.
- **`TaskViewHistory`:** методы `addRecord`/`removeRecord`/`getHistory` —
  `synchronized`. Порядок локов: `TaskManager.lock` → монитор истории.
- **Javadoc на `TaskManager`** — атомарность, персистенция, порядок захвата локов,
  исключение deadlock.
- **`HttpTaskServer`:** явный `ExecutorService` (`newFixedThreadPool(8)`),
  `setExecutor`, в `stop()` — `shutdownNow()` + `awaitTermination(1s)`.
- **Тест `TaskManagerConcurrencyTest`:** 16 потоков × 50 задач = 800 параллельных
  `addTask`. Проверяет: все SUCCESS, все 800 задач в менеджере, все id уникальны.

**Тестов:** 103 (BUILD SUCCESS).

## Что делаем сейчас

**Активный этап:** Этап 6 — расширение API (опционально).
**Ветка:** будет `develop-stage-6-api` (создать от main после merge).

### Этап 6 — план (все пункты опциональны)

1. **Пагинация** для `GET /tasks | /subtasks | /epics`:
   `?from=0&size=20&status=NEW&sortBy=startDateTime`.
   Реализация: фильтрация + сортировка + пагинация в стриме.
2. **`GET /health`** → `200 {"status":"UP"}`.
3. **`GET /version`** → `200 {"version":"0.2.0"}`.
4. **`docs/openapi.yaml`** — ручная спека, без UI.
5. Версионирование `/api/v1/...` — **НЕ ДЕЛАЕМ** (ломает текущие URL).

### Открытые мелочи

- `isTimeIntersectBoth` и `isTimeIntersectWithOthers` — пока `public`, нужны
  тестам. Станут `private` в этапе 7, если тесты переедут/изменятся.
- `getTotalOfIdInWork` — `public`, нужен одному тесту.
- `service/interfaces/` — директория опустела, можно удалить.
- В тестах всё ещё есть `System.out.println` — убираются в этапе 7.

### Дальнейшие этапы

- **Этап 7 (тесты):** переработка — конкретные исключения, детерминированное время,
  порт 0, `@AfterEach`, тела ответов, разделение на contract/integration.
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
| 10 | Хранилище: `TaskStorage` + композиция |
| 11 | Потокобезопасность: `ConcurrentHashMap` + единый `synchronized (lock)` |
| 12 | Менеджер без интерфейса; полиморфизм на `TaskStorage` |
| 13 | Генерация id: монотонный счётчик, без переиспользования |

## Модель ветвления и тегов

- Префикс веток: `develop-`.
- Каждый этап — своя ветка `develop-stage-X-...`.
- После этапа: PR на GitHub (base `main`), merge commit, тег на merge-коммит.

**Теги:**
- `v0.1-before-refactor`, `v0.2-fixes`, `v0.3-http`, `v0.4-storage` — на main.
- `v0.5-concurrency` — после merge текущей ветки.
- `v0.6-api`, `v0.7-tests`, `v1.0-portfolio` — в будущем.

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
- `mvn clean` может падать с `Failed to delete target` — файл-лок на Windows.
  Лечится закрытием IDEA и `rm -rf target`.
- `.gitattributes` нормализует `.java` в LF. Warning про CRLF — нормально.
- `idNumber = -1` — маркер «пустышка». Gson даёт `-1` при POST без `id`;
  хендлеры нормализуют в `0` в ветке создания.

### IDEA: тесты не запускаются через кнопку Run, но `./mvnw test` работает

Симптом: при запуске отдельного теста из IDEA — `package org.junit.jupiter.api does not exist`.
Через `./mvnw clean test` всё зелёно.

Причина: IDEA использует свой native build system, а не Maven. После крупных
изменений в `pom.xml` или перекладки исходников IDEA может потерять classpath.

**Решение, которое сработало:**
1. В правой панели Maven → правый клик по корневому проекту → **Unlink Maven Projects**.
2. Правый клик по `pom.xml` → **Add as Maven Project**.

Альтернативы (не помогли в этом случае, но помогают обычно):
- **Reload All Maven Projects** (кнопка в панели Maven).
- Проверить `Settings → Build → Build Tools → Maven → Ignored Files` — не
  должен быть отмечен `pom.xml`.
- `Settings → Build → Build Tools → Maven → Runner → Delegate IDE build/run actions to Maven`.
- В крайнем случае — удалить `.idea/` и `*.iml`, открыть заново через `pom.xml`.

### IDEA: Compact Middle Packages

Чтобы дерево проекта не разъезжалось «лесенкой» (`ru → yandex → practicum → java → tasktracker`),
включить **Compact Middle Packages**. Путь в новых версиях:
`File → Settings → Advanced Settings → Project View → Compact Middle Packages`.
В контекстном меню правого клика по папке этого пункта может не быть.

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