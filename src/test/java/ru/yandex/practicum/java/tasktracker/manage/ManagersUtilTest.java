package ru.yandex.practicum.java.tasktracker.manage;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.java.tasktracker.service.InMemoryHistoryManager;
import ru.yandex.practicum.java.tasktracker.service.InMemoryTaskManager;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Тесты для ManagersUtil (Менеджер менеджеров).")
class ManagersUtilTest {

    @Test
    @DisplayName("Проверяем готовность экземпляра класса InMemoryTaskManager")
    void getDefault_Test() {
        //Given
        boolean isInMemoryTaskManager = false;
        //When
        isInMemoryTaskManager = ManagersUtil.getDefault() instanceof InMemoryTaskManager;
        //Then
        assertTrue(isInMemoryTaskManager);
    }

    @Test
    @DisplayName("Проверяем готовность экземпляра класса InMemoryHistoryManager")
    void getDefaultHistory_Test() {
        //Given
        boolean isInMemoryHistoryManager = false;
        //When
        isInMemoryHistoryManager = ManagersUtil.getDefaultHistory() instanceof InMemoryHistoryManager;
        //Then
        assertTrue(isInMemoryHistoryManager);
    }
}