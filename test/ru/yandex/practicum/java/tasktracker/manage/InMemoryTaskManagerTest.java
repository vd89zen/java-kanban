package ru.yandex.practicum.java.tasktracker.manage;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import ru.yandex.practicum.java.tasktracker.task.*;
import java.util.ArrayList;

@DisplayName("Тесты для InMemoryTaskManager. ID - idNumber, уникальный идентификатор")
class InMemoryTaskManagerTest {
    private static TaskManager inMemoryTaskManager;
    private static ResultOfOperation actualResult;
    private static ResultOfOperation expectedResult;
    private static Task actualTask;
    private static Task expectedTask;
    private static Epic actualEpic;
    private static Epic expectedEpic;
    private static Subtask actualSubtask;
    private static Subtask expectedSubtask;
    private static Task baseTaskNull;
    private static Epic baseEpicNull;
    private static Subtask baseSubtaskNull;
    private static final String DESCRIPTION = "description";
    private static Task baseTask1;
    private static final String BASE_TASK_1_NAME = "baseTask1";
    private static final Integer BASE_TASK_1_ID = 1;
    private static Task baseTask2;
    private static final String BASE_TASK_2_NAME = "baseTask2";
    private static final Integer BASE_TASK_2_ID = 3;
    private static Epic baseEpic1;
    private static final String BASE_EPIC_1_NAME = "baseEpic1";
    private static final Integer BASE_EPIC_1_ID = 5;
    private static Epic baseEpic2;
    private static final String BASE_EPIC_2_NAME = "baseEpic2";
    private static final Integer BASE_EPIC_2_ID = 7;
    private static Subtask baseSubtask1;
    private static final String BASE_SUBTASK_1_NAME = "baseSubtask1";
    private static final Integer BASE_SUBTASK_1_ID = 9;
    private static Subtask baseSubtask2;
    private static final String BASE_SUBTASK_2_NAME = "baseSubtask2";
    private static final Integer BASE_SUBTASK_2_ID = 11;
    private static Subtask baseSubtask3;
    private static final String BASE_SUBTASK_3_NAME = "baseSubtask3";
    private static final Integer BASE_SUBTASK_3_ID = 13;

    @BeforeAll
    public static void Prepare_For_Test() {
        inMemoryTaskManager = ManagersUtil.getDefault();

        baseTaskNull = new Task();
        baseEpicNull = new Epic();
        baseSubtaskNull = new Subtask();

        baseTask1 = new Task(BASE_TASK_1_NAME, DESCRIPTION, StatusProgress.NEW, BASE_TASK_1_ID);
        baseTask2 = new Task(BASE_TASK_2_NAME, DESCRIPTION, StatusProgress.NEW, BASE_TASK_2_ID);
        inMemoryTaskManager.addTask(baseTask1);
        inMemoryTaskManager.addTask(baseTask2);

        baseEpic1 = new Epic(BASE_EPIC_1_NAME, DESCRIPTION, BASE_EPIC_1_ID);
        baseEpic2 = new Epic(BASE_EPIC_2_NAME, DESCRIPTION, BASE_EPIC_2_ID);
        inMemoryTaskManager.addEpic(baseEpic1);
        inMemoryTaskManager.addEpic(baseEpic2);

        baseSubtask1 = new Subtask(BASE_EPIC_1_ID,BASE_SUBTASK_1_NAME, DESCRIPTION, StatusProgress.NEW, BASE_SUBTASK_1_ID);
        baseSubtask2 = new Subtask(BASE_EPIC_2_ID,BASE_SUBTASK_2_NAME, DESCRIPTION, StatusProgress.NEW, BASE_SUBTASK_2_ID);
        baseSubtask3 = new Subtask(BASE_EPIC_2_ID,BASE_SUBTASK_3_NAME, DESCRIPTION, StatusProgress.NEW, BASE_SUBTASK_3_ID);
        inMemoryTaskManager.addSubtask(baseSubtask1);
        inMemoryTaskManager.addSubtask(baseSubtask2);
        inMemoryTaskManager.addSubtask(baseSubtask3);

        System.out.println("Tasks/Epic/Subtask (base) before test: ");
        System.out.println(inMemoryTaskManager.getAllTasks());
        System.out.println(inMemoryTaskManager.getAllEpics());
        System.out.println(inMemoryTaskManager.getAllSubtasks());
    }

    @AfterAll
    public static void Print_All() {
        System.out.println("\nTasks/Epic/Subtask (all) after test: ");
        System.out.println(inMemoryTaskManager.getAllTasks());
        System.out.println(inMemoryTaskManager.getAllEpics());
        System.out.println(inMemoryTaskManager.getAllSubtasks());
        System.out.println("\n" + "History from inMemoryTaskManager: " + inMemoryTaskManager.getHistory());
    }

