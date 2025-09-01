package ru.yandex.practicum.java.tasktracker.task;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.java.tasktracker.utils.StatusProgress;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Тест для типа задач Task")
class TaskTest {

    @Test
    @DisplayName("Проверяем сравнение двух Task с идентичными полями")
    void task_equals_Test() {
        //Given
        String taskName = "name";
        String taskDescription = "description";
        StatusProgress taskStatusProgress = StatusProgress.IN_PROGRESS;
        Integer taskIdNumber = 888;
        //When
        Task task1 =  new Task(taskIdNumber, taskName, taskDescription, taskStatusProgress);
        Task task2 =  new Task(taskIdNumber, taskName, taskDescription, taskStatusProgress);
        //Then
        assertEquals(task1, task2);
    }
}