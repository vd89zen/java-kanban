package ru.yandex.practicum.java.tasktracker.manage;

import ru.yandex.practicum.java.tasktracker.task.AbstractTask;
import ru.yandex.practicum.java.tasktracker.task.Epic;
import ru.yandex.practicum.java.tasktracker.task.Subtask;
import ru.yandex.practicum.java.tasktracker.task.Task;
import java.util.ArrayList;

public interface TaskManager {
    ArrayList<AbstractTask> getHistory();

    int getTotalOfIdNumber();

    //TASK
    ResultOfOperation addTask(Task task);

    Task getTaskForIdNumber(Integer taskIdNumber);

    ResultOfOperation removeTaskForIdNumber(Integer taskIdNumber);

    ResultOfOperation updateTask(Task task);

    ArrayList<Task> getAllTasks();

    ResultOfOperation removeAllTasks();

    //EPIC
    ResultOfOperation addEpic(Epic epic);

    Epic getEpicForIdNumber(Integer epicIdNumber);

    ResultOfOperation removeEpicForIdNumber(Integer epicIdNumber);

    ResultOfOperation updateEpic(Epic epic);

    ArrayList<Subtask> getAllSubtasksFromEpic(Integer epicID);

    ArrayList<Epic> getAllEpics();

    ResultOfOperation removeAllEpics();

    //SUBTASK
    ResultOfOperation addSubtask(Subtask subtask);

    Subtask getSubtaskForIdNumber(Integer subtaskIdNumber);

    ResultOfOperation removeSubtaskForIdNumber(Integer subtaskIdNumber);

    ResultOfOperation updateSubtask(Subtask subtask);

    ArrayList<Subtask> getAllSubtasks();

    ResultOfOperation removeAllSubtasks();

}
