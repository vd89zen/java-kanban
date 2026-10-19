package ru.yandex.practicum.java.tasktracker.manage;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ru.yandex.practicum.java.tasktracker.service.TaskManager;
import ru.yandex.practicum.java.tasktracker.service.TaskViewHistory;
import ru.yandex.practicum.java.tasktracker.task.Task;
import ru.yandex.practicum.java.tasktracker.utils.enums.StatusProgress;

import java.io.File;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Тесты для ManagersUtil")
class ManagersUtilTest {
    private static final String DATA_FILE_PROPERTY = "data.file";

    private String savedDataFile;

    @BeforeEach
    public void saveProperty() {
        savedDataFile = System.getProperty(DATA_FILE_PROPERTY);
        System.clearProperty(DATA_FILE_PROPERTY);
    }

    @AfterEach
    public void restoreProperty() {
        if (savedDataFile == null) {
            System.clearProperty(DATA_FILE_PROPERTY);
        } else {
            System.setProperty(DATA_FILE_PROPERTY, savedDataFile);
        }
    }

    @Test
    @DisplayName("getDefault возвращает TaskManager")
    void getDefault_Test() {
        assertTrue(ManagersUtil.getDefault() instanceof TaskManager);
    }

    @Test
    @DisplayName("getDefaultHistory возвращает TaskViewHistory")
    void getDefaultHistory_Test() {
        assertTrue(ManagersUtil.getDefaultHistory() instanceof TaskViewHistory);
    }

    @Test
    @DisplayName("getFileBacked(File) сохраняет данные в указанный файл")
    void getFileBacked_withFile_persistsData_Test(@TempDir Path tempDir) {
        //Given
        File file = tempDir.resolve("tasks.csv").toFile();
        TaskManager manager = ManagersUtil.getFileBacked(file);
        //When
        manager.addTask(new Task("persisted", "desc", StatusProgress.NEW));
        //Then
        assertTrue(file.exists(), "Файл должен быть создан после добавления задачи");
    }

    @Test
    @DisplayName("getFileBacked(File) загружает данные из существующего файла")
    void getFileBacked_withFile_loadsExistingData_Test(@TempDir Path tempDir) {
        //Given
        File file = tempDir.resolve("tasks.csv").toFile();
        TaskManager first = ManagersUtil.getFileBacked(file);
        first.addTask(new Task("persisted", "desc", StatusProgress.NEW));
        //When
        TaskManager second = ManagersUtil.getFileBacked(file);
        //Then
        assertEquals(1, second.getAllTasks().size());
        assertEquals("persisted", second.getAllTasks().get(0).getName().orElse(null));
    }

    @Test
    @DisplayName("getFileBacked() использует путь из sysprop data.file")
    void getFileBacked_usesDataFilePath_Test(@TempDir Path tempDir) {
        //Given
        File file = tempDir.resolve("from-sysprop.csv").toFile();
        System.setProperty(DATA_FILE_PROPERTY, file.getAbsolutePath());
        TaskManager manager = ManagersUtil.getFileBacked();
        //When
        manager.addTask(new Task("persisted", "desc", StatusProgress.NEW));
        //Then
        assertTrue(file.exists(), "Файл должен быть создан по пути из data.file");
    }
}