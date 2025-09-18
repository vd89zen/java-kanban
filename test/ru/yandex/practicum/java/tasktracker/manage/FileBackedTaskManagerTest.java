package ru.yandex.practicum.java.tasktracker.manage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.java.tasktracker.exceptions.ManagerSaveException;
import ru.yandex.practicum.java.tasktracker.service.FileBackedTaskManager;
import ru.yandex.practicum.java.tasktracker.task.Task;
import ru.yandex.practicum.java.tasktracker.utils.StatusProgress;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Тесты для FileBackedTaskManager")
class FileBackedTaskManagerTest extends TaskManagerTest{
    private static File baseTempFile;

    @BeforeEach
    public void Prepare_For_Test() throws IOException {
        baseTempFile = File.createTempFile("basetemptasks", ".csv");
        taskManager = new FileBackedTaskManager(baseTempFile);
        fillData();
    }

    @Nested
    @DisplayName("Тесты загрузки из файла")
    class LoadFromFileTests {
        @Test
        @DisplayName("Проверяем загрузку(добавление задач напрямую в коллекции) " +
                "Менеджера из файла содержащего только строку заголовков")
        void loadFromFileDirectlyToMap_In_File_Only_Headers_Test() {
            //Given
            int expectedNumberIdInWork = 0;
            System.out.println("\nRemove all tasks: " + taskManager.removeAllTasks());
            System.out.println("Remove all epics: " + taskManager.removeAllEpics());
            System.out.println("BEFORE test 'Проверяем загрузку(добавление задач напрямую в коллекции) " +
                    "Менеджера из файла содержащего только строку заголовков'");
            System.out.println(taskManager.getAllTasks());
            System.out.println(taskManager.getAllEpics());
            System.out.println(taskManager.getAllSubtasks());
            //When
            FileBackedTaskManager testTaskManager = FileBackedTaskManager.loadFromFileDirectlyToMap(baseTempFile);
            int actualNumberIdInWork = testTaskManager.getTotalOfIdInWork();
            System.out.println("\nAFTER test 'Проверяем загрузку(добавление задач напрямую в коллекции) " +
                    "Менеджера из файла содержащего только строку заголовков'");
            System.out.println(testTaskManager.getAllTasks());
            System.out.println(testTaskManager.getAllEpics());
            System.out.println(testTaskManager.getAllSubtasks());
            //Then
            assertEquals(expectedNumberIdInWork, actualNumberIdInWork);
        }

        @Test
        @DisplayName("Проверяем загрузку(добавление задач через методы) " +
                "Менеджера из файла содержащего только строку заголовков")
        void loadFromFileUsingMethods_In_File_Only_Headers_Test() {
            //Given
            int expectedNumberIdInWork = 0;
            System.out.println("\nRemove all tasks: " + taskManager.removeAllTasks());
            System.out.println("Remove all epics: " + taskManager.removeAllEpics());
            System.out.println("BEFORE test 'Проверяем загрузку(добавление задач через методы) " +
                    "Менеджера из файла содержащего только строку заголовков'");
            System.out.println(taskManager.getAllTasks());
            System.out.println(taskManager.getAllEpics());
            System.out.println(taskManager.getAllSubtasks());
            //When
            FileBackedTaskManager testTaskManager = FileBackedTaskManager.loadFromFileUsingMethods(baseTempFile);
            int actualNumberIdInWork = testTaskManager.getTotalOfIdInWork();
            System.out.println("\nAFTER test 'Проверяем загрузку(добавление задач через методы) " +
                    "Менеджера из файла содержащего только строку заголовков'");
            System.out.println(testTaskManager.getAllTasks());
            System.out.println(testTaskManager.getAllEpics());
            System.out.println(testTaskManager.getAllSubtasks());
            //Then
            assertEquals(expectedNumberIdInWork, actualNumberIdInWork);
        }

        @Test
        @DisplayName("Проверяем загрузку(добавление задач напрямую в коллекции) " +
                "Менеджера из заполненного файла")
        void loadFromFileDirectlyToMap_Filled_File_Test() {
            //Given
            int expectedNumberIdInWork = 8;
            System.out.println("BEFORE test 'Проверяем загрузку(добавление задач напрямую в коллекции) " +
                    "Менеджера из заполненного файла'");
            System.out.println(taskManager.getAllTasks());
            System.out.println(taskManager.getAllEpics());
            System.out.println(taskManager.getAllSubtasks());
            //When
            FileBackedTaskManager testTaskManager = FileBackedTaskManager.loadFromFileDirectlyToMap(baseTempFile);
            int actualNumberIdInWork = testTaskManager.getTotalOfIdInWork();
            System.out.println("\nAFTER test 'Проверяем загрузку(добавление задач напрямую в коллекции) " +
                    "Менеджера из заполненного файла'");
            System.out.println(testTaskManager.getAllTasks());
            System.out.println(testTaskManager.getAllEpics());
            System.out.println(testTaskManager.getAllSubtasks());
            //Then
            assertEquals(expectedNumberIdInWork, actualNumberIdInWork);
        }

