package ru.yandex.practicum.java.tasktracker.manage;

public final class ManagersUtil {

    private ManagersUtil() {
    }

    public static TaskManager getDefault() {
        return new InMemoryTaskManager();
    }

    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }
}
