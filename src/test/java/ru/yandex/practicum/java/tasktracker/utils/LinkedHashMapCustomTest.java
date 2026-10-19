package ru.yandex.practicum.java.tasktracker.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.java.tasktracker.utils.enums.ResultOfOperation;
import ru.yandex.practicum.java.tasktracker.utils.enums.StatusProgress;
import ru.yandex.practicum.java.tasktracker.task.Task;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

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

    private ArrayList<Integer> getIds(LinkedHashMapCustom map) {
        ArrayList<Integer> ids = new ArrayList<>();
        for (AbstractTask task : map.getListAllItem()) {
            ids.add(task.getIdNumber());
        }
        return ids;
    }

    @BeforeEach
    public void Prepare_For_Test() {
        history = new LinkedHashMapCustom();
        baseTask1 = new Task(BASE_TASK_1_ID, BASE_TASK_1_NAME, DESCRIPTION, StatusProgress.NEW);
        baseTask2 = new Task(BASE_TASK_2_ID, BASE_TASK_2_NAME, DESCRIPTION, StatusProgress.NEW);
        baseTask3 = new Task(BASE_TASK_3_ID, BASE_TASK_3_NAME, DESCRIPTION, StatusProgress.NEW);
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
    @DisplayName("Проверяем удаление первого элемента - проверкой порядка")
    void removeEntry_First_Test() {
        //Given
        history.addLast(baseTask1);
        history.addLast(baseTask2);
        history.addLast(baseTask3);
        //When
        ResultOfOperation actualResult = history.removeEntry(BASE_TASK_1_ID);
        //Then
        assertEquals(ResultOfOperation.SUCCESS, actualResult);
        assertEquals(List.of(BASE_TASK_2_ID, BASE_TASK_3_ID), getIds(history));
    }

    @Test
    @DisplayName("Проверяем удаление элемента из середины - проверкой порядка")
    void removeEntry_Middle_Test() {
        //Given
        history.addLast(baseTask1);
        history.addLast(baseTask2);
        history.addLast(baseTask3);
        //When
        ResultOfOperation actualResult = history.removeEntry(BASE_TASK_2_ID);
        //Then
        assertEquals(ResultOfOperation.SUCCESS, actualResult);
        assertEquals(List.of(BASE_TASK_1_ID, BASE_TASK_3_ID), getIds(history));
    }

    @Test
    @DisplayName("Проверяем удаление последнего элемента - проверкой порядка")
    void removeEntry_Last_Test() {
        //Given
        history.addLast(baseTask1);
        history.addLast(baseTask2);
        history.addLast(baseTask3);
        //When
        ResultOfOperation actualResult = history.removeEntry(BASE_TASK_3_ID);
        //Then
        assertEquals(ResultOfOperation.SUCCESS, actualResult);
        assertEquals(List.of(BASE_TASK_1_ID, BASE_TASK_2_ID), getIds(history));
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
    @DisplayName("Проверяем removeEntry для несуществующего id")
    void removeEntry_NotExists_Test() {
        //Given
        history.addLast(baseTask1);
        //When
        ResultOfOperation actualResult = history.removeEntry(999);
        //Then
        assertEquals(ResultOfOperation.ERROR_OBJECT_NOT_FOUND, actualResult);
        assertEquals(List.of(BASE_TASK_1_ID), getIds(history));
    }

    @Test
    @DisplayName("Проверяем removeEntry с отрицательным id")
    void removeEntry_NegativeId_Test() {
        //Given, When
        ResultOfOperation actualResult = history.removeEntry(-1);
        //Then
        assertEquals(ResultOfOperation.ERROR_ID_LESS_ZERO, actualResult);
    }

    @Test
    @DisplayName("Проверяем addLast с null")
    void addLast_Null_Test() {
        //Given, When
        ResultOfOperation actualResult = history.addLast(null);
        //Then
        assertEquals(ResultOfOperation.ERROR_OBJECT_NULL, actualResult);
        assertEquals(0, history.getListAllItem().size());
    }

    @Test
    @DisplayName("Проверяем соответствие числа обращений к задачам и записей в истории")
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

    @Test
    @DisplayName("Проверяем, что getListAllItem возвращает копии, а не ссылки")
    void getListAllItem_ReturnsCopies_Test() {
        //Given
        history.addLast(baseTask1);
        //When
        AbstractTask returned = history.getListAllItem().get(0);
        returned.setName("CHANGED");
        //Then
        AbstractTask stored = history.getListAllItem().get(0);
        assertEquals(BASE_TASK_1_NAME, stored.getName().orElse(null),
                "Изменение возвращённого объекта не должно влиять на состояние истории");
    }

    @Test
    @DisplayName("Проверяем, что createCopy независим от оригинала - удаление из копии")
    void createCopy_RemoveFromCopy_DoesNotAffectOriginal_Test() {
        //Given
        history.addLast(baseTask1);
        history.addLast(baseTask2);
        history.addLast(baseTask3);
        LinkedHashMapCustom copy = history.createCopy();
        //When
        copy.removeEntry(BASE_TASK_2_ID);
        //Then
        assertEquals(List.of(BASE_TASK_1_ID, BASE_TASK_2_ID, BASE_TASK_3_ID), getIds(history),
                "Оригинал не должен меняться при удалении из копии");
        assertEquals(List.of(BASE_TASK_1_ID, BASE_TASK_3_ID), getIds(copy),
                "Порядок в копии после удаления среднего элемента");
    }

    @Test
    @DisplayName("Проверяем, что createCopy независим от оригинала - добавление в оригинал")
    void createCopy_AddToOriginal_DoesNotAffectCopy_Test() {
        //Given
        history.addLast(baseTask1);
        LinkedHashMapCustom copy = history.createCopy();
        //When
        history.addLast(baseTask2);
        history.addLast(baseTask3);
        //Then
        assertEquals(List.of(BASE_TASK_1_ID), getIds(copy),
                "Копия не должна меняться при добавлении в оригинал");
        assertEquals(List.of(BASE_TASK_1_ID, BASE_TASK_2_ID, BASE_TASK_3_ID), getIds(history));
    }
}