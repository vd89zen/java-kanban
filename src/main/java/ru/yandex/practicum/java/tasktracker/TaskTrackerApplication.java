package ru.yandex.practicum.java.tasktracker;

import ru.yandex.practicum.java.tasktracker.http.HttpTaskServer;
import ru.yandex.practicum.java.tasktracker.manage.ManagersUtil;
import ru.yandex.practicum.java.tasktracker.service.TaskManager;

import java.io.IOException;

public class TaskTrackerApplication {

    public static void main(String[] args) throws IOException {
        TaskManager taskManager = ManagersUtil.getFileBacked();
        HttpTaskServer httpTaskServer = new HttpTaskServer(taskManager);
        httpTaskServer.start();
    }
}