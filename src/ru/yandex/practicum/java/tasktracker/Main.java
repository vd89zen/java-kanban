package ru.yandex.practicum.java.tasktracker;

import ru.yandex.practicum.java.tasktracker.http.HttpTaskServer;
import ru.yandex.practicum.java.tasktracker.manage.ManagersUtil;
import ru.yandex.practicum.java.tasktracker.service.FileBackedTaskManager;
import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {
        FileBackedTaskManager fileBackedTaskManager = ManagersUtil.getFileBackedTaskManager();
        HttpTaskServer httpTaskServer = new HttpTaskServer(fileBackedTaskManager);
        httpTaskServer.start();
        System.out.println("HTTP-сервер запущен на 8080 порту!");

        //for test
        //DataForUserScenario.fillData(fileBackedTaskManager);
    }
}
