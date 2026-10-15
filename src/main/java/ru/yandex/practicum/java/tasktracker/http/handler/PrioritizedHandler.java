package ru.yandex.practicum.java.tasktracker.http.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.yandex.practicum.java.tasktracker.utils.TextForGson;
import ru.yandex.practicum.java.tasktracker.service.TaskManager;
import java.io.IOException;

public class PrioritizedHandler extends BaseHttpHandler implements HttpHandler {

    public PrioritizedHandler(TaskManager taskManager) {
        super(taskManager);
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        TextForGson textForGson = new TextForGson();
        String requestMethod = httpExchange.getRequestMethod();
        String[] splitPath = httpExchange.getRequestURI().getPath().split("/");
        textForGson.setRequest(requestMethod + " " + httpExchange.getRequestURI());

        if (requestMethod.equals("GET")) {
            if (splitPath.length == 2) {
                sendText(httpExchange, gson.toJson(taskManager.getPrioritizedTasks()));
                return;
            } else {
                textForGson.setDetail("Path must be '/prioritized'");
                sendBadRequest(httpExchange, gson.toJson(textForGson));
                return;
            }
        }

        textForGson.setDetail("Method '" + requestMethod + "' is not allowed. Allowed: GET");
        sendMethodNotAllowed(httpExchange, gson.toJson(textForGson), "GET");
    }
}
