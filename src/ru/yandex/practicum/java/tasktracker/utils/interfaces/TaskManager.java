package ru.yandex.practicum.java.tasktracker.utils.interfaces;

import ru.yandex.practicum.java.tasktracker.task.Epic;
import ru.yandex.practicum.java.tasktracker.task.Subtask;
import ru.yandex.practicum.java.tasktracker.task.Task;
import ru.yandex.practicum.java.tasktracker.utils.AbstractTask;
import ru.yandex.practicum.java.tasktracker.utils.enums.ResultOfOperation;
import java.util.ArrayList;
import java.util.Optional;

public interface TaskManager {
    ArrayList<AbstractTask> getPrioritizedTasks();

    boolean isTimeIntersectBoth(AbstractTask taskOne, AbstractTask taskTwo);

    boolean isTimeIntersectWithOthers(AbstractTask task);

    ArrayList<AbstractTask> getHistory();

    int getTotalOfIdInWork();

    //TASK
    ResultOfOperation addTask(Task task);

    Optional<Task> getTaskByIdNumber(int taskIdNumber);

    ResultOfOperation removeTaskByIdNumber(int taskIdNumber);

    ResultOfOperation updateTask(Task task);

    Optional<ArrayList<Task>> getAllTasks();

    ResultOfOperation removeAllTasks();

    //EPIC
    ResultOfOperation addEpic(Epic epic);

    Optional<Epic> getEpicByIdNumber(int epicIdNumber);

    ResultOfOperation removeEpicByIdNumber(int epicIdNumber);

    ResultOfOperation updateEpicName(int epicIdNumber, String newEpicName);

    ResultOfOperation updateEpicDescription(int epicIdNumber, String newEpicDescription);

    Optional<ArrayList<Subtask>> getAllSubtasksFromEpic(int epicID);

    Optional<ArrayList<Epic>> getAllEpics();

    ResultOfOperation removeAllEpics();

    //SUBTASK
    ResultOfOperation addSubtask(Subtask subtask);

    Optional<Subtask> getSubtaskByIdNumber(int subtaskIdNumber);

    ResultOfOperation removeSubtaskByIdNumber(int subtaskIdNumber);

    ResultOfOperation updateSubtask(Subtask subtask);

    Optional<ArrayList<Subtask>> getAllSubtasks();

    ResultOfOperation removeAllSubtasks();

}
