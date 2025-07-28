package ru.yandex.practicum.java.tasktracker.task;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Тест для типа задач Subtask")
class SubtaskTest {

    @Test
    @DisplayName("Проверяем сравнение двух Subtask с идентичными полями")
    void subtask_equals_Test() {
        //Given
        String subtaskName = "name";
        String subtaskDescription = "description";
        StatusProgress subtaskStatusProgress = StatusProgress.IN_PROGRESS;
        Integer subtaskIdNumber = 333;
        Integer subtaskParentEpicIdNumber = 777;
        //When
        Subtask subtask1 =  new Subtask(subtaskParentEpicIdNumber, subtaskName, subtaskDescription
                , subtaskStatusProgress, subtaskIdNumber);
        Subtask subtask2 =  new Subtask(subtaskParentEpicIdNumber, subtaskName, subtaskDescription
                , subtaskStatusProgress, subtaskIdNumber);
        //Then
        assertEquals(subtask1, subtask2);
    }
}