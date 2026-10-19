# Обработка ошибок

## Формат ответа

`TextForGson` — единая структура для ошибок и подтверждений мутаций:

```json
{
  "request": "GET /tasks/abc",
  "detail": "The ID number format is incorrect: it must be a positive number."
}
```

Все поля nullable: `request`, `done`, `taskIdNumber`, `taskType`, `detail`.

## `Content-Type`

Все ответы (успешные и ошибочные) — `application/json;charset=utf-8`.
Константа `CONTENT_TYPE_JSON` в `BaseHttpHandler`.

## Коды HTTP

| Код | Когда |
|---|---|
| `200 OK` | `GET`, `POST` (обновление) |
| `201 Created` | `POST` (создание), с заголовком `Location` |
| `204 No Content` | `DELETE` |
| `400 Bad Request` | Валидация, невалидный JSON, неизвестный путь, невалидные query-параметры |
| `404 Not Found` | Объект по id не найден |
| `405 Method Not Allowed` | Метод не поддерживается, с заголовком `Allow` |
| `409 Conflict` | Пересечение интервалов |
| `415 Unsupported Media Type` | Неверный `Content-Type` |
| `500 Internal Server Error` | `ManagerSaveException` (ошибка записи CSV) |

## Хелперы `BaseHttpHandler`

| Метод | Код |
|---|---|
| `sendText` | 200 |
| `sendCreated` | 201 |
| `sendNoContent` | 204 |
| `sendBadRequest` | 400 |
| `sendNotFound` | 404 |
| `sendMethodNotAllowed` | 405 + `Allow` |
| `sendConflict` | 409 |
| `sendUnsupportedMediaType` | 415 |
| `sendError` | 500 |

## Соответствие `ResultOfOperation` → HTTP

В хендлерах `POST`:

| `ResultOfOperation` | HTTP |
|---|---|
| `SUCCESS` | 201 (создание) / 200 (обновление) |
| `ERROR_INTERSECT_TIME` | 409 |
| `ERROR_OBJECT_NULL` | 400 |
| `ERROR_OBJECT_FIELDS_NULL` | 400 |
| `ERROR_ID_LESS_ZERO` | 400 |
| `ERROR_MISMATCH_PARENT_ID` | 400 |
| `ERROR_OBJECT_ALREADY_EXISTS` | (не должно случаться на уровне API) |

## Исключения

| Исключение | Обработка |
|---|---|
| `BadRequestException` | `400` через `sendBadRequest` |
| `NotFoundException` | `404` через `sendNotFound` |
| `ManagerSaveException` | `500` через `sendError` |

Исключения ловятся в каждом хендлере в общем `try/catch` вокруг всей обработки
запроса.

## Логирование

- `warn` — пропущенные строки CSV, orphan-подзадачи при `verifyIntegrity`.
- `error` — `ManagerSaveException` и прочие необработанные исключения.
- `info` — старт/остановка сервера.