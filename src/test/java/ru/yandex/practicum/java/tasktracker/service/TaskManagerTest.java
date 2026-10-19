package ru.yandex.practicum.java.tasktracker.service;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ru.yandex.practicum.java.tasktracker.exceptions.NotFoundException;
import ru.yandex.practicum.java.tasktracker.task.*;
import ru.yandex.practicum.java.tasktracker.utils.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.stream.Stream;

import org.junit.jupiter.api.*;
import ru.yandex.practicum.java.tasktracker.utils.enums.ResultOfOperation;
import ru.yandex.practicum.java.tasktracker.utils.enums.StatusProgress;

import static org.junit.jupiter.api.Assertions.*;

class TaskManagerTest {
    protected TaskManager taskManager;
    protected ResultOfOperation actualResult;
    protected ResultOfOperation expectedResult;
    protected Task actualTask;
    protected Task expectedTask;
    protected Subtask actualSubtask;
    protected Subtask expectedSubtask;
    private static final String DESCRIPTION = "description";
    protected Task baseTask1;
    private static final String BASE_TASK_1_NAME = "baseTask1";
    private static final Integer BASE_TASK_1_ID = 1;
    protected Task baseTask2;
    private static final String BASE_TASK_2_NAME = "baseTask2";
    private static final Integer BASE_TASK_2_ID = 3;
    protected Epic baseEpic1;
    private static final String BASE_EPIC_1_NAME = "baseEpic1";
    private static final Integer BASE_EPIC_1_ID = 5;
    protected Epic baseEpic2;
    private static final String BASE_EPIC_2_NAME = "baseEpic2";
    private static final Integer BASE_EPIC_2_ID = 7;
    protected Subtask baseSubtask1;
    private static final String BASE_SUBTASK_1_NAME = "baseSubtask1";
    private static final Integer BASE_SUBTASK_1_ID = 9;
    protected Subtask baseSubtask2;
    private static final String BASE_SUBTASK_2_NAME = "baseSubtask2";
    private static final Integer BASE_SUBTASK_2_ID = 11;
    protected Subtask baseSubtask3;
    private static final String BASE_SUBTASK_3_NAME = "baseSubtask3";
    private static final Integer BASE_SUBTASK_3_ID = 13;
    protected Epic baseEpicWithoutSubtask;
    private static final String BASE_EPIC_WITHOUT_SUBTASK_NAME = "baseEpicWithoutSubtask";
    private static final Integer BASE_EPIC_WITHOUT_SUBTASK_ID = 15;

    @BeforeEach
    public void prepareForTest() {
        taskManager = new TaskManager();
        fillData();
    }

    public void fillData() {
        baseTask1 = new Task(BASE_TASK_1_ID, BASE_TASK_1_NAME, DESCRIPTION, StatusProgress.NEW);
        baseTask2 = new Task(BASE_TASK_2_ID, BASE_TASK_2_NAME, DESCRIPTION, StatusProgress.NEW);
        taskManager.addTask(baseTask1);
        taskManager.addTask(baseTask2);

        baseEpic1 = new Epic(BASE_EPIC_1_ID, BASE_EPIC_1_NAME, DESCRIPTION);
        baseEpic2 = new Epic(BASE_EPIC_2_ID, BASE_EPIC_2_NAME, DESCRIPTION);
        baseEpicWithoutSubtask = new Epic(BASE_EPIC_WITHOUT_SUBTASK_ID, BASE_EPIC_WITHOUT_SUBTASK_NAME, DESCRIPTION);
        taskManager.addEpic(baseEpic1);
        taskManager.addEpic(baseEpic2);
        taskManager.addEpic(baseEpicWithoutSubtask);

        baseSubtask1 = new Subtask(BASE_SUBTASK_1_ID, BASE_SUBTASK_1_NAME, DESCRIPTION, StatusProgress.NEW, BASE_EPIC_1_ID);
        baseSubtask2 = new Subtask(BASE_SUBTASK_2_ID, BASE_SUBTASK_2_NAME, DESCRIPTION, StatusProgress.NEW, BASE_EPIC_2_ID);
        baseSubtask3 = new Subtask(BASE_SUBTASK_3_ID, BASE_SUBTASK_3_NAME, DESCRIPTION, StatusProgress.NEW, BASE_EPIC_2_ID);
        taskManager.addSubtask(baseSubtask1);
        taskManager.addSubtask(baseSubtask2);
        taskManager.addSubtask(baseSubtask3);
    }

    @Nested
    @DisplayName("Тесты методов для работы с Task.")
    class TestForTask {
        @Test
        @DisplayName("Проверяем невозможность добавить Task со значением null")
        void addTask_Null_Test() {
            //Given
            expectedResult = ResultOfOperation.ERROR_OBJECT_NULL;
            Task testTaskNull = null;
            //When
            actualResult = taskManager.addTask(testTaskNull);
            //Then
            assertEquals(expectedResult, actualResult);
        }

