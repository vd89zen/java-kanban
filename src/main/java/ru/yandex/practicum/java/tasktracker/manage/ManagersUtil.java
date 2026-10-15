package ru.yandex.practicum.java.tasktracker.manage;

import ru.yandex.practicum.java.tasktracker.config.AppConfig;
import ru.yandex.practicum.java.tasktracker.service.TaskViewHistory;
import ru.yandex.practicum.java.tasktracker.service.TaskManager;
import ru.yandex.practicum.java.tasktracker.storage.CsvStorage;

import java.io.File;

public final class ManagersUtil {

    private ManagersUtil() {
    }

    public static TaskManager getDefault() {
        return new TaskManager();
    }

    public static TaskViewHistory getDefaultHistory() {
        return new TaskViewHistory();
    }

    public static TaskManager getFileBacked() {
        return new TaskManager(new CsvStorage(new File(AppConfig.getDataFilePath())));
    }

    public static TaskManager getFileBacked(File file) {
        return new TaskManager(new CsvStorage(file));
    }
}