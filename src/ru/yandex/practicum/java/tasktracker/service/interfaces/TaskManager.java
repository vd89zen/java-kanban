package ru.yandex.practicum.java.tasktracker.service.interfaces;

import ru.yandex.practicum.java.tasktracker.task.Epic;
import ru.yandex.practicum.java.tasktracker.task.Subtask;
import ru.yandex.practicum.java.tasktracker.task.Task;
import ru.yandex.practicum.java.tasktracker.utils.AbstractTask;
import ru.yandex.practicum.java.tasktracker.utils.enums.ResultOfOperation;
import java.util.ArrayList;

public interface TaskManager {
    ArrayList<AbstractTask> getPrioritizedTasks();

    boolean isTimeIntersectBoth(AbstractTask taskOne, AbstractTask taskTwo);

    boolean isTimeIntersectWithOthers(AbstractTask task);

    ArrayList<AbstractTask> getHistory();

    int getTotalOfIdInWork();

    //TASK
    ResultOfOperation addTask(Task task);

    Task getTaskByIdNumber(int taskIdNumber);

    ResultOfOperation removeTaskByIdNumber(int taskIdNumber);

    ResultOfOperation updateTask(Task task);

    ArrayList<Task> getAllTasks();

    ResultOfOperation removeAllTasks();

    //EPIC
    ResultOfOperation addEpic(Epic epic);

    Epic getEpicByIdNumber(int epicIdNumber);

    ResultOfOperation removeEpicByIdNumber(int epicIdNumber);

    ResultOfOperation updateEpicName(int epicIdNumber, String newEpicName);

    ResultOfOperation updateEpicDescription(int epicIdNumber, String newEpicDescription);

    ArrayList<Subtask> getAllSubtasksFromEpic(int epicID);

    ArrayList<Epic> getAllEpics();

    ResultOfOperation removeAllEpics();

    //SUBTASK
    ResultOfOperation addSubtask(Subtask subtask);

    Subtask getSubtaskByIdNumber(int subtaskIdNumber);

    ResultOfOperation removeSubtaskByIdNumber(int subtaskIdNumber);

    ResultOfOperation updateSubtask(Subtask subtask);

    ArrayList<Subtask> getAllSubtasks();

    ResultOfOperation removeAllSubtasks();

}
