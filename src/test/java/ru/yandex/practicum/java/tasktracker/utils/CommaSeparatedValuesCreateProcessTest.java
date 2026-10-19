package ru.yandex.practicum.java.tasktracker.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.java.tasktracker.task.Epic;
import ru.yandex.practicum.java.tasktracker.task.Subtask;
import ru.yandex.practicum.java.tasktracker.task.Task;
import ru.yandex.practicum.java.tasktracker.utils.enums.StatusProgress;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Тесты для CommaSeparatedValuesCreateProcess")
class CommaSeparatedValuesCreateProcessTest {
    private static final LocalDateTime BASE = LocalDateTime.of(2026, 1, 1, 10, 0);

    private CommaSeparatedValuesCreateProcess csv;

    @BeforeEach
    public void prepareForTest() {
        csv = new CommaSeparatedValuesCreateProcess();
    }

    @Nested
    @DisplayName("Тесты заголовка CSV")
    class HeaderTests {
        @Test
        @DisplayName("Заголовок содержит 10 колонок в правильном порядке")
        void getHeadersForCSV_Test() {
            assertEquals(
                    "id,type,name,description,statusProgress,parentEpic,childSubtasks,durationInMinutes,startDateTime,endDateTime",
                    csv.getHeadersForCSV());
        }
    }

    @Nested
    @DisplayName("Тесты toString(Task)")
    class TaskToStringTests {
        @Test
        @DisplayName("toString(Task) с временем старта и длительностью")
        void toString_TaskWithTime_Test() {
            //Given
            Task task = new Task(1, "task", "desc", StatusProgress.NEW, 10, BASE);
            //When
            String line = csv.toString(task);
            //Then
            assertEquals("1,TASK,task,desc,NEW,SKIP,SKIP,10,2026-01-01T10:00,2026-01-01T10:10", line);
        }

        @Test
        @DisplayName("toString(Task) без времени старта - startDateTime и endDateTime = EMPTY")
        void toString_TaskWithoutTime_Test() {
            //Given
            Task task = new Task(1, "task", "desc", StatusProgress.NEW);
            //When
            String line = csv.toString(task);
            //Then
            assertEquals("1,TASK,task,desc,NEW,SKIP,SKIP,0,EMPTY,EMPTY", line);
        }
    }

    @Nested
    @DisplayName("Тесты toString(Subtask)")
    class SubtaskToStringTests {
        @Test
        @DisplayName("toString(Subtask) с временем старта")
        void toString_SubtaskWithTime_Test() {
            //Given
            Subtask subtask = new Subtask(9, "sub", "desc", StatusProgress.IN_PROGRESS,
                    5, 10, BASE);
            //When
            String line = csv.toString(subtask);
            //Then
            assertEquals("9,SUBTASK,sub,desc,IN_PROGRESS,5,SKIP,10,2026-01-01T10:00,2026-01-01T10:10", line);
        }

        @Test
        @DisplayName("toString(Subtask) без времени старта")
        void toString_SubtaskWithoutTime_Test() {
            //Given
            Subtask subtask = new Subtask(9, "sub", "desc", StatusProgress.NEW, 5);
            //When
            String line = csv.toString(subtask);
            //Then
            assertEquals("9,SUBTASK,sub,desc,NEW,5,SKIP,0,EMPTY,EMPTY", line);
        }
    }

    @Nested
    @DisplayName("Тесты toString(Epic)")
    class EpicToStringTests {
        @Test
        @DisplayName("toString(Epic) без подзадач - childSubtasks = EMPTY")
        void toString_EpicWithoutSubtasks_Test() {
            //Given
            Epic epic = new Epic(5, "epic", "desc");
            //When
            String line = csv.toString(epic);
            //Then
            assertEquals("5,EPIC,epic,desc,NEW,SKIP,EMPTY,0,EMPTY,EMPTY", line);
        }

        @Test
        @DisplayName("toString(Epic) с одной подзадачей - childSubtasks = id подзадачи")
        void toString_EpicWithOneSubtask_Test() {
            //Given
            Epic epic = new Epic(5, "epic", "desc");
            Subtask subtask = new Subtask(9, "sub", "desc", StatusProgress.NEW, 5, 10, BASE);
            epic.addSubtask(subtask);
            //When
            String line = csv.toString(epic);
            //Then
            String[] parts = line.split(",");
            assertEquals("5", parts[0]);
            assertEquals("EPIC", parts[1]);
            assertEquals("9", parts[6]);
            assertEquals("10", parts[7]);
            assertEquals("2026-01-01T10:00", parts[8]);
            assertEquals("2026-01-01T10:10", parts[9]);
        }

        @Test
        @DisplayName("toString(Epic) с двумя подзадачами - childSubtasks через |")
        void toString_EpicWithTwoSubtasks_Test() {
            //Given
            Epic epic = new Epic(5, "epic", "desc");
            Subtask subtask1 = new Subtask(9, "sub1", "desc", StatusProgress.NEW, 5, 10, BASE);
            Subtask subtask2 = new Subtask(11, "sub2", "desc", StatusProgress.NEW, 5, 20, BASE.plusHours(1));
            epic.addSubtask(subtask1);
            epic.addSubtask(subtask2);
            //When
            String line = csv.toString(epic);
            //Then
            String[] parts = line.split(",");
            String childSubtasks = parts[6];
            assertTrue(childSubtasks.contains("|"), "childSubtasks должен содержать разделитель |");
            assertTrue(childSubtasks.contains("9"), "childSubtasks должен содержать 9");
            assertTrue(childSubtasks.contains("11"), "childSubtasks должен содержать 11");
        }
    }

