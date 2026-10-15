package ru.yandex.practicum.java.tasktracker.http;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Objects;
import com.sun.net.httpserver.HttpServer;
import ru.yandex.practicum.java.tasktracker.config.AppConfig;
import ru.yandex.practicum.java.tasktracker.http.handler.*;
import ru.yandex.practicum.java.tasktracker.manage.ManagersUtil;
import ru.yandex.practicum.java.tasktracker.service.TaskManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpTaskServer {
    private static final Logger log = LoggerFactory.getLogger(HttpTaskServer.class);
    private static final int BACKLOG = 0;
    private final HttpServer httpServer;
    private final TaskManager taskManager;
    private final int port;

    public HttpTaskServer() throws IOException {
        this.port = AppConfig.getServerPort();
        httpServer = HttpServer.create(new InetSocketAddress(port), BACKLOG);
        this.taskManager = ManagersUtil.getDefault();
        createContext();
    }

    public HttpTaskServer(TaskManager taskManager) throws IOException {
        this.port = AppConfig.getServerPort();
        this.taskManager = Objects.requireNonNull(taskManager, "'taskManager' can't be null");
        httpServer = HttpServer.create(new InetSocketAddress(port), BACKLOG);
        createContext();
    }

    public void start() {
        httpServer.start();
        log.info("HTTP server started on port {}", port);
    }

    public void stop() {
        httpServer.stop(1);
        log.info("HTTP server stopped");
    }

    private void createContext() {
        httpServer.createContext("/tasks", new TasksHandler(taskManager));
        httpServer.createContext("/subtasks", new SubtasksHandler(taskManager));
        httpServer.createContext("/epics", new EpicsHandler(taskManager));
        httpServer.createContext("/history", new HistoryHandler(taskManager));
        httpServer.createContext("/prioritized", new PrioritizedHandler(taskManager));
    }

}
