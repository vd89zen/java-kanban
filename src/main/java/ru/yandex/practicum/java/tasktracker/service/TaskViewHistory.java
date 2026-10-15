package ru.yandex.practicum.java.tasktracker.service;

import ru.yandex.practicum.java.tasktracker.utils.AbstractTask;
import ru.yandex.practicum.java.tasktracker.utils.LinkedHashMapCustom;
import ru.yandex.practicum.java.tasktracker.utils.enums.ResultOfOperation;
import java.util.ArrayList;

public class TaskViewHistory {
    private final LinkedHashMapCustom history;

    public TaskViewHistory() {
        history = new LinkedHashMapCustom();
    }


    public synchronized ResultOfOperation addRecord(AbstractTask task) {
        if (task == null) {
            return ResultOfOperation.ERROR_OBJECT_NULL;
        }

        history.addLast(task);

        return ResultOfOperation.SUCCESS;
    }


    public synchronized ResultOfOperation removeRecord(int idNumber) {
        if (idNumber < 0) {
            return ResultOfOperation.ERROR_ID_LESS_ZERO;
        }

        history.removeEntry(idNumber);

        return ResultOfOperation.SUCCESS;
    }


    public synchronized ArrayList<AbstractTask> getHistory() {
        return history.getListAllItem();
    }
}