        @Test
        @DisplayName("Проверяем добавление Task без заданного ID")
        void addTask_Without_IdNumber_Test() {
            //Given
            expectedResult = ResultOfOperation.SUCCESS;
            Task testTask = new Task("testTask", DESCRIPTION, StatusProgress.NEW);
            //When
            actualResult = taskManager.addTask(testTask);
            //Then
            assertEquals(expectedResult, actualResult);
        }

        @Test
        @DisplayName("Проверяем добавление Task с заданным ID")
        void addTask_With_Set_IdNumber_Test() {
            //Given
            expectedResult = ResultOfOperation.SUCCESS;
            Integer setIdNumber = 888;
            Task testTask888 = new Task(setIdNumber, "testTask888", DESCRIPTION, StatusProgress.NEW);
            //When
            actualResult = taskManager.addTask(testTask888);
            //Then
            assertEquals(expectedResult, actualResult);
        }

        @Test
        @DisplayName("Проверяем невозможность добавить Task с ID уже имеющимся в базе Менеджера")
        void addTask_With_IdNumber_Already_In_Manager_Test() {
            //Given
            expectedResult = ResultOfOperation.ERROR_OBJECT_ALREADY_EXISTS;
            Task testTask4 = new Task(BASE_TASK_1_ID, "testTask4", DESCRIPTION, StatusProgress.NEW);
            //When
            actualResult = taskManager.addTask(testTask4);
            //Then
            assertEquals(expectedResult, actualResult);
        }

        @Test
        @DisplayName("Проверяем добавление Task c ID ранее удалённой Task")
        void addTask_With_IdNumber_Deleted_Task_Test() {
            //Given
            expectedResult = ResultOfOperation.SUCCESS;
            taskManager.removeTaskByIdNumber(BASE_TASK_2_ID);
            //When
            Task testTask5 = new Task(BASE_TASK_2_ID, "testTask5", DESCRIPTION, StatusProgress.NEW);
            actualResult = taskManager.addTask(testTask5);
            //Then
            assertEquals(expectedResult, actualResult);
        }

        @Test
        @DisplayName("Проверяем получение существующей Task по ID")
        void getTaskForIdNumber_Exists_Test() {
            //Given
            expectedTask = baseTask1;
            //When
            actualTask = taskManager.getTaskByIdNumber(BASE_TASK_1_ID);
            //Then
            assertEquals(expectedTask, actualTask);
        }

        @Test
        @DisplayName("Проверяем выброс исключения если Task ID не найден в базе Менеджера")
        void getTaskForIdNumber_NotExists_Test() {
            //Given
            Integer notExistsIdNumber = 666;
            //When, Then
            assertThrows(NotFoundException.class, () -> taskManager.getTaskByIdNumber(notExistsIdNumber));
        }

        @Test
        @DisplayName("Проверяем обновление Task")
        void updateTask_Test() {
            //Given
            String expectedName = "testTaskUPDATED";
            String oldName = "testTask";
            Task testTaskUPD = new Task(oldName, DESCRIPTION, StatusProgress.NEW);
            taskManager.addTask(testTaskUPD);
            testTaskUPD = taskManager.getTaskByIdNumber(testTaskUPD.getIdNumber());
            //When
            testTaskUPD.setName(expectedName);
            taskManager.updateTask(testTaskUPD);
            String actualName = taskManager.getTaskByIdNumber(testTaskUPD.getIdNumber()).getName().get();
            //Then
            assertEquals(expectedName, actualName);
        }

        @Test
        @DisplayName("Проверяем удаление существующей Task по ID")
        void removeTaskForIdNumber_Test() {
            //Given
            //When
            taskManager.removeTaskByIdNumber(BASE_TASK_1_ID);
            //Then
            assertThrows(NotFoundException.class, () -> taskManager.getTaskByIdNumber(BASE_TASK_1_ID));
        }

        @Test
        @DisplayName("Проверяем получение всех Task")
        void getAllTasks_Test() {
            ArrayList<Task> expectedListOfTasks = new ArrayList<>();
            expectedListOfTasks.add(baseTask1);
            expectedListOfTasks.add(baseTask2);
            //When
            ArrayList<Task> actualListOfTasks = taskManager.getAllTasks();
            //Then
            assertEquals(expectedListOfTasks, actualListOfTasks);
        }

        @Test
        @DisplayName("Проверяем удаление всех Task")
        void removeAllTasks_Test() {
            //Given
            int expectedSize = 0;
            //When
            taskManager.removeAllTasks();
            //Then
            int actualSize = taskManager.getAllTasks().size();
            assertEquals(expectedSize, actualSize);
        }

