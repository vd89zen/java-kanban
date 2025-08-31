package ru.yandex.practicum.java.tasktracker.manage;

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