    @Nested
    @DisplayName("Тесты методов для работы с Task.")
    class testForTask {
        @Test
        @DisplayName("Проверяем невозможность добавить Task со значением null")
        void addTask_Null_Test() {
            //Given
            Task testTask1 = null;
            expectedResult = ResultOfOperation.ERROR_OBJECT_NULL;
            //When
            actualResult = inMemoryTaskManager.addTask(testTask1);
            //Then
            assertEquals(expectedResult, actualResult);
        }

        @Test
        @DisplayName("Добавляем Task без заданного ID")
        void addTask_Without_IdNumber_Test() {
            //Given
            Task testTask2 = new Task("testTask2", DESCRIPTION, StatusProgress.NEW);
            expectedResult = ResultOfOperation.SUCCESS;
            //When
            actualResult = inMemoryTaskManager.addTask(testTask2);
            //Then
            assertEquals(expectedResult, actualResult);
        }

        @Test
        @DisplayName("Добавляем Task с заданным ID")
        void addTask_With_Set_IdNumber_Test() {
            //Given
            Integer setIdNumber = 888;
            Task testTask3 = new Task("testTask3", DESCRIPTION, StatusProgress.NEW, setIdNumber);
            expectedResult = ResultOfOperation.SUCCESS;
            //When
            actualResult = inMemoryTaskManager.addTask(testTask3);
            //Then
            assertEquals(expectedResult, actualResult);
        }

        @Test
        @DisplayName("Проверяем невозможность добавить Task с ID уже имеющимся в базе Менеджера")
        void addTask_With_IdNumber_Already_In_Manager_Test() {
            //Given
            Task testTask4 = new Task("testTask4", DESCRIPTION, StatusProgress.NEW, BASE_TASK_1_ID);
            expectedResult = ResultOfOperation.ERROR_OBJECT_ALREADY_EXISTS;
            //When
            actualResult = inMemoryTaskManager.addTask(testTask4);
            //Then
            assertEquals(expectedResult, actualResult);
        }

        @Test
        @DisplayName("Получаем существующую Task по ID")
        void getTaskForIdNumber_Exists_Test() {
            //Given
            expectedTask = baseTask1;
            //When
            actualTask = inMemoryTaskManager.getTaskForIdNumber(BASE_TASK_1_ID);
            //Then
            assertEquals(expectedTask, actualTask);
        }

        @Test
        @DisplayName("Получаем Task с полями равными null, если ID не найден в базе Менеджера")
        void getTaskForIdNumber_NotExists_Test() {
            //Given
            Integer notExistsIdNumber = 666;
            expectedTask = baseTaskNull;
            //When
            actualTask = inMemoryTaskManager.getTaskForIdNumber(notExistsIdNumber);
            //Then
            assertEquals(expectedTask, actualTask);
        }
    }

    @Nested
    @DisplayName("Тесты методов для работы с Epic.")
    class testForEpic {
        @Test
        @DisplayName("Добавляем Epic без заданного ID")
        void addEpic_Without_IdNumber_Test() {
            //Given
            Epic testEpic1 = new Epic("testEpic1", DESCRIPTION);
            expectedResult = inMemoryTaskManager.addEpic(testEpic1);
            //When
            actualResult = ResultOfOperation.SUCCESS;
            //Then
            assertEquals(expectedResult, actualResult);
        }

        @Test
        @DisplayName("Получаем существующий Epic по ID")
        void getEpicForIdNumber_Exists_Test() {
            //Given
            expectedEpic = baseEpic1;
            //When
            actualEpic = inMemoryTaskManager.getEpicForIdNumber(BASE_EPIC_1_ID);
            //Then
            assertEquals(expectedEpic, actualEpic);
        }

        @Test
        @DisplayName("Получаем все Subtasks из Epic")
        void getAllSubtasksFromEpic_Test() {
            //Given
            ArrayList<Subtask> expectedListOfSubtasks = new ArrayList<>();
            expectedListOfSubtasks.add(baseSubtask1);
            //When
            ArrayList<Subtask> actualListOfSubtasks = inMemoryTaskManager.getAllSubtasksFromEpic(BASE_EPIC_1_ID);
            //Then
            assertEquals(expectedListOfSubtasks, actualListOfSubtasks);
        }

        @Test
        @DisplayName("Проверяем изменение статуса Epic после обновления Subtask с измененным статусом")
        void epic_Status_Progress_Update_Test(){
            //Given
            StatusProgress expectedStatusProgress = StatusProgress.DONE;
            baseSubtask1.setStatusProgress(StatusProgress.DONE);
            String messageIfFail = "Если подазадчи DONE, эпик должен быть DONE";
            //When
            inMemoryTaskManager.updateSubtask(baseSubtask1);
            //Then
            StatusProgress actualStatusProgress = baseEpic1.getStatusProgress();
            assertEquals(expectedStatusProgress, actualStatusProgress, messageIfFail);
        }
    }

