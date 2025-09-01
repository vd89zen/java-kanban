package ru.yandex.practicum.java.tasktracker.manage;

import ru.yandex.practicum.java.tasktracker.service.FileBackedTaskManager;
import ru.yandex.practicum.java.tasktracker.service.InMemoryHistoryManager;
import ru.yandex.practicum.java.tasktracker.utils.HistoryManager;
import ru.yandex.practicum.java.tasktracker.utils.TaskManager;

import java.io.File;

public final class ManagersUtil {

    private ManagersUtil() {
    }

    public static TaskManager getDefault() {
        return FileBackedTaskManager.loadFromFile(new File("resources/tasks.csv"));
    }

    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }

}
