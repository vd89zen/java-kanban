package ru.yandex.practicum.java.tasktracker.service;

import ru.yandex.practicum.java.tasktracker.utils.AbstractTask;
import ru.yandex.practicum.java.tasktracker.utils.HistoryManager;
import ru.yandex.practicum.java.tasktracker.utils.LinkedHashMapCustom;
import ru.yandex.practicum.java.tasktracker.utils.ResultOfOperation;

import java.util.ArrayList;

public class InMemoryHistoryManager implements HistoryManager {
    private final LinkedHashMapCustom history;

    public InMemoryHistoryManager() {
        history = new LinkedHashMapCustom();
    }

    @Override
    public ResultOfOperation addRecord(AbstractTask task) {
        if (task == null) {
            return ResultOfOperation.ERROR_OBJECT_NULL;
        }

        history.addLast(task);

        return ResultOfOperation.SUCCESS;
    }

    @Override
    public ResultOfOperation removeRecord(Integer idNumber) {
        if (idNumber == null) {
            return ResultOfOperation.ERROR_OBJECT_NULL;
        } else if (idNumber < 0) {
            return ResultOfOperation.ERROR_ID_LESS_ZERO;
        }

        history.removeEntry(idNumber);

        return ResultOfOperation.SUCCESS;
    }

    @Override
    public ArrayList<AbstractTask> getHistory() {
        return history.getListAllItem();
    }
}