        @Test
        @DisplayName("Проверяем, что Task не меняется в Менеджере без вызова метода 'updateTask'")
        void check_Immutability_Task_In_Manager_Without_Update_Test() {
            //Given
            String expectedName = "testTaskImmutability";
            String newName = "testTaskNEW";
            Task testTaskImmutability = new Task(expectedName, DESCRIPTION, StatusProgress.NEW);
            taskManager.addTask(testTaskImmutability);
            //When
            testTaskImmutability.setName(newName);
            String actualName = taskManager.getTaskByIdNumber(testTaskImmutability.getIdNumber()).getName().get();
            //Then
            assertEquals(expectedName, actualName);
        }

        @Test
        @DisplayName("hashCode двух Task с одинаковыми полями совпадает")
        void task_HashCode_EqualObjects_Test() {
            //Given
            Task t1 = new Task(BASE_TASK_1_ID, "n", DESCRIPTION, StatusProgress.NEW);
            Task t2 = new Task(BASE_TASK_1_ID, "n", DESCRIPTION, StatusProgress.NEW);
            //When
            boolean equalsResult = t1.equals(t2);
            int hashCode1 = t1.hashCode();
            int hashCode2 = t2.hashCode();
            //Then
            assertTrue(equalsResult, "Объекты должны быть равны");
            assertEquals(hashCode1, hashCode2, "У равных объектов hashCode должен совпадать");
        }

        @Test
        @DisplayName("hashCode Task стабилен при повторном вызове")
        void task_HashCode_IsStable_Test() {
            //Given
            Task task = new Task(BASE_TASK_1_ID, "n", DESCRIPTION, StatusProgress.NEW);
            //When
            int first = task.hashCode();
            int second = task.hashCode();
            //Then
            assertEquals(first, second, "hashCode не должен меняться между вызовами");
        }
    }

    @Nested
    @DisplayName("Тесты методов для работы с Epic.")
    class TestForEpic {
        @Test
        @DisplayName("Проверяем добавление Epic без заданного ID")
        void addEpic_Without_IdNumber_Test() {
            //Given
            expectedResult = ResultOfOperation.SUCCESS;
            Epic testEpic = new Epic("testEpic", DESCRIPTION);
            //When
            actualResult = taskManager.addEpic(testEpic);
            //Then
            assertEquals(expectedResult, actualResult);
        }

        @Test
        @DisplayName("Проверяем получение существующего Epic по ID")
        void getEpicForIdNumber_Exists_Test() {
            //Given
            int expectedEpicID = BASE_EPIC_1_ID;
            //When
            int actualEpicID = taskManager.getEpicByIdNumber(BASE_EPIC_1_ID).getIdNumber();
            //Then
            assertEquals(expectedEpicID, actualEpicID);
        }

        @Test
        @DisplayName("Проверяем получение всех Subtasks из Epic")
        void getAllSubtasksFromEpic_Test() {
            //Given
            ArrayList<Subtask> expectedListOfSubtasks = new ArrayList<>();
            expectedListOfSubtasks.add(baseSubtask1);
            //When
            ArrayList<Subtask> actualListOfSubtasks = taskManager.getAllSubtasksFromEpic(BASE_EPIC_1_ID);
            //Then
            assertEquals(expectedListOfSubtasks, actualListOfSubtasks);
        }

        @Test
        @DisplayName("Проверяем изменение статуса Epic после обновления статуса Subtask")
        void epic_Status_Progress_Update_Test() {
            //Given
            StatusProgress expectedStatusProgress = StatusProgress.DONE;
            String messageIfFail = "Если подазадчи DONE, эпик должен быть DONE";
            //When
            baseSubtask1.setStatusProgress(StatusProgress.DONE);
            taskManager.updateSubtask(baseSubtask1);
            StatusProgress actualStatusProgress = taskManager.getEpicByIdNumber(BASE_EPIC_1_ID).getStatusProgress();
            //Then
            assertEquals(expectedStatusProgress, actualStatusProgress, messageIfFail);
        }

        @Test
        @DisplayName("Проверяем удаление существующего Epic по ID")
        void removeEpicForIdNumber_Test() {
            //Given
            //When
            taskManager.removeEpicByIdNumber(BASE_EPIC_WITHOUT_SUBTASK_ID);
            //Then
            assertThrows(NotFoundException.class, () -> taskManager.getEpicByIdNumber(BASE_EPIC_WITHOUT_SUBTASK_ID));
        }

        @Test
        @DisplayName("Проверяем получение всех Epic")
        void getAllEpics_Test() {
            ArrayList<Epic> expectedListOfEpics = new ArrayList<>();
            expectedListOfEpics.add(taskManager.getEpicByIdNumber(BASE_EPIC_1_ID));
            expectedListOfEpics.add(taskManager.getEpicByIdNumber(BASE_EPIC_2_ID));
            expectedListOfEpics.add(taskManager.getEpicByIdNumber(BASE_EPIC_WITHOUT_SUBTASK_ID));
            //When
            ArrayList<Epic> actualListOfEpics = taskManager.getAllEpics();
            //Then
            assertEquals(expectedListOfEpics, actualListOfEpics);
        }

