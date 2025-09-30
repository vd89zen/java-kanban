package ru.yandex.practicum.java.tasktracker.http.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.yandex.practicum.java.tasktracker.utils.AbstractTask;
import ru.yandex.practicum.java.tasktracker.utils.TextForGson;
import ru.yandex.practicum.java.tasktracker.utils.interfaces.TaskManager;
import java.io.IOException;
import java.util.ArrayList;

public class PrioritizedHandler extends BaseHttpHandler implements HttpHandler {

    public PrioritizedHandler(TaskManager taskManager) {
        super(taskManager);
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        TextForGson textForGson = new TextForGson();
        String requestMethod = httpExchange.getRequestMethod();
        String[] splitPath = httpExchange.getRequestURI().getPath().split("/");

        if (requestMethod.equals("GET")) {
            if (splitPath.length == 2) {
                ArrayList<AbstractTask> prioritized = taskManager.getPrioritizedTasks();
                sendText(httpExchange, gson.toJson(prioritized));
            } else {
                textForGson.setRequest(requestMethod + " " + httpExchange.getRequestURI());
                textForGson.setDetail("Path must be '/prioritized'");
                sendBadRequest(httpExchange, gson.toJson(textForGson));
            }
        } else {
            textForGson.setRequest(requestMethod + " " + httpExchange.getRequestURI());
            textForGson.setDetail("Method must be GET");
            sendBadRequest(httpExchange, gson.toJson(textForGson));
        }
    }
}
