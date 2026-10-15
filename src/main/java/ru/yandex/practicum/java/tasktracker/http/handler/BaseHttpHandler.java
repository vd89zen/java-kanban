package ru.yandex.practicum.java.tasktracker.http.handler;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import ru.yandex.practicum.java.tasktracker.utils.AbstractTask;
import ru.yandex.practicum.java.tasktracker.utils.enums.HttpResponseCode;
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
}