package ru.yandex.practicum.java.tasktracker.storage;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.java.tasktracker.task.Epic;
import ru.yandex.practicum.java.tasktracker.task.Subtask;
import ru.yandex.practicum.java.tasktracker.task.Task;
import ru.yandex.practicum.java.tasktracker.utils.enums.StatusProgress;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Тесты для CsvStorage")
class CsvStorageTest {
    private static final String DESCRIPTION = "description";

    private File tempFile;
    private CsvStorage storage;

    @BeforeEach
    public void prepareForTest() throws IOException {
        tempFile = File.createTempFile("csv-storage-test", ".csv");
        storage = new CsvStorage(tempFile);
    }

    @AfterEach
    public void cleanUp() throws IOException {
        Files.deleteIfExists(tempFile.toPath());
    }

    private Task newTask(int id, String name) {
        return new Task(id, name, DESCRIPTION, StatusProgress.NEW);
    }

    private Epic newEpic(int id, String name) {
        return new Epic(id, name, DESCRIPTION);
    }

    private Subtask newSubtask(int id, String name, int parentEpicId) {
        return new Subtask(id, name, DESCRIPTION, StatusProgress.NEW, parentEpicId);
    }

    @Nested
    @DisplayName("Тесты save")
    class SaveTests {
        @Test
        @DisplayName("save с пустыми коллекциями записывает только заголовок")
        void save_EmptyCollections_WritesOnlyHeader_Test() throws IOException {
            //When
            storage.save(List.of(), List.of(), List.of());
            //Then
            List<String> lines = Files.readAllLines(tempFile.toPath());
            assertEquals(1, lines.size(), "Должен быть только заголовок");
            assertTrue(lines.get(0).startsWith("id,type,name"), "Первая строка — заголовок CSV");
        }

        @Test
        @DisplayName("save с данными записывает TASK, EPIC, SUBTASK в этом порядке")
        void save_WithAllTypes_WritesInTypeOrder_Test() throws IOException {
            //Given
            Task task = newTask(1, "task");
            Epic epic = newEpic(5, "epic");
            Subtask subtask = newSubtask(9, "subtask", 5);
            //When
            storage.save(List.of(task), List.of(epic), List.of(subtask));
            //Then
            List<String> lines = Files.readAllLines(tempFile.toPath());
            assertEquals(4, lines.size(), "Заголовок + 3 строки данных");
            assertTrue(lines.get(1).contains(",TASK,"), "Строка 1 — TASK");
            assertTrue(lines.get(2).contains(",EPIC,"), "Строка 2 — EPIC");
            assertTrue(lines.get(3).contains(",SUBTASK,"), "Строка 3 — SUBTASK");
        }

        @Test
        @DisplayName("save сортирует записи по id")
        void save_SortsById_Test() throws IOException {
            //Given
            Task task3 = newTask(3, "task3");
            Task task1 = newTask(1, "task1");
            Task task2 = newTask(2, "task2");
            //When
            storage.save(List.of(task3, task1, task2), List.of(), List.of());
            //Then
            List<String> lines = Files.readAllLines(tempFile.toPath());
            assertTrue(lines.get(1).startsWith("1,"), "Первая задача — id=1");
            assertTrue(lines.get(2).startsWith("2,"), "Вторая задача — id=2");
            assertTrue(lines.get(3).startsWith("3,"), "Третья задача — id=3");
        }
    }

    @Nested
    @DisplayName("Тесты load")
    class LoadTests {
        @Test
        @DisplayName("load несуществующего файла возвращает пустой LoadedState")
        void load_FileNotExists_ReturnsEmptyState_Test() {
            //Given
            File notExists = new File(tempFile.getParent(), "not-exists-" + System.nanoTime() + ".csv");
            CsvStorage notExistsStorage = new CsvStorage(notExists);
            //When
            LoadedState state = notExistsStorage.load();
            //Then
            assertNotNull(state);
            assertEquals(0, state.tasks().size());
            assertEquals(0, state.epics().size());
            assertEquals(0, state.subtasks().size());
        }

        @Test
        @DisplayName("load файла только с заголовком возвращает пустой LoadedState")
        void load_OnlyHeader_ReturnsEmptyState_Test() throws IOException {
            //Given
            storage.save(List.of(), List.of(), List.of());
            //When
            LoadedState state = storage.load();
            //Then
            assertEquals(0, state.tasks().size());
            assertEquals(0, state.epics().size());
            assertEquals(0, state.subtasks().size());
        }

