package ru.yandex.practicum.java.tasktracker.manage;

import ru.yandex.practicum.java.tasktracker.task.AbstractTask;
import java.util.LinkedList;

public class InMemoryHistoryManager implements HistoryManager {
    private final LinkedList<AbstractTask> history;

    public InMemoryHistoryManager() {
        history = new LinkedList<>();
    }

    @Override
    public ResultOfOperation addRecord(AbstractTask task) {
        if (task == null) {
            return ResultOfOperation.ERROR_OBJECT_NULL;
        }

        history.add(task);

        if (history.size() > MAX_SIZE_HISTORY) {
            history.removeFirst();
        }

        return ResultOfOperation.SUCCESS;
    }

    @Override
    public LinkedList<AbstractTask> getHistory() {
        return history;
    }
}