        @Test
        @DisplayName("Проверяем удаление всех Epic (и как следствие Subtask)")
        void removeAllEpics_Test() {
            //Given
            int expectedEpicsSize = 0;
            int expectedSubtasksSize = 0;
            //When
            taskManager.removeAllEpics();
            //Then
            int actualEpicsSize = taskManager.getAllEpics().size();
            int actualSubtasksSize = taskManager.getAllSubtasks().size();
            assertEquals(expectedEpicsSize, actualEpicsSize);
            assertEquals(expectedSubtasksSize, actualSubtasksSize);
        }

        @Test
        @DisplayName("Проверяем обновление Epic Name в Менеджере")
        void updateEpicName_Test() {
            //Given
            String expectedName = "testEpicUPDATE";
            String oldName = "testEpic";
            Epic testEpic = new Epic(oldName, DESCRIPTION);
            taskManager.addEpic(testEpic);
            //When
            taskManager.updateEpicName(testEpic.getIdNumber(), expectedName);
            String actualName = taskManager.getEpicByIdNumber(testEpic.getIdNumber()).getName().get();
            //Then
            assertEquals(expectedName, actualName);
        }

        @Test
        @DisplayName("Проверяем обновление Epic Description в Менеджере")
        void updateEpicDescription_Test() {
            //Given
            String expectedDescription = "Description UPDATE";
            String oldDescription = DESCRIPTION;
            Epic testEpic = new Epic("testEpic", oldDescription);
            taskManager.addEpic(testEpic);
            //When
            taskManager.updateEpicDescription(testEpic.getIdNumber(), expectedDescription);
            String actualDescription = taskManager.getEpicByIdNumber(testEpic.getIdNumber()).getDescription().get();
            //Then
            assertEquals(expectedDescription, actualDescription);
        }

        @Test
        @DisplayName("Проверяем, что Epic Name не меняется в Менеджере без вызова метода 'updateEpicName'")
        void check_Immutability_Epic_Name_In_Manager_Without_Update_Test() {
            //Given
            String expectedName = "testEpicImmutability";
            String newName = "testEpicNEW";
            Epic testEpicNameImmutability = new Epic(expectedName, DESCRIPTION);
            taskManager.addEpic(testEpicNameImmutability);
            //When
            testEpicNameImmutability.setName(newName);
            String actualName = taskManager.getEpicByIdNumber(testEpicNameImmutability.getIdNumber()).getName().get();
            //Then
            assertEquals(expectedName, actualName);
        }

        @Test
        @DisplayName("Проверяем, что Epic Description не меняется в Менеджере без вызова метода 'updateEpicDescription'")
        void check_Immutability_Epic_Description_In_Manager_Without_Update_Test() {
            //Given
            String expectedDescription = DESCRIPTION;
            String newDescription = "NEW Description";
            Epic testEpicDescriptionImmutability = new Epic("testEpicDescriptionImmutability", expectedDescription);
            taskManager.addEpic(testEpicDescriptionImmutability);
            //When
            testEpicDescriptionImmutability.setDescription(newDescription);
            String actualDescription = taskManager.getEpicByIdNumber(testEpicDescriptionImmutability.getIdNumber())
                    .getDescription().get();
            //Then
            assertEquals(expectedDescription, actualDescription);
        }

        @Test
        @DisplayName("Проверяем, что у пустого Epic duration=0, start/end пустые")
        void epic_EmptyEpic_HasZeroDurationAndNoStartEnd_Test() {
            //Given, When
            Epic emptyEpic = taskManager.getEpicByIdNumber(BASE_EPIC_WITHOUT_SUBTASK_ID);
            //Then
            assertEquals(0L, emptyEpic.getDurationInMinutes());
            assertTrue(emptyEpic.getStartDateTime().isEmpty());
            assertTrue(emptyEpic.getEndDateTime().isEmpty());
        }

        @Test
        @DisplayName("Проверяем, что duration Epic равен сумме duration подзадач")
        void epic_Duration_IsSumOfSubtasks_Test() {
            //Given
            LocalDateTime base = LocalDateTime.of(2026, 1, 1, 10, 0);
            Epic epic = new Epic("epic-duration", DESCRIPTION);
            taskManager.addEpic(epic);
            Subtask subtask1 = new Subtask("s1", DESCRIPTION, StatusProgress.NEW,
                    epic.getIdNumber(), 30, base);
            Subtask subtask2 = new Subtask("s2", DESCRIPTION, StatusProgress.NEW,
                    epic.getIdNumber(), 20, base.plusHours(1));
            //When
            taskManager.addSubtask(subtask1);
            taskManager.addSubtask(subtask2);
            Epic fromManager = taskManager.getEpicByIdNumber(epic.getIdNumber());
            //Then
            assertEquals(50L, fromManager.getDurationInMinutes());
        }

