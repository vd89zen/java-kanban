package ru.yandex.practicum.java.tasktracker.task;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ru.yandex.practicum.java.tasktracker.utils.enums.ResultOfOperation;
import ru.yandex.practicum.java.tasktracker.utils.enums.StatusProgress;

import java.util.List;
import java.util.stream.Stream;

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

    @Test
    @DisplayName("Проверяем сравнение двух Epic с одинаковым набором подзадач")
    void epic_equals_withSubtasks_Test() {
        //Given
        Epic epic1 = new Epic(777, "epic", "description");
        Epic epic2 = new Epic(777, "epic", "description");
        Subtask subtask1ForEpic1 = new Subtask(1, "s1", "desc", StatusProgress.NEW, 777);
        Subtask subtask1ForEpic2 = new Subtask(1, "s1", "desc", StatusProgress.NEW, 777);
        //When
        epic1.addSubtask(subtask1ForEpic1);
        epic2.addSubtask(subtask1ForEpic2);
        //Then
        assertEquals(epic1, epic2);
        assertEquals(epic1.hashCode(), epic2.hashCode());
    }

    @Test
    @DisplayName("Проверяем, что Epic с разным набором подзадач не равны")
    void epic_notEquals_withDifferentSubtasks_Test() {
        //Given
        Epic epic1 = new Epic(777, "epic", "description");
        Epic epic2 = new Epic(777, "epic", "description");
        Subtask subtaskForEpic1 = new Subtask(1, "s1", "desc", StatusProgress.NEW, 777);
        Subtask subtaskForEpic2 = new Subtask(2, "s2", "desc", StatusProgress.NEW, 777);
        //When
        epic1.addSubtask(subtaskForEpic1);
        epic2.addSubtask(subtaskForEpic2);
        //Then
        assertNotEquals(epic1, epic2);
    }

    @Test
    @DisplayName("Проверяем, что Epic с одинаковыми id, но разными полями подзадач, не равны")
    void epic_notEquals_withSameSubtaskIdsButDifferentFields_Test() {
        //Given
        Epic epic1 = new Epic(777, "epic", "description");
        Epic epic2 = new Epic(777, "epic", "description");
        Subtask subtask1 = new Subtask(1, "s1", "desc", StatusProgress.NEW, 777);
        Subtask subtask2 = new Subtask(1, "s1", "desc", StatusProgress.IN_PROGRESS, 777);
        //When
        epic1.addSubtask(subtask1);
        epic2.addSubtask(subtask2);
        //Then
        assertNotEquals(epic1, epic2);
    }

    static Stream<Arguments> statusCases() {
        return Stream.of(
                Arguments.of(List.of(), StatusProgress.NEW),
                Arguments.of(List.of(StatusProgress.NEW), StatusProgress.NEW),
                Arguments.of(List.of(StatusProgress.NEW, StatusProgress.NEW), StatusProgress.NEW),
                Arguments.of(List.of(StatusProgress.IN_PROGRESS), StatusProgress.IN_PROGRESS),
                Arguments.of(List.of(StatusProgress.IN_PROGRESS, StatusProgress.IN_PROGRESS),
                        StatusProgress.IN_PROGRESS),
                Arguments.of(List.of(StatusProgress.DONE), StatusProgress.DONE),
                Arguments.of(List.of(StatusProgress.DONE, StatusProgress.DONE), StatusProgress.DONE),
                Arguments.of(List.of(StatusProgress.NEW, StatusProgress.DONE), StatusProgress.IN_PROGRESS),
                Arguments.of(List.of(StatusProgress.IN_PROGRESS, StatusProgress.DONE), StatusProgress.IN_PROGRESS),
                Arguments.of(List.of(StatusProgress.NEW, StatusProgress.IN_PROGRESS, StatusProgress.DONE),
                        StatusProgress.IN_PROGRESS)
        );
    }

    @ParameterizedTest(name = "[{index}] subtasks={0} -> epic={1}")
    @MethodSource("statusCases")
    @DisplayName("Статус Epic пересчитывается по статусам подзадач")
    void epic_Status_DerivedFromSubtasks_Test(List<StatusProgress> subtaskStatuses,
                                              StatusProgress expectedEpicStatus) {
        //Given
        Epic epic = new Epic("epic", "desc");
        int subtaskId = 1;
        for (StatusProgress status : subtaskStatuses) {
            Subtask subtask = new Subtask(subtaskId++, "sub", "desc", status, 0);
            assertEquals(ResultOfOperation.SUCCESS, epic.addSubtask(subtask),
                    "addSubtask должен вернуть SUCCESS для валидной подзадачи");
        }
        //When
        StatusProgress actual = epic.getStatusProgress();
        //Then
        assertEquals(expectedEpicStatus, actual);
    }
}