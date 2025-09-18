package ru.yandex.practicum.java.tasktracker;

import ru.yandex.practicum.java.tasktracker.exceptions.*;
import ru.yandex.practicum.java.tasktracker.manage.*;
import ru.yandex.practicum.java.tasktracker.service.*;
import ru.yandex.practicum.java.tasktracker.task.*;
import ru.yandex.practicum.java.tasktracker.utils.*;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;

public class Main {
    public static void main(String[] args) throws IOException {
        /*
         *простое сравнение двух методов загрузки из файла
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
        //Время загрузки FileBackedTaskManager.loadFromFileUsingMethods - 20376700 nanoseconds
        //Время загрузки FileBackedTaskManager.loadFromFileDirectlyToMap - 5025100 nanoseconds

        //FileBackedTaskManager fileBackedTaskManagerFromFile = FileBackedTaskManager.loadFromFileUsingMethods(tempFile);
        FileBackedTaskManager fileBackedTaskManagerFromFile = FileBackedTaskManager.loadFromFileDirectlyToMap(tempFile);

        //3
        System.out.println("\nДанные из Менеджера созданного из файла");
        System.out.println(fileBackedTaskManagerFromFile.getAllTasks());
        System.out.println(fileBackedTaskManagerFromFile.getAllEpics());
        System.out.println(fileBackedTaskManagerFromFile.getAllSubtasks());
    }
}