        @Test
        @DisplayName("Проверяем, что startDateTime Epic равен минимальному startDateTime подзадач")
        void epic_StartDateTime_IsMinOfSubtasks_Test() {
            //Given
            LocalDateTime base = LocalDateTime.of(2026, 1, 1, 10, 0);
            Epic epic = new Epic("epic-start", DESCRIPTION);
            taskManager.addEpic(epic);
            Subtask early = new Subtask("early", DESCRIPTION, StatusProgress.NEW,
                    epic.getIdNumber(), 10, base);
            Subtask late = new Subtask("late", DESCRIPTION, StatusProgress.NEW,
                    epic.getIdNumber(), 10, base.plusHours(2));
            //When
            taskManager.addSubtask(late);
            taskManager.addSubtask(early);
            Epic fromManager = taskManager.getEpicByIdNumber(epic.getIdNumber());
            //Then
            assertEquals(base, fromManager.getStartDateTime().orElse(null));
        }

        @Test
        @DisplayName("Проверяем, что endDateTime Epic равен максимальному endDateTime подзадач")
        void epic_EndDateTime_IsMaxOfSubtasks_Test() {
            //Given
            LocalDateTime base = LocalDateTime.of(2026, 1, 1, 10, 0);
            Epic epic = new Epic("epic-end", DESCRIPTION);
            taskManager.addEpic(epic);
            Subtask early = new Subtask("early", DESCRIPTION, StatusProgress.NEW,
                    epic.getIdNumber(), 30, base);
            Subtask late = new Subtask("late", DESCRIPTION, StatusProgress.NEW,
                    epic.getIdNumber(), 20, base.plusHours(2));
            //When
            taskManager.addSubtask(early);
            taskManager.addSubtask(late);
            Epic fromManager = taskManager.getEpicByIdNumber(epic.getIdNumber());
            //Then
            assertEquals(base.plusHours(2).plusMinutes(20),
                    fromManager.getEndDateTime().orElse(null));
        }

        @Test
        @DisplayName("Проверяем пересчёт полей Epic при обновлении Subtask")
        void epic_Recalculation_OnUpdateSubtask_Test() {
            //Given
            LocalDateTime base = LocalDateTime.of(2026, 1, 1, 10, 0);
            Epic epic = new Epic("epic-upd", DESCRIPTION);
            taskManager.addEpic(epic);
            Subtask subtask = new Subtask("s", DESCRIPTION, StatusProgress.NEW,
                    epic.getIdNumber(), 10, base);
            taskManager.addSubtask(subtask);
            //When
            subtask.setDurationInMinutes(60);
            subtask.setStartDateTime(base.plusHours(1));
            taskManager.updateSubtask(subtask);
            Epic fromManager = taskManager.getEpicByIdNumber(epic.getIdNumber());
            //Then
            assertEquals(60L, fromManager.getDurationInMinutes());
            assertEquals(base.plusHours(1), fromManager.getStartDateTime().orElse(null));
            assertEquals(base.plusHours(2), fromManager.getEndDateTime().orElse(null));
        }

        @Test
        @DisplayName("Проверяем пересчёт полей Epic при удалении Subtask")
        void epic_Recalculation_OnRemoveSubtask_Test() {
            //Given
            LocalDateTime base = LocalDateTime.of(2026, 1, 1, 10, 0);
            Epic epic = new Epic("epic-rem", DESCRIPTION);
            taskManager.addEpic(epic);
            Subtask early = new Subtask("early", DESCRIPTION, StatusProgress.NEW,
                    epic.getIdNumber(), 30, base);
            Subtask late = new Subtask("late", DESCRIPTION, StatusProgress.NEW,
                    epic.getIdNumber(), 20, base.plusHours(2));
            taskManager.addSubtask(early);
            taskManager.addSubtask(late);
            //When
            taskManager.removeSubtaskByIdNumber(early.getIdNumber());
            Epic fromManager = taskManager.getEpicByIdNumber(epic.getIdNumber());
            //Then
            assertEquals(20L, fromManager.getDurationInMinutes());
            assertEquals(base.plusHours(2), fromManager.getStartDateTime().orElse(null));
            assertEquals(base.plusHours(2).plusMinutes(20),
                    fromManager.getEndDateTime().orElse(null));
        }

        @Test
        @DisplayName("hashCode двух Epic с одинаковыми полями совпадает")
        void epic_HashCode_EqualObjects_Test() {
            //Given
            Epic e1 = new Epic(BASE_EPIC_1_ID, "n", DESCRIPTION);
            Epic e2 = new Epic(BASE_EPIC_1_ID, "n", DESCRIPTION);
            //When
            boolean equalsResult = e1.equals(e2);
            int hashCode1 = e1.hashCode();
            int hashCode2 = e2.hashCode();
            //Then
            assertTrue(equalsResult, "Объекты должны быть равны");
            assertEquals(hashCode1, hashCode2, "У равных объектов hashCode должен совпадать");
        }

