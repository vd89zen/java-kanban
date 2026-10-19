package ru.yandex.practicum.java.tasktracker.http;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
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
    private static final int THREAD_POOL_SIZE = 8;
    private static final long SHUTDOWN_TIMEOUT_SECONDS = 1;

    private final HttpServer httpServer;
    private final TaskManager taskManager;
    private final int port;
    private final ExecutorService executor;

    public HttpTaskServer() throws IOException {
        this(ManagersUtil.getDefault(), AppConfig.getServerPort());
    }

    public HttpTaskServer(TaskManager taskManager) throws IOException {
        this(taskManager, AppConfig.getServerPort());
    }

    public HttpTaskServer(TaskManager taskManager, int port) throws IOException {
        this.taskManager = Objects.requireNonNull(taskManager, "'taskManager' can't be null");
        this.executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        httpServer = HttpServer.create(new InetSocketAddress(port), BACKLOG);
        httpServer.setExecutor(executor);
        this.port = httpServer.getAddress().getPort();
        createContext();
    }

    public void start() {
        httpServer.start();
        log.info("HTTP server started on port {}", port);
    }

    public void stop() {
        httpServer.stop(1);
        executor.shutdownNow();
        try {
            if (executor.awaitTermination(SHUTDOWN_TIMEOUT_SECONDS, TimeUnit.SECONDS) == false) {
                log.warn("Thread pool did not terminate within {} second(s)", SHUTDOWN_TIMEOUT_SECONDS);
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            log.warn("Interrupted while waiting for thread pool shutdown");
        }
        log.info("HTTP server stopped");
    }

    public int getPort() {
        return port;
    }

    private void createContext() {
        httpServer.createContext("/tasks", new TasksHandler(taskManager));
        httpServer.createContext("/subtasks", new SubtasksHandler(taskManager));
        httpServer.createContext("/epics", new EpicsHandler(taskManager));
        httpServer.createContext("/history", new HistoryHandler(taskManager));
        httpServer.createContext("/prioritized", new PrioritizedHandler(taskManager));
        httpServer.createContext("/health", new HealthHandler(taskManager));
        httpServer.createContext("/version", new VersionHandler(taskManager));
    }
}