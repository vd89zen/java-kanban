package ru.yandex.practicum.java.tasktracker.manage;

import ru.yandex.practicum.java.tasktracker.service.InMemoryHistoryManager;
import ru.yandex.practicum.java.tasktracker.service.InMemoryTaskManager;
import ru.yandex.practicum.java.tasktracker.utils.HistoryManager;
import ru.yandex.practicum.java.tasktracker.utils.TaskManager;

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
