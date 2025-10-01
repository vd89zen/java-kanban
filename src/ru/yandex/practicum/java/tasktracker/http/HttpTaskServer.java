package ru.yandex.practicum.java.tasktracker.http;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Objects;
import com.sun.net.httpserver.HttpServer;
import ru.yandex.practicum.java.tasktracker.http.handler.*;
import ru.yandex.practicum.java.tasktracker.manage.ManagersUtil;
import ru.yandex.practicum.java.tasktracker.service.interfaces.TaskManager;

public class HttpTaskServer {
    private static final int PORT = 8080;
    private static final int BACKLOG = 0;
    private final HttpServer httpServer;
    private final TaskManager taskManager;

    public HttpTaskServer() throws IOException {
        httpServer = HttpServer.create(new InetSocketAddress(PORT), BACKLOG);
        this.taskManager = ManagersUtil.getDefault();
        createContext();
    }

    public HttpTaskServer(TaskManager taskManager) throws IOException {
        this.taskManager = Objects.requireNonNull(taskManager, "'taskManager' can't be null");
        httpServer = HttpServer.create(new InetSocketAddress(PORT), BACKLOG);
        createContext();
    }

    public void start() {
        httpServer.start();
    }

    public void stop() {
        httpServer.stop(1);
    }

    private void createContext() {
        httpServer.createContext("/tasks", new TasksHandler(taskManager));
        httpServer.createContext("/subtasks", new SubtasksHandler(taskManager));
        httpServer.createContext("/epics", new EpicsHandler(taskManager));
        httpServer.createContext("/history", new HistoryHandler(taskManager));
        httpServer.createContext("/prioritized", new PrioritizedHandler(taskManager));
    }

}