        @Test
        @DisplayName("hashCode Epic стабилен при повторном вызове")
        void epic_HashCode_IsStable_Test() {
            //Given
            Epic epic = new Epic(BASE_EPIC_1_ID, "n", DESCRIPTION);
            //When
            int first = epic.hashCode();
            int second = epic.hashCode();
            //Then
            assertEquals(first, second, "hashCode не должен меняться между вызовами");
        }
    }

    @Nested
    @DisplayName("Тесты методов для работы с Subtask.")
    class TestForSubtask {
        @Test
        @DisplayName("Проверяем невозможность добавить Subtask уже имеющийся в базе Менеджера")
        void addSubtask_Already_In_Manager_Test() {
            //Given
            expectedResult = ResultOfOperation.ERROR_OBJECT_ALREADY_EXISTS;
            //When
            actualResult = taskManager.addSubtask(baseSubtask3);
            //Then
            assertEquals(expectedResult, actualResult);
        }

        @Test
        @DisplayName("Проверяем добавление Subtask чей Epic есть в базе Менеджера")
        void addSubtask_New_To_Exists_Epic_Test() {
            //Given
            Subtask testSubtaskEpic2 = new Subtask("testSubtaskEpic2", DESCRIPTION, StatusProgress.NEW, BASE_EPIC_2_ID);
            expectedResult = ResultOfOperation.SUCCESS;
            //When
            actualResult = taskManager.addSubtask(testSubtaskEpic2);
            //Then
            assertEquals(expectedResult, actualResult);
        }

        @Test
        @DisplayName("Проверяем невозможность добавить Subtask чьего Epic нет в базе Менеджера")
        void addSubtask_New_To_Not_Exists_Epic_Test() {
            //Given
            Integer notExistsParentEpicIdNumber = 666;
            Subtask testSubtaskEpic666 = new Subtask("testSubtaskEpic666", DESCRIPTION, StatusProgress.NEW,
                    notExistsParentEpicIdNumber);
            expectedResult = ResultOfOperation.ERROR_MISMATCH_PARENT_ID;
            //When
            actualResult = taskManager.addSubtask(testSubtaskEpic666);
            //Then
            assertEquals(expectedResult, actualResult);
        }

        @Test
        @DisplayName("Проверяем получение существующей Subtask по ID")
        void getSubtaskForIdNumber_Exists_Test() {
            //Given
            expectedSubtask = baseSubtask1;
            //When
            actualSubtask = taskManager.getSubtaskByIdNumber(BASE_SUBTASK_1_ID);
            //Then
            assertEquals(expectedSubtask, actualSubtask);
        }

        @Test
        @DisplayName("Проверяем удаление существующей Subtask по ID")
        void removeSubtaskForIdNumber_Test() {
            //When
            taskManager.removeSubtaskByIdNumber(BASE_SUBTASK_1_ID);
            //Then
            assertThrows(RuntimeException.class, () -> taskManager.getSubtaskByIdNumber(BASE_SUBTASK_1_ID));
        }

        @Test
        @DisplayName("Проверяем выброс исключения если Subtask ID не найден в базе Менеджера")
        void getSubtaskForIdNumber_Not_Exists_Test() {
            //Given
            Integer notExistsIdNumber = 666;
            //When, Then
            assertThrows(NotFoundException.class, () -> taskManager.getSubtaskByIdNumber(notExistsIdNumber));
        }

        @Test
        @DisplayName("Проверяем обновление Subtask")
        void updateSubtask_Test() {
            //Given
            StatusProgress expectedStatusProgress = StatusProgress.DONE;
            baseSubtask2.setStatusProgress(StatusProgress.DONE);
            //When
            taskManager.updateSubtask(baseSubtask2);
            StatusProgress actualStatusProgress = taskManager.getSubtaskByIdNumber(BASE_SUBTASK_2_ID).getStatusProgress();
            //Then
            assertEquals(expectedStatusProgress, actualStatusProgress);
        }

        @Test
        @DisplayName("Проверяем получение всех Subtask")
        void getAllSubtasks_Test() {
            ArrayList<Subtask> expectedListOfSubtasks = new ArrayList<>();
            expectedListOfSubtasks.add(baseSubtask1);
            expectedListOfSubtasks.add(baseSubtask2);
            expectedListOfSubtasks.add(baseSubtask3);
            //When
            ArrayList<Subtask> actualListOfSubtasks = taskManager.getAllSubtasks();
            //Then
            assertEquals(expectedListOfSubtasks, actualListOfSubtasks);
        }

