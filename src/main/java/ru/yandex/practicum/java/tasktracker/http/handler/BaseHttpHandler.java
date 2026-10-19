package ru.yandex.practicum.java.tasktracker.http.handler;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import ru.yandex.practicum.java.tasktracker.exceptions.BadRequestException;
import ru.yandex.practicum.java.tasktracker.utils.AbstractTask;
import ru.yandex.practicum.java.tasktracker.utils.enums.HttpResponseCode;
import ru.yandex.practicum.java.tasktracker.utils.enums.StatusProgress;
import ru.yandex.practicum.java.tasktracker.utils.gson.AbstractTaskDeserializer;
import ru.yandex.practicum.java.tasktracker.utils.gson.DurationTypeAdapter;
import ru.yandex.practicum.java.tasktracker.utils.gson.LocalDateTimeTypeAdapter;
import ru.yandex.practicum.java.tasktracker.service.TaskManager;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class BaseHttpHandler {
    protected static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
    private static final String CONTENT_TYPE_JSON = "application/json;charset=utf-8";

    protected final TaskManager taskManager;
    protected Gson gson;

    public BaseHttpHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
        gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeTypeAdapter())
                .registerTypeAdapter(Duration.class, new DurationTypeAdapter())
                .registerTypeAdapter(AbstractTask.class, new AbstractTaskDeserializer())
                .create();
    }

    protected void sendText(HttpExchange httpExchange, String responseText) throws IOException {
        httpExchange.getResponseHeaders().add("Content-Type", CONTENT_TYPE_JSON);
        writeResponse(httpExchange, responseText, HttpResponseCode.OK.getCode());
    }

    protected void sendCreated(HttpExchange httpExchange, String responseText) throws IOException {
        httpExchange.getResponseHeaders().add("Content-Type", CONTENT_TYPE_JSON);
        writeResponse(httpExchange, responseText, HttpResponseCode.CREATED.getCode());
    }

    protected void sendNoContent(HttpExchange httpExchange) throws IOException {
        httpExchange.sendResponseHeaders(HttpResponseCode.NO_CONTENT.getCode(), -1);
        httpExchange.close();
    }

    protected void sendNotFound(HttpExchange httpExchange, String responseText) throws IOException {
        httpExchange.getResponseHeaders().add("Content-Type", CONTENT_TYPE_JSON);
        writeResponse(httpExchange, responseText, HttpResponseCode.NOT_FOUND.getCode());
    }

    protected void sendBadRequest(HttpExchange httpExchange, String responseText) throws IOException {
        httpExchange.getResponseHeaders().add("Content-Type", CONTENT_TYPE_JSON);
        writeResponse(httpExchange, responseText, HttpResponseCode.BAD_REQUEST.getCode());
    }

    protected void sendConflict(HttpExchange httpExchange, String responseText) throws IOException {
        httpExchange.getResponseHeaders().add("Content-Type", CONTENT_TYPE_JSON);
        writeResponse(httpExchange, responseText, HttpResponseCode.CONFLICT.getCode());
    }

    protected void sendMethodNotAllowed(HttpExchange httpExchange, String responseText,
                                        String allowedMethods) throws IOException {
        httpExchange.getResponseHeaders().add("Content-Type", CONTENT_TYPE_JSON);
        httpExchange.getResponseHeaders().add("Allow", allowedMethods);
        writeResponse(httpExchange, responseText, HttpResponseCode.METHOD_NOT_ALLOWED.getCode());
    }

    protected void sendUnsupportedMediaType(HttpExchange httpExchange, String responseText) throws IOException {
        httpExchange.getResponseHeaders().add("Content-Type", CONTENT_TYPE_JSON);
        writeResponse(httpExchange, responseText, HttpResponseCode.UNSUPPORTED_MEDIA_TYPE.getCode());
    }

    protected void sendError(HttpExchange httpExchange, String responseText) throws IOException {
        httpExchange.getResponseHeaders().add("Content-Type", CONTENT_TYPE_JSON);
        writeResponse(httpExchange, responseText, HttpResponseCode.INTERNAL_SERVER_ERROR.getCode());
    }

    protected void writeResponse(HttpExchange httpExchange, String responseText, int responseCode) throws IOException {
        byte[] response = responseText.getBytes(DEFAULT_CHARSET);
        try (OutputStream os = httpExchange.getResponseBody()) {
            httpExchange.sendResponseHeaders(responseCode, response.length);
            os.write(response);
        }
        httpExchange.close();
    }

    protected QueryParams parseQueryParams(HttpExchange httpExchange) {
        String rawQuery = httpExchange.getRequestURI().getQuery();
        if (rawQuery == null || rawQuery.isBlank()) {
            return QueryParams.defaults();
        }

        Map<String, String> params = new HashMap<>();
        for (String pair : rawQuery.split("&")) {
            String[] parts = pair.split("=", 2);
            if (parts.length == 2) {
                params.put(parts[0], parts[1]);
            }
        }

        int from = parseIntParam(params, "from", 0);
        int size = parseIntParam(params, "size", 20);

        if (from < 0) {
            throw new BadRequestException("'from' must be >= 0");
        }
        if (size < 1 || size > 100) {
            throw new BadRequestException("'size' must be between 1 and 100");
        }

        StatusProgress status = parseStatus(params.get("status"));
        String sortBy = params.getOrDefault("sortBy", "id");
        if (!"id".equals(sortBy) && !"startDateTime".equals(sortBy)) {
            throw new BadRequestException("'sortBy' must be 'id' or 'startDateTime'");
        }

        return new QueryParams(from, size, status, sortBy);
    }

    protected Comparator<AbstractTask> comparatorFor(String sortBy) {
        if ("startDateTime".equals(sortBy)) {
            return Comparator.comparing(
                    task -> task.getStartDateTime().orElse(LocalDateTime.MAX));
        }
        return Comparator.comparingInt(AbstractTask::getIdNumber);
    }

    private int parseIntParam(Map<String, String> params, String name, int defaultValue) {
        String value = params.get(name);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            throw new BadRequestException("'" + name + "' must be an integer, got: '" + value + "'");
        }
    }

    private StatusProgress parseStatus(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return StatusProgress.valueOf(value);
        } catch (IllegalArgumentException exception) {
            throw new BadRequestException("'status' must be one of: NEW, IN_PROGRESS, DONE; got: '" + value + "'");
        }
    }
}