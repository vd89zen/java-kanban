package ru.yandex.practicum.java.tasktracker.http.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.yandex.practicum.java.tasktracker.service.TaskManager;
import ru.yandex.practicum.java.tasktracker.utils.TextForGson;

import java.io.IOException;
import java.util.Map;

public class HealthHandler extends BaseHttpHandler implements HttpHandler {

    public HealthHandler(TaskManager taskManager) {
        super(taskManager);
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        TextForGson textForGson = new TextForGson();
        String requestMethod = httpExchange.getRequestMethod();
        String[] splitPath = httpExchange.getRequestURI().getPath().split("/");

        if (requestMethod.equals("GET") == false) {
            textForGson.setDetail("Method '" + requestMethod + "' is not allowed. Allowed: GET");
            sendMethodNotAllowed(httpExchange, gson.toJson(textForGson), "GET");
            return;
        }

        if (splitPath.length != 2) {
            textForGson.setDetail("For GET path must be '/health'");
            sendBadRequest(httpExchange, gson.toJson(textForGson));
            return;
        }

        Map<String, String> body = Map.of("status", "UP");
        sendText(httpExchange, gson.toJson(body));
    }
}