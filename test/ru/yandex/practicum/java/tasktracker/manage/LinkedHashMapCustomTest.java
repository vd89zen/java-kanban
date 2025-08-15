package ru.yandex.practicum.java.tasktracker.manage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import ru.yandex.practicum.java.tasktracker.task.AbstractTask;
import ru.yandex.practicum.java.tasktracker.task.StatusProgress;
import ru.yandex.practicum.java.tasktracker.task.Task;
import java.util.ArrayList;

@DisplayName("Тесты для класса LinkedHashMapCustom (используется для хранения истории")
class LinkedHashMapCustomTest {
    private static LinkedHashMapCustom history;
    private static LinkedHashMapCustom actualHistory;
    private static LinkedHashMapCustom expectedHistory;
    private static ResultOfOperation actualResult;
    private static ResultOfOperation expectedResult;
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

    @BeforeEach
    public void Prepare_For_Test() {
        history = new LinkedHashMapCustom();
        baseTask1 = new Task(BASE_TASK_1_NAME, DESCRIPTION, StatusProgress.NEW, BASE_TASK_1_ID);
        baseTask2 = new Task(BASE_TASK_2_NAME, DESCRIPTION, StatusProgress.NEW, BASE_TASK_2_ID);
        baseTask3 = new Task(BASE_TASK_3_NAME, DESCRIPTION, StatusProgress.NEW, BASE_TASK_3_ID);
    }

    @Test
    @DisplayName("Проверяем добавление элемента в пустую коллекцию")
    void addLast_To_Empty_HashMap_Test() {
        //Given
        expectedResult = ResultOfOperation.SUCCESS;
        //When
        actualResult = history.addLast(baseTask1);
        //Then
        assertEquals(expectedResult, actualResult);
    }

    @Test
    @DisplayName("Проверяем добавление элемента в коллекцию с 1 элементом")
    void addLast_To_HashMap_With_One_Item_Test() {
        //Given
        history.addLast(baseTask1);
        expectedResult = ResultOfOperation.SUCCESS;
        //When
        actualResult = history.addLast(baseTask2);
        //Then
        assertEquals(expectedResult, actualResult);
    }

    @Test
    @DisplayName("Проверяем что в коллекции остаётся только последнее обращение к задаче")
    void addLast_Only_Last_Access_Save_To_HashMap_Test() {
        //Given
        ArrayList<AbstractTask> expectedOrder = new ArrayList<>();
        expectedOrder.add(baseTask1);
        expectedOrder.add(baseTask3);
        expectedOrder.add(baseTask2);
        history.addLast(baseTask1);
        history.addLast(baseTask2);
        history.addLast(baseTask3);
        //When
        history.addLast(baseTask2);
        ArrayList<AbstractTask> actualOrder = history.getListAllItem();
        //Then
        assertEquals(expectedOrder, actualOrder);
    }

    @Test
    @DisplayName("Проверяем равенство оригинала и копии истории")
    void createCopy_Equals_Origin_And_Copy_Test() {
        //Given
        history.addLast(baseTask1);
        history.addLast(baseTask2);
        history.addLast(baseTask3);
        //When
        LinkedHashMapCustom historyCopy = history.createCopy();
        //Then
        assertEquals(history, historyCopy);
    }

    @Test
    @DisplayName("Проверяем удаление элемента из коллекции - проверкой результата операции")
    void removeEntry_Chek_Result_Of_Operation_Test() {
        //Given
        history.addLast(baseTask1);
        history.addLast(baseTask2);
        expectedResult = ResultOfOperation.SUCCESS;
        //When
        actualResult = history.removeEntry(BASE_TASK_1_ID);
        //Then
        assertEquals(expectedResult, actualResult);
    }

    @Test
    @DisplayName("Проверяем удаление элемента из коллекции - сравнением до и после")
    void removeEntry_Equals_Before_And_After_Test() {
        //Given
        history.addLast(baseTask1);
        history.addLast(baseTask2);
        expectedHistory = history.createCopy();
        //When
        history.removeEntry(BASE_TASK_1_ID);
        actualHistory = history.createCopy();
        //Then
        assertNotEquals(expectedHistory, actualHistory);
    }

    @Test
    @DisplayName("Проверяем соответсвие числа обращений к задачам и записей в истории")
    void getListAllItem_Test() {
        //Given
        history.addLast(baseTask1);
        history.addLast(baseTask2);
        history.addLast(baseTask3);
        Integer expectedSize = 3;
        //When
        ArrayList<AbstractTask> testHistory = history.getListAllItem();
        Integer actualSize = testHistory.size();
        //Then
        assertEquals(expectedSize, actualSize);
    }

    @Test
    @DisplayName("Проверяем получение пустого списка из пустой коллекции")
    void getListAllItem_From_Empty_History_Test() {
        //Given
        Integer expectedSize = 0;
        //When
        ArrayList<AbstractTask> testHistory = history.getListAllItem();
        Integer actualSize = testHistory.size();
        //Then
        assertEquals(expectedSize, actualSize);
    }
}