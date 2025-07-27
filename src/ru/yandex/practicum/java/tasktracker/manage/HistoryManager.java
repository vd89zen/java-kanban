package ru.yandex.practicum.java.tasktracker.manage;

import ru.yandex.practicum.java.tasktracker.task.AbstractTask;
import java.util.LinkedList;

public interface HistoryManager {
    byte MAX_SIZE_HISTORY = 10;

    ResultOfOperation addRecord(AbstractTask task);

    LinkedList<AbstractTask> getHistory();
}
