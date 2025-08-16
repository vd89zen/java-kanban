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
    public static void Prepare_For_All_Test() {
        baseTaskNull = new Task();
        baseEpicNull = new Epic();
        baseSubtaskNull = new Subtask();
    }

    @BeforeEach
    public void Prepare_For_Test() {
        inMemoryTaskManager = ManagersUtil.getDefault();

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
    }

    @Nested
    @DisplayName("Тесты методов для работы с Task.")
    class testForTask {
        @Test
        @DisplayName("Проверяем невозможность добавить Task со значением null")
        void addTask_Null_Test() {
            //Given
            expectedResult = ResultOfOperation.ERROR_OBJECT_NULL;
            Task testTask1 = null;
            //When
            actualResult = inMemoryTaskManager.addTask(testTask1);
            //Then
            assertEquals(expectedResult, actualResult);
        }

        @Test
        @DisplayName("Проверяем добавение Task без заданного ID")
        void addTask_Without_IdNumber_Test() {
            //Given
            expectedResult = ResultOfOperation.SUCCESS;
            Task testTask2 = new Task("testTask2", DESCRIPTION, StatusProgress.NEW);
            System.out.println("\nTasks BEFORE test 'Проверяем добавение Task без заданного ID':");
            System.out.println(inMemoryTaskManager.getAllTasks());
            //When
            actualResult = inMemoryTaskManager.addTask(testTask2);
            System.out.println("\nTasks AFTER test 'Проверяем добавение Task без заданного ID':");
            System.out.println(inMemoryTaskManager.getAllTasks());
            //Then
            assertEquals(expectedResult, actualResult);
        }

        @Test
        @DisplayName("Проверяем добавление Task с заданным ID")
        void addTask_With_Set_IdNumber_Test() {
            //Given
            expectedResult = ResultOfOperation.SUCCESS;
            Integer setIdNumber = 888;
            Task testTask3 = new Task("testTask3", DESCRIPTION, StatusProgress.NEW, setIdNumber);
            System.out.println("\nTasks BEFORE test 'Проверяем добавление Task с заданным ID':");
            System.out.println(inMemoryTaskManager.getAllTasks());
            //When
            actualResult = inMemoryTaskManager.addTask(testTask3);
            System.out.println("\nTasks AFTER test 'Проверяем добавление Task с заданным ID':");
            System.out.println(inMemoryTaskManager.getAllTasks());
            //Then
            assertEquals(expectedResult, actualResult);
        }

        @Test
        @DisplayName("Проверяем невозможность добавить Task с ID уже имеющимся в базе Менеджера")
        void addTask_With_IdNumber_Already_In_Manager_Test() {
            //Given
            expectedResult = ResultOfOperation.ERROR_OBJECT_ALREADY_EXISTS;
            Task testTask4 = new Task("testTask4", DESCRIPTION, StatusProgress.NEW, BASE_TASK_1_ID);
            //When
            actualResult = inMemoryTaskManager.addTask(testTask4);
            //Then
            assertEquals(expectedResult, actualResult);
        }

        @Test
        @DisplayName("Проверяем добавление Task c ID ранее удалённой Task")
        void addTask_With_IdNumber_Deleted_Task_Test() {
            //Given
            expectedResult = ResultOfOperation.SUCCESS;
            System.out.println("\nTasks BEFORE test 'Проверяем добавление Task c ID ранее удалённой Task':");
            System.out.println(inMemoryTaskManager.getAllTasks());
            inMemoryTaskManager.removeTaskForIdNumber(BASE_TASK_2_ID);
            System.out.println(inMemoryTaskManager.getAllTasks());
            //When
            Task testTask5 = new Task("testTask5", DESCRIPTION, StatusProgress.NEW, BASE_TASK_2_ID);
            actualResult = inMemoryTaskManager.addTask(testTask5);
            System.out.println("\nTasks AFTER test 'Проверяем добавление Task c ID ранее удалённой Task':");
            System.out.println(inMemoryTaskManager.getAllTasks());
            //Then
            assertEquals(expectedResult, actualResult);
        }

        @Test
        @DisplayName("Проверяем получение существующей Task по ID")
        void getTaskForIdNumber_Exists_Test() {
            //Given
            expectedTask = baseTask1;
            //When
            actualTask = inMemoryTaskManager.getTaskForIdNumber(BASE_TASK_1_ID);
            //Then
            assertEquals(expectedTask, actualTask);
        }

        @Test
        @DisplayName("Проверяем получение Task с полями равными null, если ID не найден в базе Менеджера")
        void getTaskForIdNumber_NotExists_Test() {
            //Given
            expectedTask = baseTaskNull;
            Integer notExistsIdNumber = 666;
            //When
            actualTask = inMemoryTaskManager.getTaskForIdNumber(notExistsIdNumber);
            //Then
            assertEquals(expectedTask, actualTask);
        }

        @Test
        @DisplayName("Проверяем обновление Task")
        void updateTask_Test() {
            //Given
            String expectedName = "testTask6-UPDATED";
            String oldName = "testTask6";
            Task testTask6 = new Task(oldName, DESCRIPTION, StatusProgress.NEW);
            inMemoryTaskManager.addTask(testTask6);
            System.out.println("\nTasks BEFORE test 'Проверяем обновление Task':");
            System.out.println(inMemoryTaskManager.getAllTasks());
            //When
            testTask6.setName(expectedName);
            inMemoryTaskManager.updateTask(testTask6);
            String actualName = inMemoryTaskManager.getTaskForIdNumber(testTask6.getIdNumber()).getName();
            System.out.println("\nTasks AFTER test 'Проверяем обновление Task':");
            System.out.println(inMemoryTaskManager.getAllTasks());
            //Then
            assertEquals(expectedName, actualName);
        }

        @Test
        @DisplayName("Проверяем получение всех Task")
        void getAllTasks_Test() {
            ArrayList<Task> expectedListOfTasks = new ArrayList<>();
            expectedListOfTasks.add(baseTask1);
            expectedListOfTasks.add(baseTask2);
            //When
            ArrayList<Task> actualListOfTasks = inMemoryTaskManager.getAllTasks();
            //Then
            assertEquals(expectedListOfTasks, actualListOfTasks);
        }

        @Test
        @DisplayName("Проверяем удаление всех Task")
        void removeAllTasks_Test() {
            //Given
            Integer expectedSizeOfTasks = 0;
            System.out.println("\nTasks BEFORE test 'Проверяем удаление всех Task':");
            System.out.println(inMemoryTaskManager.getAllTasks());
            //When
            inMemoryTaskManager.removeAllTasks();
            Integer actualSizeOfTasks = inMemoryTaskManager.getAllTasks().size();
            System.out.println("\nTasks AFTER test 'Проверяем удаление всех Task':");
            System.out.println(inMemoryTaskManager.getAllTasks());
            //Then
            assertEquals(expectedSizeOfTasks, actualSizeOfTasks);
        }

        @Test
        @DisplayName("Проверяем, что Task не меняется в Менеджере без вызова метода 'updateTask'")
        void check_Immutability_Task_In_Manager_Without_Update_Test() {
            //Given
            String expectedName = "testTask7";
            String newName = "testTask7-NEW";
            Task testTask7 = new Task(expectedName, DESCRIPTION, StatusProgress.NEW);
            inMemoryTaskManager.addTask(testTask7);
            System.out.println("\nTasks BEFORE test 'Проверяем, что Task не меняется в Менеджере без вызова метода 'updateTask'':");
            System.out.println(inMemoryTaskManager.getAllTasks());
            //When
            testTask7.setName(newName);
            String actualName = inMemoryTaskManager.getTaskForIdNumber(testTask7.getIdNumber()).getName();
            System.out.println("\nTasks AFTER test 'Проверяем, что Task не меняется в Менеджере без вызова метода 'updateTask'':");
            System.out.println(inMemoryTaskManager.getAllTasks());
            //Then
            assertEquals(expectedName, actualName);
        }
    }

    @Nested
    @DisplayName("Тесты методов для работы с Epic.")
    class testForEpic {
        @Test
        @DisplayName("Проверяем добавление Epic без заданного ID")
        void addEpic_Without_IdNumber_Test() {
            //Given
            expectedResult = ResultOfOperation.SUCCESS;
            Epic testEpic1 = new Epic("testEpic1", DESCRIPTION);
            System.out.println("\nEpics BEFORE test 'Проверяем добавление Epic без заданного ID':");
            System.out.println(inMemoryTaskManager.getAllEpics());
            //When
            actualResult = inMemoryTaskManager.addEpic(testEpic1);
            System.out.println("\nEpics AFTER test 'Проверяем добавление Epic без заданного ID':");
            System.out.println(inMemoryTaskManager.getAllEpics());
            //Then
            assertEquals(expectedResult, actualResult);
        }

        @Test
        @DisplayName("Проверяем получение существующего Epic по ID")
        void getEpicForIdNumber_Exists_Test() {
            //Given
            Epic testEpic2 = new Epic("testEpic2", DESCRIPTION);
            inMemoryTaskManager.addEpic(testEpic2);
            expectedEpic = testEpic2;
            //When
            actualEpic = inMemoryTaskManager.getEpicForIdNumber(testEpic2.getIdNumber());
            //Then
            assertEquals(expectedEpic, actualEpic);
        }

        @Test
        @DisplayName("Проверяем получение всех Subtasks из Epic")
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
            System.out.println("\nEpic/Subtasks BEFORE test 'Проверяем изменение статуса Epic после обновления Subtask с измененным статусом':");
            System.out.println(inMemoryTaskManager.getAllEpics());
            System.out.println(inMemoryTaskManager.getAllSubtasks());
            //When
            inMemoryTaskManager.updateSubtask(baseSubtask1);
            StatusProgress actualStatusProgress = inMemoryTaskManager.getEpicForIdNumber(BASE_EPIC_1_ID).getStatusProgress();
            System.out.println("\nEpic/Subtasks AFTER test 'Проверяем изменение статуса Epic после обновления Subtask с измененным статусом':");
            System.out.println(inMemoryTaskManager.getAllEpics());
            System.out.println(inMemoryTaskManager.getAllSubtasks());
            //Then
            assertEquals(expectedStatusProgress, actualStatusProgress, messageIfFail);
        }

        @Test
        @DisplayName("Проверяем получение всех Epic")
        void getAllEpics_Test() {
            ArrayList<Epic> expectedListOfEpics = new ArrayList<>();
            expectedListOfEpics.add(baseEpic1);
            expectedListOfEpics.add(baseEpic2);
            //When
            ArrayList<Epic> actualListOfEpics = inMemoryTaskManager.getAllEpics();
            //Then
            assertEquals(expectedListOfEpics, actualListOfEpics);
        }

        @Test
        @DisplayName("Проверяем удаление всех Epic")
        void removeAllEpics_Test() {
            //Given
            Integer expectedSizeOfEpics = 0;
            System.out.println("\nEpic/Subtasks BEFORE test 'Проверяем удаление всех Epic':");
            System.out.println(inMemoryTaskManager.getAllEpics());
            System.out.println(inMemoryTaskManager.getAllSubtasks());
            //When
            inMemoryTaskManager.removeAllEpics();
            Integer actualSizeOfEpics = inMemoryTaskManager.getAllEpics().size();
            System.out.println("\nEpic/Subtasks AFTER test 'Проверяем удаление всех Epic':");
            System.out.println(inMemoryTaskManager.getAllEpics());
            System.out.println(inMemoryTaskManager.getAllSubtasks());
            //Then
            assertEquals(expectedSizeOfEpics, actualSizeOfEpics);
        }

        @Test
        @DisplayName("Проверяем, что Epic не меняется в Менеджере без вызова метода 'updateEpic'")
        void check_Immutability_Epic_In_Manager_Without_Update_Test() {
            //Given
            String expectedName = "testEpic3";
            String newName = "testEpic3-NEW";
            Epic testEpic3 = new Epic(expectedName, DESCRIPTION);
            inMemoryTaskManager.addEpic(testEpic3);
            System.out.println("\nEpic BEFORE test 'Проверяем, что Epic не меняется в Менеджере без вызова метода 'updateEpic'':");
            System.out.println(inMemoryTaskManager.getAllEpics());
            //When
            testEpic3.setName(newName);
            String actualName = inMemoryTaskManager.getEpicForIdNumber(testEpic3.getIdNumber()).getName();
            System.out.println("\nEpic AFTER test 'Проверяем, что Epic не меняется в Менеджере без вызова метода 'updateEpic'':");
            System.out.println(inMemoryTaskManager.getAllEpics());
            //Then
            assertEquals(expectedName, actualName);
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
        @DisplayName("Проверяем добавление Subtask чей Epic есть в базе Менеджера")
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
            Subtask testSubtask2 = new Subtask(notExistsParentEpicIdNumber, "testSubtask2"
                    , DESCRIPTION, StatusProgress.NEW);
            expectedResult = ResultOfOperation.ERROR_MISMATCH_PARENT_ID;
            //When
            actualResult = inMemoryTaskManager.addSubtask(testSubtask2);
            //Then
            assertEquals(expectedResult, actualResult);
        }

        @Test
        @DisplayName("Проверяем получение существующей Subtask по ID")
        void getSubtaskForIdNumber_Exists_Test() {
            //Given
            expectedSubtask = baseSubtask1;
            //When
            actualSubtask = inMemoryTaskManager.getSubtaskForIdNumber(BASE_SUBTASK_1_ID);
            //Then
            assertEquals(expectedSubtask, actualSubtask);
        }

        @Test
        @DisplayName("Проверяем получение Subtask с полями равными null, если ID не найден в базе Менеджера")
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
        @DisplayName("Проверяем обновление Subtask")
        void updateSubtask_Test() {
            //Given
            StatusProgress expectedStatusProgress = StatusProgress.DONE;
            baseSubtask2.setStatusProgress(StatusProgress.DONE);
            //When
            inMemoryTaskManager.updateSubtask(baseSubtask2);
            StatusProgress actualStatusProgress = inMemoryTaskManager.getSubtaskForIdNumber(BASE_SUBTASK_2_ID).getStatusProgress();
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
            ArrayList<Subtask> actualListOfSubtasks = inMemoryTaskManager.getAllSubtasks();
            //Then
            assertEquals(expectedListOfSubtasks, actualListOfSubtasks);
        }

        @Test
        @DisplayName("Проверяем удаление всех Subtask")
        void removeAllSubtasks_Test() {
            //Given
            Integer expectedSizeOfSubtasks = 0;
            //When
            inMemoryTaskManager.removeAllSubtasks();
            Integer actualSizeOfSubtasks = inMemoryTaskManager.getAllSubtasks().size();
            //Then
            assertEquals(expectedSizeOfSubtasks, actualSizeOfSubtasks);
        }

        @Test
        @DisplayName("Проверяем, что Subtask не меняется в Менеджере без вызова метода 'updateSubtask'")
        void check_Immutability_Subtask_In_Manager_Without_Update_Test() {
            //Given
            String expectedName = "testSubtask2";
            String newName = "testSubtask2-NEW";
            Subtask testSubtask2 = new Subtask(BASE_EPIC_2_ID, expectedName, DESCRIPTION, StatusProgress.NEW);
            inMemoryTaskManager.addSubtask(testSubtask2);
            //When
            testSubtask2.setName(newName);
            String actualName = inMemoryTaskManager.getSubtaskForIdNumber(testSubtask2.getIdNumber()).getName();
            //Then
            assertEquals(expectedName, actualName);
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
}