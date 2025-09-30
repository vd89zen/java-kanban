package ru.yandex.practicum.java.tasktracker.utils.interfaces;

import ru.yandex.practicum.java.tasktracker.utils.AbstractTask;
import ru.yandex.practicum.java.tasktracker.utils.enums.ResultOfOperation;

import java.util.ArrayList;

public interface HistoryManager {

    ResultOfOperation addRecord(AbstractTask task);

    ResultOfOperation removeRecord(int idNumber);

    ArrayList<AbstractTask> getHistory();
}
