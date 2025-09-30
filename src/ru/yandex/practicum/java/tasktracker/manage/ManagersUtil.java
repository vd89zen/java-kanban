package ru.yandex.practicum.java.tasktracker.manage;

import ru.yandex.practicum.java.tasktracker.service.FileBackedTaskManager;
import ru.yandex.practicum.java.tasktracker.service.InMemoryHistoryManager;
import ru.yandex.practicum.java.tasktracker.service.InMemoryTaskManager;
import ru.yandex.practicum.java.tasktracker.utils.interfaces.HistoryManager;
import ru.yandex.practicum.java.tasktracker.utils.interfaces.TaskManager;

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
        return FileBackedTaskManager.loadFromFileUsingMethods(new File("resources/tasks.csv"));
    }
}