    @Nested
    @DisplayName("Тесты методов для работы с Subtask.")
    class testForSubtask {
        @Test
        @DisplayName("Проверяем невозможность добавить Subtask уже имеющийся в базе Менеджера")
        void addSubtask_Already_In_Manager_Test() {
            //Given
            expectedResult = ResultOfOperation.ERROR_OBJECT_ALREADY_EXISTS;
            //When
            actualResult = inMemoryTaskManager.addSubtask(baseSubtask3);
            //Then
            assertEquals(expectedResult, actualResult);
        }

        @Test
        @DisplayName("Добавляем Subtask чей Epic есть в базе Менеджера")
        void addSubtask_New_To_Exists_Epic_Test() {
            //Given
            Subtask testSubtask1 = new Subtask(BASE_EPIC_2_ID,"testSubtask1", DESCRIPTION, StatusProgress.NEW);
            expectedResult = ResultOfOperation.SUCCESS;
            //When
            actualResult = inMemoryTaskManager.addSubtask(testSubtask1);
            //Then
            assertEquals(expectedResult, actualResult);
        }

        @Test
        @DisplayName("Проверяем невозможность добавить Subtask чьего Epic нет в базе Менеджера")
        void addSubtask_New_To_Not_Exists_Epic_Test() {
            //Given
            Integer notExistsParentEpicIdNumber = 666;
            Subtask testSubtask2 = new Subtask(notExistsParentEpicIdNumber, "testSubtask2", DESCRIPTION, StatusProgress.NEW);
            expectedResult = ResultOfOperation.ERROR_MISMATCH_PARENT_ID;
            //When
            actualResult = inMemoryTaskManager.addSubtask(testSubtask2);
            //Then
            assertEquals(expectedResult, actualResult);
        }

        @Test
        @DisplayName("Получаем существующий Subtask по ID")
        void getSubtaskForIdNumber_Exists_Test() {
            //Given
            expectedSubtask = baseSubtask1;
            //When
            actualSubtask = inMemoryTaskManager.getSubtaskForIdNumber(BASE_SUBTASK_1_ID);
            //Then
            assertEquals(expectedSubtask, actualSubtask);
        }

        @Test
        @DisplayName("Получаем Subtask с полями равными null, если ID не найден в базе Менеджера")
        void getSubtaskForIdNumber_Not_Exists_Test() {
            //Given
            Integer notExistsIdNumber = 666;
            expectedSubtask = baseSubtaskNull;
            //When
            actualSubtask = inMemoryTaskManager.getSubtaskForIdNumber(notExistsIdNumber);
            //Then
            assertEquals(expectedSubtask, actualSubtask);
        }

        @Test
        @DisplayName("Обновляем Subtask после изменения")
        void updateSubtask_Test() {
            //Given
            baseSubtask2.setStatusProgress(StatusProgress.DONE);
            expectedResult = ResultOfOperation.SUCCESS;
            //When
            actualResult = inMemoryTaskManager.updateSubtask(baseSubtask2);
            //Then
            assertEquals(expectedResult, actualResult);
        }
    }

    @Test
    @DisplayName("Проверяем фиксацию истории обращений по ID к Task/Epic/Subtask")
    void getHistory_Test() {
        //Given
        boolean isHistoryMoreThanZeroExpected = true;
        inMemoryTaskManager.getTaskForIdNumber(baseTask2.getIdNumber());
        inMemoryTaskManager.getEpicForIdNumber(baseEpic2.getIdNumber());
        inMemoryTaskManager.getSubtaskForIdNumber(baseSubtask2.getIdNumber());
        String messageIfFail = "История не может быть пустой после обращения к задачам по idNumber";
        //When
        boolean isHistoryMoreThanZeroActual = inMemoryTaskManager.getHistory().size() > 0;
        //Then
        assertEquals(isHistoryMoreThanZeroExpected, isHistoryMoreThanZeroActual, messageIfFail);
    }

    @Test
    @DisplayName("Проверяем сохранение в истории повторных просмотров")
    void getHistory_Checking_Saving_Repeat_Views_Test() {
        //Given
        TaskManager inMemoryTaskManager2 = ManagersUtil.getDefault();
        Task testTask5 = new Task("testTask5", DESCRIPTION, StatusProgress.NEW);
        inMemoryTaskManager2.addTask(testTask5);
        inMemoryTaskManager2.getTaskForIdNumber(testTask5.getIdNumber());
        testTask5.setStatusProgress(StatusProgress.DONE);
        inMemoryTaskManager2.updateTask(testTask5);
        AbstractTask firstView = inMemoryTaskManager2.getHistory().getFirst();
        StatusProgress expectedStatusProgress = firstView.getStatusProgress();
        //When
        inMemoryTaskManager2.getTaskForIdNumber(testTask5.getIdNumber());
        //Then
        System.out.println("\n" + "History from inMemoryTaskManager2: " + inMemoryTaskManager2.getHistory());
        AbstractTask secondView = inMemoryTaskManager2.getHistory().getLast();
        StatusProgress actualStatusProgress = secondView.getStatusProgress();
        assertNotEquals(expectedStatusProgress, actualStatusProgress);
    }
}