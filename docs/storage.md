# Хранение

## `TaskStorage` (интерфейс)

```java
void save(Collection<Task> tasks, Collection<Epic> epics, Collection<Subtask> subtasks)
LoadedState load()
```

`LoadedState` — record с тремя коллекциями. Один `load()` — один проход по файлу.

## Реализации

### `NoopStorage`

`save()` — пустой, `load()` — пустые коллекции. NullObject для чистой
in-memory-конфигурации (используется в тестах и в `ManagersUtil.getDefault()`).

### `CsvStorage`

Конструктор: `CsvStorage(File file)`. Файл не создаётся до первого `save()`.

**`save()`:**
1. Если у файла есть родительская директория — `Files.createDirectories(...)`.
2. Заголовок CSV.
3. Сортировка по id: сначала все `TASK`, потом `EPIC`, потом `SUBTASK`.
4. Запись строк.

Сортировка по id даёт стабильный diff при сравнении файлов (git, diff-утилиты).

**`load()`:**
1. Файла нет → пустой `LoadedState`.
2. Чтение всех строк.
3. Пустые строки пропускаются.
4. Строки с числом колонок < 10 пропускаются с warn-логом.
5. Строки с неизвестным `type` пропускаются с warn-логом.
6. `type` разбирается: `TASK`/`EPIC`/`SUBTASK` → в соответствующую коллекцию.
7. Невалидные числа / даты → `RuntimeException` (не пропускаются — это не
   «мусорная строка», а битые данные).

## Формат CSV

Заголовок:

```
id,type,name,description,statusProgress,parentEpic,childSubtasks,durationInMinutes,startDateTime,endDateTime
```

10 колонок. Плейсхолдеры:
- `SKIP` — поле неприменимо к типу (например, `parentEpic` для `TASK`).
- `EMPTY` — отсутствующее значение (`startDateTime`, `endDateTime`,
  `childSubtasks` у эпика без подзадач).
- `childSubtasks` у эпика — список id через `|` (`9|11`).

Пример:

```
id,type,name,description,statusProgress,parentEpic,childSubtasks,durationInMinutes,startDateTime,endDateTime
1,TASK,review,check PR,NEW,SKIP,SKIP,60,2026-10-15T10:00,2026-10-15T11:00
3,EPIC,release,prepare,IN_PROGRESS,SKIP,5|7,90,2026-10-15T10:00,2026-10-15T11:30
5,SUBTASK,build,mvn package,DONE,3,SKIP,30,2026-10-15T10:00,2026-10-15T10:30
```

## Парсинг CSV

`CommaSeparatedValuesCreateProcess` инкапсулирует `toString` и `fromStrings` для
каждого типа. `setFieldsFromStrings` проверяет `fields.length >= 10` и оборачивает
`NumberFormatException` в `RuntimeException` с понятным сообщением.

## Восстановление связей

`CsvStorage.load()` возвращает «плоский» `LoadedState` (три независимые коллекции).
Связи восстанавливает `TaskManager.initFromStorage()`:
1. Все объекты — в мапы.
2. `epic.addSubtask(subtask)` для каждой подзадачи.
3. Приоритеты — объекты с `startDateTime` в `prioritizedTasks`.
4. `verifyIntegrity()` — удаление orphan-подзадач (ссылающихся на несуществующий
   эпик) с warn-логом.