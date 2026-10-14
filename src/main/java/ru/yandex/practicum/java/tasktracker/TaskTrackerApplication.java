package ru.yandex.practicum.java.tasktracker;

import ru.yandex.practicum.java.tasktracker.http.HttpTaskServer;
import ru.yandex.practicum.java.tasktracker.manage.ManagersUtil;
import ru.yandex.practicum.java.tasktracker.service.FileBackedTaskManager;
import java.io.IOException;

public class TaskTrackerApplication {

    public static void main(String[] args) throws IOException {
        FileBackedTaskManager fileBackedTaskManager = ManagersUtil.getFileBackedTaskManager();
        HttpTaskServer httpTaskServer = new HttpTaskServer(fileBackedTaskManager);
        httpTaskServer.start();
    }
}