    @Nested
    @DisplayName("Тесты парсинга из строк")
    class ParsingTests {
        @Test
        @DisplayName("getTaskFromStrings - все поля восстанавливаются")
        void getTaskFromStrings_Test() {
            //Given
            String line = "1,TASK,task,desc,NEW,SKIP,SKIP,10,2026-01-01T10:00,2026-01-01T10:10";
            //When
            Task task = csv.getTaskFromStrings(line.split(","));
            //Then
            assertEquals(1, task.getIdNumber());
            assertEquals("task", task.getName().orElse(null));
            assertEquals("desc", task.getDescription().orElse(null));
            assertEquals(StatusProgress.NEW, task.getStatusProgress());
            assertEquals(10L, task.getDurationInMinutes());
            assertEquals(Optional.of(BASE), task.getStartDateTime());
        }

        @Test
        @DisplayName("getTaskFromStrings с EMPTY в startDateTime - время пустое")
        void getTaskFromStrings_NoTime_Test() {
            //Given
            String line = "1,TASK,task,desc,NEW,SKIP,SKIP,0,EMPTY,EMPTY";
            //When
            Task task = csv.getTaskFromStrings(line.split(","));
            //Then
            assertTrue(task.getStartDateTime().isEmpty());
        }

        @Test
        @DisplayName("getSubtaskFromStrings - parentEpicIdNumber восстанавливается")
        void getSubtaskFromStrings_Test() {
            //Given
            String line = "9,SUBTASK,sub,desc,IN_PROGRESS,5,SKIP,10,2026-01-01T10:00,2026-01-01T10:10";
            //When
            Subtask subtask = csv.getSubtaskFromStrings(line.split(","));
            //Then
            assertEquals(9, subtask.getIdNumber());
            assertEquals(5, subtask.getParentEpicIdNumber());
            assertEquals(StatusProgress.IN_PROGRESS, subtask.getStatusProgress());
            assertEquals(10L, subtask.getDurationInMinutes());
        }

        @Test
        @DisplayName("getEpicFromStrings - базовые поля восстанавливаются")
        void getEpicFromStrings_Test() {
            //Given
            String line = "5,EPIC,epic,desc,NEW,SKIP,EMPTY,0,EMPTY,EMPTY";
            //When
            Epic epic = csv.getEpicFromStrings(line.split(","));
            //Then
            assertEquals(5, epic.getIdNumber());
            assertEquals("epic", epic.getName().orElse(null));
            assertEquals("desc", epic.getDescription().orElse(null));
            assertEquals(StatusProgress.NEW, epic.getStatusProgress());
        }
    }

    @Nested
    @DisplayName("Тесты битых строк")
    class BrokenLinesTests {
        @Test
        @DisplayName("Короткая строка (меньше 10 колонок) - RuntimeException")
        void shortLine_Throws_Test() {
            //Given
            String[] fields = {"1", "TASK", "task"};
            //When, Then
            assertThrows(RuntimeException.class, () -> csv.getTaskFromStrings(fields));
        }

        @Test
        @DisplayName("Нечисловой id - RuntimeException")
        void invalidId_Throws_Test() {
            //Given
            String[] fields = {"abc", "TASK", "task", "desc", "NEW", "SKIP", "SKIP", "0", "EMPTY", "EMPTY"};
            //When, Then
            assertThrows(RuntimeException.class, () -> csv.getTaskFromStrings(fields));
        }

        @Test
        @DisplayName("Нечисловой durationInMinutes - RuntimeException")
        void invalidDuration_Throws_Test() {
            //Given
            String[] fields = {"1", "TASK", "task", "desc", "NEW", "SKIP", "SKIP", "ten", "EMPTY", "EMPTY"};
            //When, Then
            assertThrows(RuntimeException.class, () -> csv.getTaskFromStrings(fields));
        }

        @Test
        @DisplayName("Невалидный startDateTime - RuntimeException")
        void invalidStartDateTime_Throws_Test() {
            //Given
            String[] fields = {"1", "TASK", "task", "desc", "NEW", "SKIP", "SKIP", "0", "not-a-date", "EMPTY"};
            //When, Then
            assertThrows(RuntimeException.class, () -> csv.getTaskFromStrings(fields));
        }

        @Test
        @DisplayName("Повторный парсинг после ошибки - парсер не сломан")
        void parserStillWorksAfterException_Test() {
            //Given
            String[] broken = {"abc", "TASK", "task", "desc", "NEW", "SKIP", "SKIP", "0", "EMPTY", "EMPTY"};
            assertThrows(RuntimeException.class, () -> csv.getTaskFromStrings(broken));
            //When
            String[] valid = {"1", "TASK", "task", "desc", "NEW", "SKIP", "SKIP", "0", "EMPTY", "EMPTY"};
            Task task = csv.getTaskFromStrings(valid);
            //Then
            assertEquals(1, task.getIdNumber());
        }
    }
}