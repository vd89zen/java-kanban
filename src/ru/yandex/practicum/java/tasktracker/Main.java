package ru.yandex.practicum.java.tasktracker;

import ru.yandex.practicum.java.tasktracker.manage.ManagersUtil;
import ru.yandex.practicum.java.tasktracker.task.StatusProgress;
import ru.yandex.practicum.java.tasktracker.manage.TaskManager;
import ru.yandex.practicum.java.tasktracker.task.Epic;
import ru.yandex.practicum.java.tasktracker.task.Subtask;

public class Main {

    public static void main(String[] args) {

        TaskManager inMemoryTaskManager = ManagersUtil.getDefault();

        Epic epic = new Epic("name", "description", 1);
        Subtask baseSubtask1 = new Subtask(1,"baseSubtask1 name", "baseSubtask1 description"
                , StatusProgress.NEW, 2);
        Subtask baseSubtask2 = new Subtask(1,"baseSubtask2 name", "baseSubtask2 description"
                , StatusProgress.NEW, 3);
        Subtask baseSubtask3 = new Subtask(1,"baseSubtask3 name", "baseSubtask3 description"
                , StatusProgress.NEW, 4);

        inMemoryTaskManager.addEpic(epic);
        inMemoryTaskManager.addSubtask(baseSubtask1);
        inMemoryTaskManager.addSubtask(baseSubtask2);
        inMemoryTaskManager.addSubtask(baseSubtask3);

        System.out.println("Epic: " + inMemoryTaskManager.getEpicForIdNumber(1));
        System.out.println();
        System.out.println("Subtasks: " + inMemoryTaskManager.getAllSubtasks());
        System.out.println();

        baseSubtask1.setStatusProgress(StatusProgress.DONE);

        System.out.println("Epic: " + inMemoryTaskManager.getEpicForIdNumber(1));
        System.out.println();
        System.out.println("Subtasks: " + inMemoryTaskManager.getAllSubtasks());
        System.out.println();
        System.out.println("Subtasks: " + inMemoryTaskManager.getAllSubtasksFromEpic(1));
        System.out.println();
    }
}
