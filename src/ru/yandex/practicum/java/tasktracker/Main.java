package ru.yandex.practicum.java.tasktracker;

import ru.yandex.practicum.java.tasktracker.manage.ManagersUtil;
import ru.yandex.practicum.java.tasktracker.manage.TaskManager;
import ru.yandex.practicum.java.tasktracker.task.Epic;
import ru.yandex.practicum.java.tasktracker.task.StatusProgress;
import ru.yandex.practicum.java.tasktracker.task.Subtask;
import ru.yandex.practicum.java.tasktracker.task.Task;

public class Main {
    private static final String DESCRIPTION = "description";
    private static final String TASK_1_NAME = "Task1";
    private static final Integer TASK_1_ID = 1;
    private static final String TASK_2_NAME = "Task2";
    private static final Integer TASK_2_ID = 3;
    private static final String EPIC_1_WITH_SUBTASK_NAME = "Epic1 with subtask";
    private static final Integer EPIC_1_WITH_SUBTASK_ID = 5;
    private static final String EPIC_2_WITHOUT_SUBTASK_NAME = "Epic2 without subtask";
    private static final Integer EPIC_2_WITHOUT_SUBTASK_ID = 7;
    private static final String SUBTASK_1_NAME = "Subtask1";
    private static final Integer SUBTASK_1_ID = 9;
    private static final String SUBTASK_2_NAME = "Subtask2";
    private static final Integer SUBTASK_2_ID = 11;
    private static final String SUBTASK_3_NAME = "Subtask3";
    private static final Integer SUBTASK_3_ID = 13;

    public static void main(String[] args) {
        System.out.println("main start");
        // Дополнительное задание. Реализуем пользовательский сценарий

        //#1 - Создайте две задачи, эпик с тремя подзадачами и эпик без подзадач.
        TaskManager inMemoryTaskManager = ManagersUtil.getDefault();

        Task task1 = new Task(TASK_1_NAME, DESCRIPTION, StatusProgress.NEW, TASK_1_ID);
        Task task2 = new Task(TASK_2_NAME, DESCRIPTION, StatusProgress.NEW, TASK_2_ID);
        inMemoryTaskManager.addTask(task1);
        inMemoryTaskManager.addTask(task2);

        Epic epicWithSubtask = new Epic(EPIC_1_WITH_SUBTASK_NAME, DESCRIPTION, EPIC_1_WITH_SUBTASK_ID);
        Epic epicWithoutSubtask = new Epic(EPIC_2_WITHOUT_SUBTASK_NAME, DESCRIPTION, EPIC_2_WITHOUT_SUBTASK_ID);
        inMemoryTaskManager.addEpic(epicWithSubtask);
        inMemoryTaskManager.addEpic(epicWithoutSubtask);

        Subtask subtask1 = new Subtask(EPIC_1_WITH_SUBTASK_ID, SUBTASK_1_NAME, DESCRIPTION, StatusProgress.NEW, SUBTASK_1_ID);
        Subtask subtask2 = new Subtask(EPIC_1_WITH_SUBTASK_ID, SUBTASK_2_NAME, DESCRIPTION, StatusProgress.NEW, SUBTASK_2_ID);
        Subtask subtask3 = new Subtask(EPIC_1_WITH_SUBTASK_ID, SUBTASK_3_NAME, DESCRIPTION, StatusProgress.NEW, SUBTASK_3_ID);
        inMemoryTaskManager.addSubtask(subtask1);
        inMemoryTaskManager.addSubtask(subtask2);
        inMemoryTaskManager.addSubtask(subtask3);

        System.out.println("\nTasks/Epic/Subtask on the start: ");
        System.out.println(inMemoryTaskManager.getAllTasks());
        System.out.println(inMemoryTaskManager.getAllEpics());
        System.out.println(inMemoryTaskManager.getAllSubtasks());

        //#2 - Запросите созданные задачи несколько раз в разном порядке.
        //#3 - ... выведите историю и убедитесь, что в ней нет повторов.
        System.out.println("\nHistory on the start: ");
        System.out.println(inMemoryTaskManager.getHistory());
        //get in order
        inMemoryTaskManager.getTaskForIdNumber(TASK_1_ID);
        inMemoryTaskManager.getTaskForIdNumber(TASK_2_ID);
        inMemoryTaskManager.getEpicForIdNumber(EPIC_1_WITH_SUBTASK_ID);
        inMemoryTaskManager.getEpicForIdNumber(EPIC_2_WITHOUT_SUBTASK_ID);
        inMemoryTaskManager.getSubtaskForIdNumber(SUBTASK_1_ID);
        inMemoryTaskManager.getSubtaskForIdNumber(SUBTASK_2_ID);
        inMemoryTaskManager.getSubtaskForIdNumber(SUBTASK_3_ID);
        System.out.println("\nHistory after get in order: ");
        System.out.println(inMemoryTaskManager.getHistory());
        //get again - revers
        inMemoryTaskManager.getSubtaskForIdNumber(SUBTASK_3_ID);
        inMemoryTaskManager.getSubtaskForIdNumber(SUBTASK_2_ID);
        inMemoryTaskManager.getSubtaskForIdNumber(SUBTASK_1_ID);
        inMemoryTaskManager.getEpicForIdNumber(EPIC_2_WITHOUT_SUBTASK_ID);
        inMemoryTaskManager.getEpicForIdNumber(EPIC_1_WITH_SUBTASK_ID);
        inMemoryTaskManager.getTaskForIdNumber(TASK_2_ID);
        inMemoryTaskManager.getTaskForIdNumber(TASK_1_ID);
        System.out.println("\nHistory after get again in revers order: ");
        System.out.println(inMemoryTaskManager.getHistory());
        //get again - even
        inMemoryTaskManager.getEpicForIdNumber(EPIC_2_WITHOUT_SUBTASK_ID);
        inMemoryTaskManager.getTaskForIdNumber(TASK_2_ID);
        inMemoryTaskManager.getSubtaskForIdNumber(SUBTASK_2_ID);
        System.out.println("\nHistory after get again even Tasks/Epic/Subtask: ");
        System.out.println(inMemoryTaskManager.getHistory());

        //#4 - Удалите задачу, которая есть в истории, и проверьте, что при печати она не будет выводиться.
        inMemoryTaskManager.removeTaskForIdNumber(TASK_1_ID);
        System.out.println("\nHistory after remove Task1: ");
        System.out.println(inMemoryTaskManager.getHistory());

        //#5 - Удалите эпик с тремя подзадачами и убедитесь, что из истории удалился как эпик, так его подзадачи.
        inMemoryTaskManager.removeEpicForIdNumber(EPIC_1_WITH_SUBTASK_ID);
        System.out.println("\nHistory after remove Epic1: ");
        System.out.println(inMemoryTaskManager.getHistory());

        //after all
        System.out.println("\nTasks/Epic/Subtask in the end: ");
        System.out.println("Tasks - " + inMemoryTaskManager.getAllTasks());
        System.out.println("Epic - " + inMemoryTaskManager.getAllEpics());
        System.out.println("Subtask - " + inMemoryTaskManager.getAllSubtasks());

    }
}
