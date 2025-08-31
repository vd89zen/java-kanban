package ru.yandex.practicum.java.tasktracker.manage;

import ru.yandex.practicum.java.tasktracker.task.Epic;
import ru.yandex.practicum.java.tasktracker.task.StatusProgress;
import ru.yandex.practicum.java.tasktracker.task.Subtask;
import ru.yandex.practicum.java.tasktracker.task.Task;

public class DataForUserScenario {
    private static final String DESCRIPTION = "description";
    private static Task task1;
    private static final String TASK_1_NAME = "task1";
    private static final Integer TASK_1_ID = 1;
    private static Task task2;
    private static final String TASK_2_NAME = "task2";
    private static final Integer TASK_2_ID = 3;
    private static Epic epic1;
    private static final String EPIC_1_WITH_SUBTASK_NAME = "epic1 WITH subtask";
    private static final Integer EPIC_1_WITH_SUBTASK_ID = 5;
    private static Epic epic2;
    private static final String EPIC_2_WITHOUT_SUBTASK_NAME = "epic2 WITHOUT subtask";
    private static final Integer EPIC_2_WITHOUT_SUBTASK_ID = 7;
    private static Subtask subtask1;
    private static final String SUBTASK_1_NAME = "subtask1";
    private static final Integer SUBTASK_1_ID = 9;
    private static Subtask subtask2;
    private static final String SUBTASK_2_NAME = "subtask2";
    private static final Integer SUBTASK_2_ID = 11;
    private static Subtask subtask3;
    private static final String SUBTASK_3_NAME = "subtask3";
    private static final Integer SUBTASK_3_ID = 13;


    public static void fillData(TaskManager taskManager) {
        task1 = new Task(TASK_1_ID, TASK_1_NAME, DESCRIPTION, StatusProgress.NEW);
        task2 = new Task(TASK_2_ID, TASK_2_NAME, DESCRIPTION, StatusProgress.NEW);
        taskManager.addTask(task1);
        taskManager.addTask(task2);

        epic1 = new Epic(EPIC_1_WITH_SUBTASK_ID, EPIC_1_WITH_SUBTASK_NAME, DESCRIPTION);
        epic2 = new Epic(EPIC_2_WITHOUT_SUBTASK_ID, EPIC_2_WITHOUT_SUBTASK_NAME, DESCRIPTION);
        taskManager.addEpic(epic1);
        taskManager.addEpic(epic2);

        subtask1 = new Subtask(SUBTASK_1_ID, SUBTASK_1_NAME, DESCRIPTION, StatusProgress.NEW, EPIC_1_WITH_SUBTASK_ID);
        subtask2 = new Subtask(SUBTASK_2_ID, SUBTASK_2_NAME, DESCRIPTION, StatusProgress.NEW, EPIC_1_WITH_SUBTASK_ID);
        subtask3 = new Subtask(SUBTASK_3_ID, SUBTASK_3_NAME, DESCRIPTION, StatusProgress.NEW, EPIC_1_WITH_SUBTASK_ID);
        taskManager.addSubtask(subtask1);
        taskManager.addSubtask(subtask2);
        taskManager.addSubtask(subtask3);
    }
}
