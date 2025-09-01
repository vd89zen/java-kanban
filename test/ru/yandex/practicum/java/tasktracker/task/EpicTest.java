package ru.yandex.practicum.java.tasktracker.task;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Тест для типа задач Epic")
class EpicTest {

    @Test
    @DisplayName("Проверяем сравнение двух Epic с идентичными полями")
    void epic_equals_Test() {
        //Given
        String epicName = "name";
        String epicDescription = "description";
        Integer epicIdNumber = 777;
        //When
        Epic epic1 =  new Epic(epicIdNumber, epicName, epicDescription);
        Epic epic2 =  new Epic(epicIdNumber, epicName, epicDescription);
        //Then
        assertEquals(epic1, epic2);
    }
}