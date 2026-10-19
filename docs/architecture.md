# Архитектура

## Слои

```
HTTP → handler → TaskManager → TaskStorage (CsvStorage | NoopStorage)
                      │
                      └── TaskViewHistory (LinkedHashMapCustom)
```

## Пакеты

| Пакет | Назначение |
|---|---|
| `http` | `HttpTaskServer` — регистрация контекстов, `ExecutorService` |
| `http.handler` | Хендлеры, `BaseHttpHandler`, `QueryParams` |
| `service` | `TaskManager` (бизнес-логика), `TaskViewHistory` (история) |
| `storage` | `TaskStorage`, `LoadedState`, `NoopStorage`, `CsvStorage` |
| `task` | `Task`, `Epic`, `Subtask` |
| `utils` | `AbstractTask`, `LinkedHashMapCustom`, `TextForGson`, CSV-парсер |
| `utils.enums` | `StatusProgress`, `TypesTasks`, `ResultOfOperation`, `HttpResponseCode` |
| `utils.gson` | Адаптеры Gson для `LocalDateTime`, `Duration`, `AbstractTask` |
| `config` | `AppConfig` — чтение env/sysprop, версия |
| `manage` | `ManagersUtil` — фабрики менеджеров |
| `exceptions` | `BadRequestException`, `NotFoundException`, `ManagerSaveException` |

## Разделение ответственности

- **`TaskManager`** — единственная точка бизнес-логики. Проверки, генерация id,
  приоритеты, пересечения, каскадные удаления. Не знает про HTTP и про то, куда
  сохраняются данные.
- **`TaskStorage`** — интерфейс персистенции. `CsvStorage` пишет/читает CSV,
  `NoopStorage` — пустышка для чистой in-memory-конфигурации (NullObject pattern).
- **Хендлеры** — парсинг HTTP, валидация входных данных (query, тело), формирование
  ответа. Бизнес-логику не содержат.
- **`BaseHttpHandler`** — общий Gson, `sendXxx`-хелперы, `parseQueryParams`,
  `comparatorFor`. Наследуется всеми хендлерами.
- **`AppConfig`** — единая точка чтения конфигурации. Приоритет: env > sysprop >
  дефолт. Версия читается из `app.properties` (подставляется Maven при сборке).

## Потокобезопасность

`HttpTaskServer` использует `ExecutorService` на 8 потоков, поэтому `TaskManager`
должен быть потокобезопасным.

- **Коллекции:** `ConcurrentHashMap` для `tasks`/`epics`/`subtasks`,
  `ConcurrentHashMap.newKeySet()` для `allIdInWork`, `ConcurrentSkipListSet` для
  `prioritizedTasks`. Чтения не блокируются.
- **Мутации:** все мутационные методы (`addTask`, `updateTask`, `removeEpicByIdNumber`
  и т.д.) обёрнуты в `synchronized (lock)`, где `lock` — приватное поле
  `private final Object lock = new Object()`.
- **Под локом:** проверки, зависящие от общего состояния (уникальность id,
  пересечение интервалов, наличие родителя), генерация id, все изменения коллекций.
- **Вне лока:** `persist()` — I/O не держит блокировку.
- **`TaskViewHistory`:** методы `addRecord`/`removeRecord`/`getHistory` —
  `synchronized`. Внутри — `LinkedHashMapCustom`, не потокобезопасен.
- **Порядок захвата:** `TaskManager.lock` → монитор `TaskViewHistory`. Обратного
  пути нет (история не знает про менеджер). Deadlock исключён.

## Генерация id

Монотонный счётчик `nextId`. Удалённые id не переиспользуются автоматически.
При старте `initFromStorage()` восстанавливает `nextId = max(existing ids)`.
Пользовательские id (`id > 0`, при прямом вызове менеджера) учитываются:
`nextId = Math.max(nextId, id)`.

Почему не переиспользуем: id должны быть уникальны во времени, а не только среди
существующих объектов. Иначе старая ссылка «задача 5» после удаления и создания
новой задачи стала бы указывать на другую задачу.

## Инициализация из storage

В конструкторе `TaskManager` вызывается `initFromStorage()`:
1. `storage.load()` → `LoadedState` с тремя коллекциями.
2. Все объекты копируются в мапы, `nextId` восстанавливается.
3. Восстанавливаются связи эпик ↔ подзадача (`parent.addSubtask(subtask)`).
4. Восстанавливаются приоритеты (`prioritizedTasks.add(...)` для объектов с
   `startDateTime`).
5. `verifyIntegrity()` — ищет orphan-подзадачи (ссылающиеся на несуществующий
   эпик), удаляет их с warn-логом. Ловит битые CSV, не роняя приложение.