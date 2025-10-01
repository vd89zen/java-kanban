package ru.yandex.practicum.java.tasktracker.http.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.yandex.practicum.java.tasktracker.utils.TextForGson;
import ru.yandex.practicum.java.tasktracker.service.interfaces.TaskManager;
import java.io.IOException;

public class HistoryHandler extends BaseHttpHandler implements HttpHandler {

    public HistoryHandler(TaskManager taskManager) {
        super(taskManager);
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        TextForGson textForGson = new TextForGson();
        String requestMethod = httpExchange.getRequestMethod();
        String[] splitPath = httpExchange.getRequestURI().getPath().split("/");

        if (requestMethod.equals("GET")) {
            if (splitPath.length == 2) {
                sendText(httpExchange, gson.toJson(taskManager.getHistory()));
                return;
            } else {
                textForGson.setDetail("Path must be '/history'");
            }
        } else {
            textForGson.setDetail("Method must be GET");
        }

        textForGson.setRequest(requestMethod + " " + httpExchange.getRequestURI());
        sendBadRequest(httpExchange, gson.toJson(textForGson));
    }
}
