package ru.yandex.practicum.java.tasktracker.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.java.tasktracker.task.Epic;
import ru.yandex.practicum.java.tasktracker.task.Subtask;
import ru.yandex.practicum.java.tasktracker.task.Task;
import ru.yandex.practicum.java.tasktracker.utils.enums.ResultOfOperation;
import ru.yandex.practicum.java.tasktracker.utils.enums.StatusProgress;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Тесты для TaskViewHistory")
class TaskViewHistoryTest {
    private static final String DESCRIPTION = "description";
    private static final int TASK_1_ID = 1;
    private static final int TASK_2_ID = 2;
    private static final int EPIC_1_ID = 3;
    private static final int EPIC_2_ID = 4;
    private static final int SUBTASK_1_ID = 5;
    private static final int SUBTASK_2_ID = 6;
    private static final int SUBTASK_3_ID = 7;

    private TaskViewHistory history;
    private Task task1;
    private Task task2;
    private Epic epic1;
    private Epic epic2;
    private Subtask subtask1;
    private Subtask subtask2;
    private Subtask subtask3;

    @BeforeEach
    public void prepareForTest() {
        history = new TaskViewHistory();
        task1 = new Task(TASK_1_ID, "task1", DESCRIPTION, StatusProgress.NEW);
        task2 = new Task(TASK_2_ID, "task2", DESCRIPTION, StatusProgress.NEW);
        epic1 = new Epic(EPIC_1_ID, "epic1", DESCRIPTION);
        epic2 = new Epic(EPIC_2_ID, "epic2", DESCRIPTION);
        subtask1 = new Subtask(SUBTASK_1_ID, "subtask1", DESCRIPTION, StatusProgress.NEW, EPIC_1_ID);
        subtask2 = new Subtask(SUBTASK_2_ID, "subtask2", DESCRIPTION, StatusProgress.NEW, EPIC_1_ID);
        subtask3 = new Subtask(SUBTASK_3_ID, "subtask3", DESCRIPTION, StatusProgress.NEW, EPIC_1_ID);
    }

    @Test
    @DisplayName("Проверяем добавление записи в историю")
    void addRecord_Test() {
        //Given
        ResultOfOperation expectedResult = ResultOfOperation.SUCCESS;
        //When
        ResultOfOperation actualResult = history.addRecord(task1);
        //Then
        assertEquals(expectedResult, actualResult);
    }

    @Test
    @DisplayName("Проверяем удаление записи из истории")
    void removeRecord_Test() {
        //Given
        ResultOfOperation expectedResult = ResultOfOperation.SUCCESS;
        history.addRecord(task1);
        //When
        ResultOfOperation actualResult = history.removeRecord(TASK_1_ID);
        //Then
        assertEquals(expectedResult, actualResult);
    }

    @Test
    @DisplayName("Проверяем получение истории")
    void getHistory_Test() {
        //Given
        int expectedSize = 1;
        history.addRecord(task1);
        //When
        int actualSize = history.getHistory().size();
        //Then
        assertEquals(expectedSize, actualSize);
    }

    @Test
    @DisplayName("Проверяем получение пустой истории")
    void getHistory_Empty_Test() {
        //Given
        int expectedSize = 0;
        //When
        int actualSize = history.getHistory().size();
        //Then
        assertEquals(expectedSize, actualSize);
    }

    @Test
    @DisplayName("Проверяем отсутствие дублей в истории")
    void addRecord_Same_Task_Twice_Test() {
        //Given
        int expectedSize = 1;
        history.addRecord(task1);
        task1.setStatusProgress(StatusProgress.DONE);
        //When
        history.addRecord(task1);
        int actualSize = history.getHistory().size();
        //Then
        assertEquals(expectedSize, actualSize);
    }

    @Test
    @DisplayName("Проверяем удаление из истории записи в начале списка")
    void removeRecord_First_Test() {
        //Given
        ResultOfOperation expectedResult = ResultOfOperation.SUCCESS;
        history.addRecord(task1);
        history.addRecord(task2);
        history.addRecord(epic1);
        history.addRecord(epic2);
        history.addRecord(subtask1);
        history.addRecord(subtask2);
        history.addRecord(subtask3);
        //When
        ResultOfOperation actualResult = history.removeRecord(TASK_1_ID);
        //Then
        assertEquals(expectedResult, actualResult);
        assertEquals(6, history.getHistory().size());
    }

    @Test
    @DisplayName("Проверяем удаление из истории записи в середине списка")
    void removeRecord_Middle_Test() {
        //Given
        ResultOfOperation expectedResult = ResultOfOperation.SUCCESS;
        history.addRecord(task1);
        history.addRecord(task2);
        history.addRecord(epic1);
        history.addRecord(epic2);
        history.addRecord(subtask1);
        history.addRecord(subtask2);
        history.addRecord(subtask3);
        //When
        ResultOfOperation actualResult = history.removeRecord(EPIC_2_ID);
        //Then
        assertEquals(expectedResult, actualResult);
        assertEquals(6, history.getHistory().size());
    }

    @Test
    @DisplayName("Проверяем удаление из истории записи в конце списка")
    void removeRecord_Last_Test() {
        //Given
        ResultOfOperation expectedResult = ResultOfOperation.SUCCESS;
        history.addRecord(task1);
        history.addRecord(task2);
        history.addRecord(epic1);
        history.addRecord(epic2);
        history.addRecord(subtask1);
        history.addRecord(subtask2);
        history.addRecord(subtask3);
        //When
        ResultOfOperation actualResult = history.removeRecord(SUBTASK_3_ID);
        //Then
        assertEquals(expectedResult, actualResult);
        assertEquals(6, history.getHistory().size());
    }
}