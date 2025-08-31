package ru.yandex.practicum.java.tasktracker.manage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.java.tasktracker.task.StatusProgress;
import ru.yandex.practicum.java.tasktracker.task.Task;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Тесты для InMemoryHistoryManager.")
class InMemoryHistoryManagerTest {
    private static InMemoryHistoryManager inMemoryHistoryManager;
    private static ResultOfOperation actualResult;
    private static ResultOfOperation expectedResult;
    private static final String DESCRIPTION = "description";
    private static Task baseTask1;
    private static final String BASE_TASK_1_NAME = "baseTask1";
    private static final Integer BASE_TASK_1_ID = 1;

    @BeforeEach
    public void Prepare_For_Test() {
        inMemoryHistoryManager = new InMemoryHistoryManager();
        baseTask1 = new Task(BASE_TASK_1_ID, BASE_TASK_1_NAME, DESCRIPTION, StatusProgress.NEW);
    }

    @Test
    @DisplayName("Проверяем добавление записи в историю")
    void addRecord_Test() {
        //Given
        expectedResult = ResultOfOperation.SUCCESS;
        //When
        actualResult = inMemoryHistoryManager.addRecord(baseTask1);
        //Then
        assertEquals(expectedResult, actualResult);
    }

    @Test
    @DisplayName("Проверяем удаление записи из истории")
    void removeRecord_Test() {
        //Given
        inMemoryHistoryManager.addRecord(baseTask1);
        expectedResult = ResultOfOperation.SUCCESS;
        //When
        actualResult = inMemoryHistoryManager.removeRecord(BASE_TASK_1_ID);
        //Then
        assertEquals(expectedResult, actualResult);
    }

    @Test
    @DisplayName("Проверяем получение истории")
    void getHistory_Test() {
        //Given
        inMemoryHistoryManager.addRecord(baseTask1);
        Integer expectedSizeHistory = 1;
        //When
        Integer actualSizeHistory = inMemoryHistoryManager.getHistory().size();
        //Then
        assertEquals(expectedSizeHistory, actualSizeHistory);
    }
}