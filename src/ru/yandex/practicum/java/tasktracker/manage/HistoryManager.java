package ru.yandex.practicum.java.tasktracker.manage;

import ru.yandex.practicum.java.tasktracker.task.AbstractTask;
import java.util.ArrayList;

public interface HistoryManager {

    ResultOfOperation addRecord(AbstractTask task);

    ResultOfOperation removeRecord(Integer idNumber);

    ArrayList<AbstractTask> getHistory();
}
