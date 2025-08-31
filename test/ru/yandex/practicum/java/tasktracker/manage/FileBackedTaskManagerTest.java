package ru.yandex.practicum.java.tasktracker.manage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.java.tasktracker.task.StatusProgress;
import ru.yandex.practicum.java.tasktracker.task.Task;
import java.io.File;
import java.io.IOException;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Тесты для InMemoryTaskManager")
class FileBackedTaskManagerTest {
    private static final String DESCRIPTION = "description";
    private static Task baseTask1;
    private static final String BASE_TASK_1_NAME = "baseTask1";
    private static final Integer BASE_TASK_1_ID = 1;
    private static Task baseTask2;
    private static final String BASE_TASK_2_NAME = "baseTask2";
    private static final Integer BASE_TASK_2_ID = 2;
    private static Task baseTask3;
    private static final String BASE_TASK_3_NAME = "baseTask3";
    private static final Integer BASE_TASK_3_ID = 3;
    private static FileBackedTaskManager fileBackedTaskManager;
    private static File baseTempFile;

    @BeforeEach
    public void Prepare_For_Test() throws IOException {
        baseTempFile = File.createTempFile("basetemptasks", ".csv");
        fileBackedTaskManager = new FileBackedTaskManager(baseTempFile);
        baseTask1 = new Task(BASE_TASK_1_ID, BASE_TASK_1_NAME, DESCRIPTION, StatusProgress.NEW);
        baseTask2 = new Task(BASE_TASK_2_ID, BASE_TASK_2_NAME, DESCRIPTION, StatusProgress.NEW);
        baseTask3 = new Task(BASE_TASK_3_ID, BASE_TASK_3_NAME, DESCRIPTION, StatusProgress.NEW);
        fileBackedTaskManager.addTask(baseTask1);
        fileBackedTaskManager.addTask(baseTask2);
        fileBackedTaskManager.addTask(baseTask3);
        System.out.println("\nTasks BeforeEach");
        System.out.println(fileBackedTaskManager.getAllTasks());
    }

    @Test
    @DisplayName("Проверяем загрузку Менеджера из файла 'пустого' содержащего только строку заголовков")
    void loadFromFile_Empty_Test() {
        //Given
        final int expectedSize = 0;
        System.out.println("\nRemove all tasks: " + fileBackedTaskManager.removeAllTasks());
        System.out.println("Tasks BEFORE test 'Проверяем загрузку Менеджера из файла содержащего только строку заголовков'");
        System.out.println(fileBackedTaskManager.getAllTasks());
        //When
        FileBackedTaskManager testTaskManager = FileBackedTaskManager.loadFromFile(baseTempFile);
        final int actualSize = testTaskManager.getAllTasks().size();
        System.out.println("\nTasks AFTER test 'Проверяем загрузку Менеджера из файла содержащего только строку заголовков'");
        System.out.println(testTaskManager.getAllTasks());
        //Then
        assertEquals(expectedSize, actualSize);
    }

    @Test
    @DisplayName("Проверяем загрузку Менеджера из заполненного файла")
    void loadFromFile_Filled_Test() {
        //Given
        final int expectedSize = 3;
        //When
        FileBackedTaskManager testTaskManager = FileBackedTaskManager.loadFromFile(baseTempFile);
        final int actualSize = testTaskManager.getAllTasks().size();
        System.out.println("\nTasks AFTER test 'Проверяем загрузку Менеджера из заполненного файла");
        System.out.println(testTaskManager.getAllTasks());
        //Then
        assertEquals(expectedSize, actualSize);
    }

    @Test
    @DisplayName("Проверяем обновление Task")
    void updateTask_Test() {
        //Given
        String expectedName = "testTask-UPDATED";
        String oldName = "testTask";
        Task testTask = new Task(oldName, DESCRIPTION, StatusProgress.NEW);
        fileBackedTaskManager.addTask(testTask);
        System.out.println("\nTasks BEFORE test 'Проверяем обновление Task':");
        System.out.println(fileBackedTaskManager.getAllTasks());
        //When
        testTask.setName(expectedName);
        fileBackedTaskManager.updateTask(testTask);
        String actualName = fileBackedTaskManager.getTaskForIdNumber(testTask.getIdNumber()).getName();
        System.out.println("\nTasks AFTER test 'Проверяем обновление Task':");
        System.out.println(fileBackedTaskManager.getAllTasks());
        //Then
        assertEquals(expectedName, actualName);
    }
}