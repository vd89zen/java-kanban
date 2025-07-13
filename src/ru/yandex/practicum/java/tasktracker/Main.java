package ru.yandex.practicum.java.tasktracker;

import ru.yandex.practicum.java.tasktracker.manage.StatusProgress;
import ru.yandex.practicum.java.tasktracker.manage.TaskManager;
import ru.yandex.practicum.java.tasktracker.task.Epic;
import ru.yandex.practicum.java.tasktracker.task.Subtask;
import ru.yandex.practicum.java.tasktracker.task.Task;

public class Main {

    public static void main(String[] args) {
        //простой тест из тз
        TaskManager taskManager = new TaskManager();

        Task task1 = new Task("", "first task", StatusProgress.NEW);
        Task task2 = new Task("second", "second task", StatusProgress.NEW);
        System.out.println("add task 1: " + taskManager.addTask(task1));
        System.out.println("add task 2: " + taskManager.addTask(task2));

        Epic epic1 = new Epic("epic-1", "this epic with sub");
        Epic epic2 = new Epic("epic-2", "this epic with sub");
        System.out.println("add epic 1: " + taskManager.addEpic(epic1));//1sub
        System.out.println("add epic 2: " + taskManager.addEpic(epic2));//2sub


        Subtask subtask1 = new Subtask(epic1.getIdNumber(), "1-1", "1 sub", StatusProgress.NEW);
        Subtask subtask2 = new Subtask(epic2.getIdNumber(), "2-2", "2 sub", StatusProgress.NEW);
        Subtask subtask3 = new Subtask(epic2.getIdNumber(), "3-2", "3 sub", StatusProgress.NEW);
        System.out.println("add subtask 1: " + taskManager.addSubtask(subtask1));
        System.out.println("add subtask 2: " + taskManager.addSubtask(subtask2));
        System.out.println("add subtask 3: " + taskManager.addSubtask(subtask3));

        //print
        System.out.println("print");
        System.out.println(taskManager.getAllTasks());
        System.out.println();
        System.out.println(taskManager.getAllEpics());
        System.out.println();
        System.out.println(taskManager.getAllSubtasks());
        System.out.println();

        System.out.println("TASKs change StatusProgress");
        task1.statusProgress = StatusProgress.IN_PROGRESS;
        task2.statusProgress = StatusProgress.DONE;
        System.out.println("update task1: " + taskManager.updateTask(task1));
        System.out.println("update task2: " + taskManager.updateTask(task2));
        System.out.println("SUBTASKs change StatusProgress");
        subtask1.statusProgress = StatusProgress.IN_PROGRESS;
        subtask2.statusProgress = StatusProgress.DONE;
        subtask3.statusProgress = StatusProgress.IN_PROGRESS;
        System.out.println("update subtask 1: " + taskManager.updateSubtask(subtask1));
        System.out.println("update subtask 2: " + taskManager.updateSubtask(subtask2));
        System.out.println("update subtask 3: " + taskManager.updateSubtask(subtask3));
        System.out.println();

        //print
        System.out.println("print");
        System.out.println(taskManager.getAllTasks());
        System.out.println();
        System.out.println(taskManager.getAllEpics());
        System.out.println();
        System.out.println(taskManager.getAllSubtasks());
        System.out.println();

        System.out.println("TASK 2 Удаление по идентификатору: " + taskManager.removeTaskForIdNumber(task2.getIdNumber()));
        System.out.println("EPIC 1 Удаление по идентификатору: " + taskManager.removeEpicForIdNumber(epic1.getIdNumber()));
        System.out.println("SUBTASK 3 Удаление по идентификатору: " + taskManager.removeSubtaskForIdNumber(subtask3.getIdNumber()));

        //print
        System.out.println("print");
        System.out.println(taskManager.getAllTasks());
        System.out.println();
        System.out.println(taskManager.getAllEpics());
        System.out.println();
        System.out.println(taskManager.getAllSubtasks());
        System.out.println();

        //check epics update
        System.out.println("EPICs check update after kill all SUBTASKs");
        System.out.println("SUBTASKs 4,5,6 add with sp DONE");
        Subtask subtask4 = new Subtask(epic2.getIdNumber(), "4-2", "4 sub", StatusProgress.DONE);
        Subtask subtask5 = new Subtask(epic2.getIdNumber(), "5-2", "5 sub", StatusProgress.DONE);
        Subtask subtask6 = new Subtask(epic2.getIdNumber(), "6-2", "6 sub", StatusProgress.DONE);
        System.out.println("add subtask 4: " + taskManager.addSubtask(subtask4));
        System.out.println("add subtask 5: " + taskManager.addSubtask(subtask5));
        System.out.println("add subtask 6: " + taskManager.addSubtask(subtask6));
        //print
        System.out.println("print");
        System.out.println(taskManager.getAllSubtasks());
        System.out.println();
        System.out.println("EPICs status before");
        System.out.println(taskManager.getAllEpics());
        System.out.println();
        System.out.println("SUBTASKs kill all: " + taskManager.removeAllSubtasks());
        System.out.println("EPICs status after");
        System.out.println(taskManager.getAllEpics());
        System.out.println();

        //return empty not null
        System.out.println("*return empty not null*");
        System.out.println("TASKs kill all: " + taskManager.removeAllTasks());
        System.out.println("EPICSs kill all: " + taskManager.removeAllEpics());
        System.out.println("print");
        System.out.println("TASKs: " + taskManager.getAllTasks());
        System.out.println();
        System.out.println("EPICs: " + taskManager.getAllEpics());
        System.out.println();
        System.out.println("SUBTASKs: " + taskManager.getAllSubtasks());
        System.out.println();
    }
}