        @Test
        @DisplayName("Проверяем удаление всех Subtask")
        void removeAllSubtasks_Test() {
            //Given
            int expectedSize = 0;
            //When
            taskManager.removeAllSubtasks();
            //Then
            int actualSize = taskManager.getAllSubtasks().size();
            assertEquals(expectedSize, actualSize);
        }

        @Test
        @DisplayName("Проверяем, что Subtask не меняется в Менеджере без вызова метода 'updateSubtask'")
        void check_Immutability_Subtask_In_Manager_Without_Update_Test() {
            //Given
            String expectedName = "testSubtaskImmutability";
            String newName = "testSubtaskNEW";
            Subtask testSubtaskImmutability = new Subtask(expectedName, DESCRIPTION, StatusProgress.NEW, BASE_EPIC_2_ID);
            taskManager.addSubtask(testSubtaskImmutability);
            //When
            testSubtaskImmutability.setName(newName);
            String actualName = taskManager.getSubtaskByIdNumber(testSubtaskImmutability.getIdNumber()).getName().get();
            //Then
            assertEquals(expectedName, actualName);
        }

        @Test
        @DisplayName("hashCode двух Subtask с одинаковыми полями совпадает")
        void subtask_HashCode_EqualObjects_Test() {
            //Given
            Subtask s1 = new Subtask(BASE_SUBTASK_1_ID, "n", DESCRIPTION, StatusProgress.NEW,
                    BASE_EPIC_1_ID);
            Subtask s2 = new Subtask(BASE_SUBTASK_1_ID, "n", DESCRIPTION, StatusProgress.NEW,
                    BASE_EPIC_1_ID);
            //When
            boolean equalsResult = s1.equals(s2);
            int hashCode1 = s1.hashCode();
            int hashCode2 = s2.hashCode();
            //Then
            assertTrue(equalsResult, "Объекты должны быть равны");
            assertEquals(hashCode1, hashCode2, "У равных объектов hashCode должен совпадать");
        }

        @Test
        @DisplayName("hashCode Subtask стабилен при повторном вызове")
        void subtask_HashCode_IsStable_Test() {
            //Given
            Subtask subtask = new Subtask(BASE_SUBTASK_1_ID, "n", DESCRIPTION, StatusProgress.NEW,
                    BASE_EPIC_1_ID);
            //When
            int first = subtask.hashCode();
            int second = subtask.hashCode();
            //Then
            assertEquals(first, second, "hashCode не должен меняться между вызовами");
        }
    }

    @Test
    @DisplayName("Проверяем получение количества всех ID Task/Epic/Subtask находящихся в Менеджере")
    void getTotalOfIdInWork_Test() {
        //Given
        int expectedNumber = 8;
        //When
        int actualNumber = taskManager.getTotalOfIdInWork();
        //Then
        assertEquals(expectedNumber, actualNumber);
    }

    @Test
    @DisplayName("Проверяем получение истории")
    void getHistory_Test() {
        //Given
        ArrayList<AbstractTask> expectedHistory = new ArrayList<>();
        expectedHistory.add(taskManager.getTaskByIdNumber(BASE_TASK_2_ID));
        expectedHistory.add(taskManager.getEpicByIdNumber(BASE_EPIC_2_ID));
        expectedHistory.add(taskManager.getSubtaskByIdNumber(BASE_SUBTASK_2_ID));
        //When
         ArrayList<AbstractTask> actualHistory = taskManager.getHistory();
        //Then
        assertEquals(expectedHistory, actualHistory);
    }

    @Test
    @DisplayName("Проверяем получение списка приоритетных задач")
    void getPrioritizedTasks_Test() {
        //Given
        ArrayList<AbstractTask> expectedList = new ArrayList<>();
        LocalDateTime base = LocalDateTime.of(2026, 1, 1, 10, 0);
        baseTask1.setDurationInMinutes(10);
        baseTask1.setStartDateTime(base.plusHours(3));
        baseSubtask2.setDurationInMinutes(10);
        baseSubtask2.setStartDateTime(base.plusHours(2));
        baseSubtask3.setDurationInMinutes(10);
        baseSubtask3.setStartDateTime(base.plusHours(1));
        expectedList.add(baseSubtask3);
        expectedList.add(baseSubtask2);
        expectedList.add(baseTask1);
        taskManager.updateTask(baseTask1);
        taskManager.updateSubtask(baseSubtask2);
        taskManager.updateSubtask(baseSubtask3);
        //When
        ArrayList<AbstractTask> actualList = taskManager.getPrioritizedTasks();
        //Then
        assertEquals(expectedList, actualList);
    }

