package ru.yandex.practicum.java.tasktracker.manage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.java.tasktracker.utils.*;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Тесты для InMemoryHistoryManager.")
class InMemoryHistoryManagerTest extends TaskManagerTest {
    HistoryManager inMemoryHistoryManager;

    @BeforeEach
    public void Prepare_For_Test() {
        taskManager = ManagersUtil.getDefault();
        inMemoryHistoryManager = ManagersUtil.getDefaultHistory();
        fillData();

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
        expectedResult = ResultOfOperation.SUCCESS;
        inMemoryHistoryManager.addRecord(baseTask1);
        //When
        actualResult = inMemoryHistoryManager.removeRecord(BASE_TASK_1_ID);
        //Then
        assertEquals(expectedResult, actualResult);
    }

    @Test
    @DisplayName("Проверяем получение истории")
    void getHistory_Test() {
        //Given
        int expectedSizeHistory = 1;
        inMemoryHistoryManager.addRecord(baseTask1);
        //When
        int actualSizeHistory = inMemoryHistoryManager.getHistory().size();
        //Then
        assertEquals(expectedSizeHistory, actualSizeHistory);
    }

    @Test
    @DisplayName("Проверяем получение пустой истории")
    void getHistory_Empty_Test() {
        //Given
        int expectedSizeHistory = 0;
        //When
        int actualSizeHistory = inMemoryHistoryManager.getHistory().size();
        //Then
        assertEquals(expectedSizeHistory, actualSizeHistory);
    }

    @Test
    @DisplayName("Проверяем отсутсвие дублей в истории")
    void addRecord_Same_Task_Twice_Test() {
        //Given
        int expectedSizeHistory = 1;
        inMemoryHistoryManager.addRecord(baseTask1);
        baseTask1.setStatusProgress(StatusProgress.DONE);
        inMemoryHistoryManager.addRecord(baseTask1);
        //When
        int actualSizeHistory = inMemoryHistoryManager.getHistory().size();
        //Then
        assertEquals(expectedSizeHistory, actualSizeHistory);
    }

    @Test
    @DisplayName("Проверяем удаление из истории записи в начале списка")
    void removeRecord_First_Test() {
        //Given
        expectedResult = ResultOfOperation.SUCCESS;
        inMemoryHistoryManager.addRecord(baseTask1);
        inMemoryHistoryManager.addRecord(baseTask2);
        inMemoryHistoryManager.addRecord(baseEpic1);
        inMemoryHistoryManager.addRecord(baseEpic2);
        inMemoryHistoryManager.addRecord(baseSubtask1);
        inMemoryHistoryManager.addRecord(baseSubtask2);
        inMemoryHistoryManager.addRecord(baseSubtask3);
        System.out.println("\nhistory BEFORE: " + inMemoryHistoryManager.getHistory());
        //When
        actualResult = inMemoryHistoryManager.removeRecord(BASE_TASK_1_ID);
        System.out.println("\nhistory AFTER: " + inMemoryHistoryManager.getHistory());
        //Then
        assertEquals(expectedResult, actualResult);
    }

    @Test
    @DisplayName("Проверяем удаление из истории записи в середине списка")
    void removeRecord_Middle_Test() {
        //Given
        expectedResult = ResultOfOperation.SUCCESS;
        inMemoryHistoryManager.addRecord(baseTask1);
        inMemoryHistoryManager.addRecord(baseTask2);
        inMemoryHistoryManager.addRecord(baseEpic1);
        inMemoryHistoryManager.addRecord(baseEpic2);
        inMemoryHistoryManager.addRecord(baseSubtask1);
        inMemoryHistoryManager.addRecord(baseSubtask2);
        inMemoryHistoryManager.addRecord(baseSubtask3);
        System.out.println("\nhistory BEFORE: " + inMemoryHistoryManager.getHistory());
        //When
        actualResult = inMemoryHistoryManager.removeRecord(BASE_EPIC_2_ID);
        System.out.println("\nhistory AFTER: " + inMemoryHistoryManager.getHistory());
        //Then
        assertEquals(expectedResult, actualResult);
    }

    @Test
    @DisplayName("Проверяем удаление из истории записи в конце списка")
    void removeRecord_Last_Test() {
        //Given
        expectedResult = ResultOfOperation.SUCCESS;
        inMemoryHistoryManager.addRecord(baseTask1);
        inMemoryHistoryManager.addRecord(baseTask2);
        inMemoryHistoryManager.addRecord(baseEpic1);
        inMemoryHistoryManager.addRecord(baseEpic2);
        inMemoryHistoryManager.addRecord(baseSubtask1);
        inMemoryHistoryManager.addRecord(baseSubtask2);
        inMemoryHistoryManager.addRecord(baseSubtask3);
        System.out.println("\nhistory BEFORE: " + inMemoryHistoryManager.getHistory());
        //When
        actualResult = inMemoryHistoryManager.removeRecord(BASE_SUBTASK_3_ID);
        System.out.println("\nhistory AFTER: " + inMemoryHistoryManager.getHistory());
        //Then
        assertEquals(expectedResult, actualResult);
    }

}