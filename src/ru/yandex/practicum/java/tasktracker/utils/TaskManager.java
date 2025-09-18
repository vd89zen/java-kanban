package ru.yandex.practicum.java.tasktracker.utils;

import ru.yandex.practicum.java.tasktracker.task.Epic;
import ru.yandex.practicum.java.tasktracker.task.Subtask;
import ru.yandex.practicum.java.tasktracker.task.Task;
import java.util.ArrayList;
import java.util.Optional;
import java.util.TreeSet;

public interface TaskManager {
    TreeSet<AbstractTask> getPrioritizedTasks();

    boolean isTimeIntersectBoth(AbstractTask taskOne, AbstractTask taskTwo);

    boolean isTimeIntersectWithOthers(AbstractTask task);

    ArrayList<AbstractTask> getHistory();

    int getTotalOfIdInWork();

    //TASK
    ResultOfOperation addTask(Task task);

    Optional<Task> getTaskForIdNumber(int taskIdNumber);

    ResultOfOperation removeTaskForIdNumber(int taskIdNumber);

    ResultOfOperation updateTask(Task task);

    Optional<ArrayList<Task>> getAllTasks();

    ResultOfOperation removeAllTasks();

    //EPIC
    ResultOfOperation addEpic(Epic epic);

    Optional<Epic> getEpicForIdNumber(int epicIdNumber);

    ResultOfOperation removeEpicForIdNumber(int epicIdNumber);

    ResultOfOperation updateEpicName(int epicIdNumber, String newEpicName);

    ResultOfOperation updateEpicDescription(int epicIdNumber, String newEpicDescription);

    Optional<ArrayList<Subtask>> getAllSubtasksFromEpic(int epicID);

    Optional<ArrayList<Epic>> getAllEpics();

    ResultOfOperation removeAllEpics();

    //SUBTASK
    ResultOfOperation addSubtask(Subtask subtask);

    Optional<Subtask> getSubtaskForIdNumber(int subtaskIdNumber);

    ResultOfOperation removeSubtaskForIdNumber(int subtaskIdNumber);

    ResultOfOperation updateSubtask(Subtask subtask);

    Optional<ArrayList<Subtask>> getAllSubtasks();

    ResultOfOperation removeAllSubtasks();

}
