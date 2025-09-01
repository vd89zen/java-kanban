package ru.yandex.practicum.java.tasktracker;

import ru.yandex.practicum.java.tasktracker.service.FileBackedTaskManager;
import ru.yandex.practicum.java.tasktracker.utils.DataForUserScenario;

import java.io.File;
import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        /*
         * Дополнительное задание. Реализуем пользовательский сценарий.
         * 1. Заведите несколько разных задач, эпиков и подзадач.
         * 2. Создайте новый FileBackedTaskManager-менеджер из этого же файла.
         * 3. Проверьте, что все задачи, эпики, подзадачи, которые были в старом менеджере, есть в новом.
         */

        File tempFile = File.createTempFile("temptasks", ".csv");

        FileBackedTaskManager fileBackedTaskManager = new FileBackedTaskManager(tempFile);
        //1
        DataForUserScenario.fillData(fileBackedTaskManager);
        System.out.println("\nДанные из Менеджера созданного с нуля");
        System.out.println(fileBackedTaskManager.getAllTasks());
        System.out.println(fileBackedTaskManager.getAllEpics());
        System.out.println(fileBackedTaskManager.getAllSubtasks());
        //2
        FileBackedTaskManager fileBackedTaskManagerFromFile = FileBackedTaskManager.loadFromFile(tempFile);
        //3
        System.out.println("\nДанные из Менеджера созданного из файла");
        System.out.println(fileBackedTaskManagerFromFile.getAllTasks());
        System.out.println(fileBackedTaskManagerFromFile.getAllEpics());
        System.out.println(fileBackedTaskManagerFromFile.getAllSubtasks());
    }
}
