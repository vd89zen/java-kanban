package ru.yandex.practicum.java.tasktracker.utils;

import java.util.ArrayList;

public interface HistoryManager {

    ResultOfOperation addRecord(AbstractTask task);

    ResultOfOperation removeRecord(int idNumber);

    ArrayList<AbstractTask> getHistory();
}
