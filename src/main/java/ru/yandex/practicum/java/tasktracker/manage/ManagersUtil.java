package ru.yandex.practicum.java.tasktracker.manage;

import ru.yandex.practicum.java.tasktracker.config.AppConfig;
import ru.yandex.practicum.java.tasktracker.service.FileBackedTaskManager;
import ru.yandex.practicum.java.tasktracker.service.InMemoryHistoryManager;
import ru.yandex.practicum.java.tasktracker.service.InMemoryTaskManager;
import ru.yandex.practicum.java.tasktracker.service.interfaces.HistoryManager;
import ru.yandex.practicum.java.tasktracker.service.interfaces.TaskManager;

import java.io.File;

public final class ManagersUtil {

    private ManagersUtil() {
    }

    public static TaskManager getDefault() {
        return new InMemoryTaskManager();
    }

    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }

    public static FileBackedTaskManager getFileBackedTaskManager() {
        String dataFilePath = AppConfig.getDataFilePath();
        return FileBackedTaskManager.loadFromFileUsingMethods(new File(dataFilePath));
    }

    public static FileBackedTaskManager getFileBackedTaskManager(File file) {
        return FileBackedTaskManager.loadFromFileUsingMethods(file);
    }
}
