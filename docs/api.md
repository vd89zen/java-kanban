# API

Базовый URL: `http://localhost:8080`. Все ответы — JSON в UTF-8.

## Общие правила

- **Создание:** `POST` с `id <= 0` (или без `id`) → `201 Created` + `Location: /{resource}/{id}`.
- **Обновление:** `POST` с `id > 0` → `200 OK` (в `TasksHandler`, `SubtasksHandler`;
  `EpicsHandler` запрещает обновление → `400`).
- **Удаление:** `DELETE` → `204 No Content`.
- **Неверный `Content-Type`** (не `application/json`) → `415 Unsupported Media Type`.
- **Неподдерживаемый метод** → `405 Method Not Allowed` + заголовок `Allow`.
- **Пересечение интервалов** → `409 Conflict`.
- **Пустой список** → `200 OK` + `[]`.

## Задачи

| Метод | Путь | Успех |
|---|---|---|
| `GET` | `/tasks` | `200 OK` + `[Task]` |
| `GET` | `/tasks/{id}` | `200 OK` + `Task` |
| `POST` | `/tasks` | `201 Created` (создание) / `200 OK` (обновление) |
| `DELETE` | `/tasks/{id}` | `204 No Content` |

**Создание:**

```bash
curl -X POST http://localhost:8080/tasks \
  -H "Content-Type: application/json" \
  -d '{
        "name": "Сделать ревью",
        "description": "Проверить PR",
        "statusProgress": "NEW",
        "durationInMinutes": 60,
        "startDateTime": "10:00, 15:10:2026"
      }'
```

`id = 0` (или отсутствие) — создание, `id > 0` — обновление.

## Эпики

| Метод | Путь | Успех |
|---|---|---|
| `GET` | `/epics` | `200 OK` + `[Epic]` |
| `GET` | `/epics/{id}` | `200 OK` + `Epic` |
| `GET` | `/epics/{id}/subtasks` | `200 OK` + `[Subtask]` |
| `POST` | `/epics` | `201 Created` |
| `DELETE` | `/epics/{id}` | `204 No Content` |

Обновление эпика через API запрещено (`400`) — статус, длительность и границы
пересчитываются автоматически по подзадачам.

## Подзадачи

| Метод | Путь | Успех |
|---|---|---|
| `GET` | `/subtasks` | `200 OK` + `[Subtask]` |
| `GET` | `/subtasks/{id}` | `200 OK` + `Subtask` |
| `POST` | `/subtasks` | `201 Created` / `200 OK` |
| `DELETE` | `/subtasks/{id}` | `204 No Content` |

`parentEpicIdNumber` обязателен и должен ссылаться на существующий эпик.

## История и приоритеты

| Метод | Путь | Успех |
|---|---|---|
| `GET` | `/history` | `200 OK` + `[AbstractTask]` |
| `GET` | `/prioritized` | `200 OK` + `[AbstractTask]` |

`/history` — порядок последнего просмотра (от старых к новым). `/prioritized` —
сортировка по `startDateTime`, задачи без времени не попадают.

## Служебные

| Метод | Путь | Успех |
|---|---|---|
| `GET` | `/health` | `200 OK` + `{"status":"UP"}` |
| `GET` | `/version` | `200 OK` + `{"version":"1.0.0-SNAPSHOT"}` |

## Пагинация

`GET /tasks`, `/subtasks`, `/epics`:

| Параметр | Описание | Дефолт |
|---|---|---|
| `from` | Смещение (≥ 0) | `0` |
| `size` | Размер страницы (1..100) | `20` |
| `status` | Фильтр по статусу (`NEW`, `IN_PROGRESS`, `DONE`) | без фильтра |
| `sortBy` | `id` или `startDateTime` | `id` |

Невалидные значения → `400` с сообщением про конкретный параметр. Неизвестные
параметры игнорируются. Не тримим — строгий контракт.

```bash
curl 'http://localhost:8080/tasks?from=0&size=10&status=NEW&sortBy=startDateTime'
```

## Формат времени

`startDateTime` — строка `HH:mm, dd:MM:yyyy` (например, `"10:00, 15:10:2026"`).
Отсутствие — `"EMPTY"`. Задаётся кастомным `LocalDateTimeTypeAdapter`.

`durationInMinutes` — число минут (`DurationTypeAdapter`).

## Формат ответа на мутацию

**Успех:**

```json
{
  "done": "add",
  "taskIdNumber": 5,
  "taskType": "task"
}
```

`done`: `add`, `update`, `delete`. `taskType`: `task`, `subtask`, `epic`.

**Ошибка:**

```json
{
  "request": "GET /tasks/abc",
  "detail": "The ID number format is incorrect: it must be a positive number."
}
```