    static Stream<Arguments> intersectCases() {
        LocalDateTime base = LocalDateTime.of(2026, 1, 1, 10, 0);
        return Stream.of(
                Arguments.of("полное совпадение",
                        base, 60L, base, 60L, true),
                Arguments.of("t1 внутри t2",
                        base.plusMinutes(15), 15L, base, 60L, true),
                Arguments.of("t2 внутри t1",
                        base, 60L, base.plusMinutes(15), 15L, true),
                Arguments.of("пересечение в начале",
                        base, 30L, base.plusMinutes(15), 45L, true),
                Arguments.of("пересечение в конце",
                        base.plusMinutes(30), 30L, base, 45L, true),
                Arguments.of("касание: t1.end == t2.start",
                        base, 30L, base.plusMinutes(30), 30L, true),
                Arguments.of("касание: t2.end == t1.start",
                        base.plusMinutes(30), 30L, base, 30L, true),
                Arguments.of("t1 полностью до t2",
                        base, 30L, base.plusHours(1), 30L, false),
                Arguments.of("t2 полностью до t1",
                        base.plusHours(1), 30L, base, 30L, false),
                Arguments.of("зазор 1 минута",
                        base, 59L, base.plusHours(1), 30L, false)
        );
    }

    @ParameterizedTest(name = "[{index}] {0} -> {5}")
    @MethodSource("intersectCases")
    @DisplayName("isTimeIntersectBoth возвращает ожидаемый результат для разных интервалов")
    void isTimeIntersectBoth_Cases_Test(String description,
                                        LocalDateTime start1, long duration1,
                                        LocalDateTime start2, long duration2,
                                        boolean expected) {
        //Given
        Task t1 = new Task("t1", "d", StatusProgress.NEW, duration1, start1);
        Task t2 = new Task("t2", "d", StatusProgress.NEW, duration2, start2);
        //When
        boolean actual = taskManager.isTimeIntersectBoth(t1, t2);
        //Then
        assertEquals(expected, actual, description);
    }

    @Test
    @DisplayName("isTimeIntersectBoth с null или без startDateTime возвращает false")
    void isTimeIntersectBoth_NullOrNoStart_ReturnsFalse_Test() {
        //Given
        LocalDateTime base = LocalDateTime.of(2026, 1, 1, 10, 0);
        Task withTime = new Task("t", "d", StatusProgress.NEW, 60, base);
        Task withoutTime = new Task("t", "d", StatusProgress.NEW);
        //When, Then
        assertAll(
                () -> assertFalse(taskManager.isTimeIntersectBoth(null, withTime)),
                () -> assertFalse(taskManager.isTimeIntersectBoth(withTime, null)),
                () -> assertFalse(taskManager.isTimeIntersectBoth(withoutTime, withTime)),
                () -> assertFalse(taskManager.isTimeIntersectBoth(withTime, withoutTime))
        );
    }

    @Test
    @DisplayName("Проверяем пересечение задачи по времени с приоритетными задачами")
    void isTimeIntersectWithOthers_Test() {
        //Given, expected true
        LocalDateTime base = LocalDateTime.of(2026, 1, 1, 10, 0);
        baseTask1.setDurationInMinutes(10);
        baseTask1.setStartDateTime(base.plusHours(3));
        baseSubtask2.setDurationInMinutes(10);
        baseSubtask2.setStartDateTime(base.plusHours(2));
        baseSubtask3.setDurationInMinutes(10);
        baseSubtask3.setStartDateTime(base.plusHours(1));
        taskManager.updateTask(baseTask1);
        taskManager.updateSubtask(baseSubtask2);
        taskManager.updateSubtask(baseSubtask3);
        baseTask2.setDurationInMinutes(10);
        baseTask2.setStartDateTime(base.plusHours(1).plusMinutes(5));
        //When, Then
        assertTrue(taskManager.isTimeIntersectWithOthers(baseTask2));
    }

    @Test
    @DisplayName("Проверяем замену задачи (с новым временем) в приоритетных задачах")
    void check_Replace_Task_In_Prioritized_With_New_Time_Test() {
        //Given
        LocalDateTime base = LocalDateTime.of(2026, 1, 1, 10, 0);
        LocalDateTime oldStart = base.plusHours(2);
        LocalDateTime newStart = base.plusHours(2).plusMinutes(5);
        baseTask1.setDurationInMinutes(10);
        baseTask1.setStartDateTime(base.plusHours(1));
        baseTask2.setDurationInMinutes(10);
        baseTask2.setStartDateTime(oldStart);
        taskManager.updateTask(baseTask1);
        taskManager.updateTask(baseTask2);
        //When
        baseTask2.setStartDateTime(newStart);
        taskManager.updateTask(baseTask2);
        //Then
        ArrayList<AbstractTask> prioritized = taskManager.getPrioritizedTasks();
        AbstractTask actual = prioritized.stream()
                .filter(task -> task.getIdNumber() == BASE_TASK_2_ID)
                .findFirst()
                .orElseThrow(() -> new AssertionError("Задача с id=" + BASE_TASK_2_ID + " не найдена в prioritizedTasks"));
        assertEquals(newStart, actual.getStartDateTime().orElse(null),
                "Задача должна быть с обновлённым startDateTime");
    }

}