        @Test
        @DisplayName("Проверяем загрузку(добавление задач через методы) " +
                "Менеджера из заполненного файла")
        void loadFromFileUsingMethods_Filled_File_Test() {
            //Given
            int expectedNumberIdInWork = 8;
            System.out.println("BEFORE test 'Проверяем загрузку(добавление задач через методы) " +
                    "Менеджера из заполненного файла'");
            System.out.println(taskManager.getAllTasks());
            System.out.println(taskManager.getAllEpics());
            System.out.println(taskManager.getAllSubtasks());
            //When
            FileBackedTaskManager testTaskManager = FileBackedTaskManager.loadFromFileUsingMethods(baseTempFile);
            int actualNumberIdInWork = testTaskManager.getTotalOfIdInWork();
            System.out.println("\nAFTER test 'Проверяем загрузку(добавление задач через методы) " +
                    "Менеджера из заполненного файла'");
            System.out.println(testTaskManager.getAllTasks());
            System.out.println(testTaskManager.getAllEpics());
            System.out.println(testTaskManager.getAllSubtasks());
            //Then
            assertEquals(expectedNumberIdInWork, actualNumberIdInWork);
        }
    }

    @Test
    @DisplayName("Проверяем перехват исключений при работе с файлом (при загрузке/добавлении задач напрямую в коллекции)")
    void catch_File_Exception_When_Load_Directly_To_Map_Test () throws Exception {
        //Given
        File invalidTempFile = File.createTempFile("invalidtemptasks", ".csv");
        Files.write(invalidTempFile.toPath(), "id,INVALID_TYPE,data".getBytes());
        //When, Then
        Exception exception = assertThrows(RuntimeException.class, () -> {
            FileBackedTaskManager.loadFromFileDirectlyToMap(invalidTempFile);
        });
        assertTrue(exception.getMessage().contains("Неверный порядок данных в файле"));
    }

    @Test
    @DisplayName("Проверяем перехват исключений при работе с файлом (при загрузке/добавлении задач через методы)")
    void catch_File_Exception_When_Load_Using_Methods_Test () throws Exception {
        //Given
        File invalidTempFile = File.createTempFile("invalidtemptasks", ".csv");
        Files.write(invalidTempFile.toPath(), "id,INVALID_TYPE,data".getBytes());
        //When, Then
        Exception exception = assertThrows(RuntimeException.class, () -> {
            FileBackedTaskManager.loadFromFileUsingMethods(invalidTempFile);
        });
        assertTrue(exception.getMessage().contains("Неверный порядок данных в файле"));
    }

    @Test
    @DisplayName("Проверяем что исключения не выбрасываются, а создаётся новый экземпляр менеджера" +
            " при отсутствующем файле для загрузки (при загрузке/добавлении задач напрямую в коллекции)")
    void file_Not_Exists_Exception_Not_Throw_When_Load_Directly_To_Map_Test () throws Exception {
        //Given
        File notExistsTempFile = new File("notexiststempfile.csv");
        //When, Then
        FileBackedTaskManager fileBackedTaskManager = FileBackedTaskManager.loadFromFileDirectlyToMap(notExistsTempFile);
        assertNotNull(fileBackedTaskManager);
    }

    @Test
    @DisplayName("Проверяем что исключения не выбрасываются, а создаётся новый экземпляр менеджера " +
            "при отсутствующем файле для загрузки (при загрузке/добавлении задач через методы)")
    void file_Not_Exists_Exception_Not_Throw_When_Load_Using_Methods_Test () throws Exception {
        //Given
        File notExistsTempFile = new File("notexiststempfile.csv");
        //When, Then
        FileBackedTaskManager fileBackedTaskManager = FileBackedTaskManager.loadFromFileUsingMethods(notExistsTempFile);
        assertNotNull(fileBackedTaskManager);
    }

    @Test
    @DisplayName("Проверяем перехват исключения при сохранении в файл (вызывается при добавалении задачи)")
    void catch_Exception_When_Save_File_Read_Only_Test () throws Exception {
        //Given
        File tempFileReadOnly = File.createTempFile("tempFileReadOnly", ".csv");
        tempFileReadOnly.setReadOnly();
        FileBackedTaskManager fileBackedTaskManager = new FileBackedTaskManager(tempFileReadOnly);
        Task testTask = new Task("testTask", DESCRIPTION, StatusProgress.NEW);
        //When, Then
        ManagerSaveException exception = assertThrows(ManagerSaveException.class, () -> {
            fileBackedTaskManager.addTask(testTask);
        });
        assertTrue(exception.getMessage().contains("Ошибка записи в файл: "));
    }

}