        @Test
        @DisplayName("load пропускает строку с недостаточным числом колонок")
        void load_SkipsLineWithTooFewColumns_Test() throws IOException {
            //Given
            Task validTask = newTask(1, "valid");
            storage.save(List.of(validTask), List.of(), List.of());
            // добавляем в файл битую строку
            Files.writeString(tempFile.toPath(),
                    System.lineSeparator() + "99,TASK,broken" + System.lineSeparator(),
                    java.nio.file.StandardOpenOption.APPEND);
            //When
            LoadedState state = storage.load();
            //Then
            assertEquals(1, state.tasks().size(), "Валидная задача должна загрузиться");
            assertEquals(1, state.tasks().iterator().next().getIdNumber());
        }

        @Test
        @DisplayName("load пропускает строку с неизвестным типом")
        void load_SkipsLineWithUnknownType_Test() throws IOException {
            //Given
            Task validTask = newTask(1, "valid");
            storage.save(List.of(validTask), List.of(), List.of());
            // строка с 10 колонками, но тип UNKNOWN
            String unknownLine = "99,UNKNOWN,x,x,NEW,SKIP,SKIP,0,EMPTY,EMPTY";
            Files.writeString(tempFile.toPath(),
                    System.lineSeparator() + unknownLine + System.lineSeparator(),
                    java.nio.file.StandardOpenOption.APPEND);
            //When
            LoadedState state = storage.load();
            //Then
            assertEquals(1, state.tasks().size());
            assertEquals(0, state.epics().size());
            assertEquals(0, state.subtasks().size());
        }

        @Test
        @DisplayName("load игнорирует пустые строки")
        void load_SkipsBlankLines_Test() throws IOException {
            //Given
            Task validTask = newTask(1, "valid");
            storage.save(List.of(validTask), List.of(), List.of());
            Files.writeString(tempFile.toPath(),
                    System.lineSeparator() + System.lineSeparator() + "   " + System.lineSeparator(),
                    java.nio.file.StandardOpenOption.APPEND);
            //When
            LoadedState state = storage.load();
            //Then
            assertEquals(1, state.tasks().size(), "Пустые строки не должны приводить к ошибкам");
        }

        @Test
        @DisplayName("load бросает исключение при нечисловом id")
        void load_ThrowsOnInvalidId_Test() throws IOException {
            //Given
            storage.save(List.of(), List.of(), List.of());
            String invalidLine = "not-a-number,TASK,name,desc,NEW,SKIP,SKIP,0,EMPTY,EMPTY";
            Files.writeString(tempFile.toPath(),
                    System.lineSeparator() + invalidLine + System.lineSeparator(),
                    java.nio.file.StandardOpenOption.APPEND);
            //When, Then
            assertThrows(RuntimeException.class, () -> storage.load());
        }
    }

    @Nested
    @DisplayName("Круговой прогон save → load")
    class RoundTripTests {
        @Test
        @DisplayName("save → load возвращает те же данные")
        void roundTrip_ReturnsSameData_Test() {
            //Given
            Task task = newTask(1, "task");
            Epic epic = newEpic(5, "epic");
            Subtask subtask = newSubtask(9, "subtask", 5);
            //When
            storage.save(List.of(task), List.of(epic), List.of(subtask));
            LoadedState state = storage.load();
            //Then
            assertEquals(1, state.tasks().size());
            assertEquals(1, state.epics().size());
            assertEquals(1, state.subtasks().size());

            Task loadedTask = state.tasks().iterator().next();
            assertEquals(task.getIdNumber(), loadedTask.getIdNumber());
            assertEquals(task.getName().orElse(null), loadedTask.getName().orElse(null));
            assertEquals(task.getStatusProgress(), loadedTask.getStatusProgress());

            Epic loadedEpic = state.epics().iterator().next();
            assertEquals(epic.getIdNumber(), loadedEpic.getIdNumber());
            assertEquals(epic.getName().orElse(null), loadedEpic.getName().orElse(null));

            Subtask loadedSubtask = state.subtasks().iterator().next();
            assertEquals(subtask.getIdNumber(), loadedSubtask.getIdNumber());
            assertEquals(subtask.getParentEpicIdNumber(), loadedSubtask.getParentEpicIdNumber());
        }

        @Test
        @DisplayName("Повторный save перезаписывает файл, а не дополняет")
        void save_OverwritesFile_Test() {
            //Given
            storage.save(List.of(newTask(1, "task1"), newTask(2, "task2")), List.of(), List.of());
            //When
            storage.save(List.of(newTask(3, "task3")), List.of(), List.of());
            //Then
            LoadedState state = storage.load();
            assertEquals(1, state.tasks().size(), "Должна остаться только одна задача");
            assertEquals(3, state.tasks().iterator().next().getIdNumber());
        }
    }
}