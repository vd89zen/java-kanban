package ru.yandex.practicum.java.tasktracker.manage;

import ru.yandex.practicum.java.tasktracker.exceptions.NotFoundException;
import ru.yandex.practicum.java.tasktracker.task.*;
import ru.yandex.practicum.java.tasktracker.utils.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import org.junit.jupiter.api.*;
import ru.yandex.practicum.java.tasktracker.utils.enums.ResultOfOperation;
import ru.yandex.practicum.java.tasktracker.utils.enums.StatusProgress;
import ru.yandex.practicum.java.tasktracker.service.interfaces.TaskManager;
import static org.junit.jupiter.api.Assertions.*;

abstract class TaskManagerTest<T extends TaskManager> {
    protected TaskManager taskManager;
    protected ResultOfOperation actualResult;
    protected ResultOfOperation expectedResult;
    protected Task actualTask;
    protected Task expectedTask;
    protected Subtask actualSubtask;
    protected Subtask expectedSubtask;
    protected final String DESCRIPTION = "description";
    protected Task baseTask1;
    protected final String BASE_TASK_1_NAME = "baseTask1";
    protected final Integer BASE_TASK_1_ID = 1;
    protected Task baseTask2;
    protected final String BASE_TASK_2_NAME = "baseTask2";
    protected final Integer BASE_TASK_2_ID = 3;
    protected Epic baseEpic1;
    protected final String BASE_EPIC_1_NAME = "baseEpic1";
    protected final Integer BASE_EPIC_1_ID = 5;
    protected Epic baseEpic2;
    protected final String BASE_EPIC_2_NAME = "baseEpic2";
    protected final Integer BASE_EPIC_2_ID = 7;
    protected Subtask baseSubtask1;
    protected final String BASE_SUBTASK_1_NAME = "baseSubtask1";
    protected final Integer BASE_SUBTASK_1_ID = 9;
    protected Subtask baseSubtask2;
    protected final String BASE_SUBTASK_2_NAME = "baseSubtask2";
    protected final Integer BASE_SUBTASK_2_ID = 11;
    protected Subtask baseSubtask3;
    protected final String BASE_SUBTASK_3_NAME = "baseSubtask3";
    protected final Integer BASE_SUBTASK_3_ID = 13;
    protected Epic baseEpicWithoutSubtask;
    protected final String BASE_EPIC_WITHOUT_SUBTASK_NAME = "baseEpicWithoutSubtask";
    protected final Integer BASE_EPIC_WITHOUT_SUBTASK_ID = 15;

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
    class testForTask {
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
            System.out.println("\nTasks BEFORE test 'Проверяем добавение Task без заданного ID':");
            System.out.println(taskManager.getAllTasks());
            //When
            actualResult = taskManager.addTask(testTask);
            System.out.println("\nTasks AFTER test 'Проверяем добавение Task без заданного ID':");
            System.out.println(taskManager.getAllTasks());
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
            System.out.println("\nTasks BEFORE test 'Проверяем добавление Task с заданным ID':");
            System.out.println(taskManager.getAllTasks());
            //When
            actualResult = taskManager.addTask(testTask888);
            System.out.println("\nTasks AFTER test 'Проверяем добавление Task с заданным ID':");
            System.out.println(taskManager.getAllTasks());
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
            System.out.println("\nTasks BEFORE test 'Проверяем добавление Task c ID ранее удалённой Task':");
            System.out.println(taskManager.getAllTasks());
            System.out.println("taskManager.removeTaskForIdNumber(BASE_TASK_2_ID): " +
                    taskManager.removeTaskByIdNumber(BASE_TASK_2_ID));
            System.out.println(taskManager.getAllTasks());
            //When
            Task testTask5 = new Task(BASE_TASK_2_ID, "testTask5", DESCRIPTION, StatusProgress.NEW);
            actualResult = taskManager.addTask(testTask5);
            System.out.println("\nTasks AFTER test 'Проверяем добавление Task c ID ранее удалённой Task':");
            System.out.println(taskManager.getAllTasks());
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
            assertThrows(RuntimeException.class, () -> taskManager.getTaskByIdNumber(notExistsIdNumber));
        }

        @Test
        @DisplayName("Проверяем обновление Task")
        void updateTask_Test() {
            //Given
            String expectedName = "testTaskUPDATED";
            String oldName = "testTask";
            Task testTaskUPD = new Task(oldName, DESCRIPTION, StatusProgress.NEW);
            taskManager.addTask(testTaskUPD);
            System.out.println("\nTasks BEFORE test 'Проверяем обновление Task':");
            System.out.println(taskManager.getAllTasks());
            testTaskUPD = taskManager.getTaskByIdNumber(testTaskUPD.getIdNumber());
            //When
            testTaskUPD.setName(expectedName);
            taskManager.updateTask(testTaskUPD);
            String actualName = taskManager.getTaskByIdNumber(testTaskUPD.getIdNumber()).getName().get();
            System.out.println("\nTasks AFTER test 'Проверяем обновление Task':");
            System.out.println(taskManager.getAllTasks());
            //Then
            assertEquals(expectedName, actualName);
        }

        @Test
        @DisplayName("Проверяем удаление существующей Task по ID")
        void removeTaskForIdNumber_Test() {
            //Given
            System.out.println("\nTasks BEFORE test 'Проверяем удаление существующей Task по ID':");
            System.out.println(taskManager.getAllTasks());
            //When
            taskManager.removeTaskByIdNumber(BASE_TASK_1_ID);
            //Then
            System.out.println("\nTasks AFTER test 'Проверяем удаление существующей Task по ID':");
            System.out.println(taskManager.getAllTasks());
            assertThrows(RuntimeException.class, () -> taskManager.getTaskByIdNumber(BASE_TASK_1_ID));
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
            System.out.println("\nTasks BEFORE test 'Проверяем удаление всех Task':");
            System.out.println(taskManager.getAllTasks());
            //When
            taskManager.removeAllTasks();
            //Then
            System.out.println("\nTasks AFTER test 'Проверяем удаление всех Task':");
            try {
                taskManager.getAllTasks();
            } catch (NotFoundException e) {
                System.out.println(e.getMessage());
            }
            assertThrows(RuntimeException.class, () -> taskManager.getAllTasks());
        }

        @Test
        @DisplayName("Проверяем, что Task не меняется в Менеджере без вызова метода 'updateTask'")
        void check_Immutability_Task_In_Manager_Without_Update_Test() {
            //Given
            String expectedName = "testTaskImmutability";
            String newName = "testTaskNEW";
            Task testTaskImmutability = new Task(expectedName, DESCRIPTION, StatusProgress.NEW);
            taskManager.addTask(testTaskImmutability);
            System.out.println("\nTasks BEFORE test 'Проверяем, что Task не меняется в Менеджере без вызова метода 'updateTask'':");
            System.out.println(taskManager.getAllTasks());
            //When
            testTaskImmutability.setName(newName);
            String actualName = taskManager.getTaskByIdNumber(testTaskImmutability.getIdNumber()).getName().get();
            System.out.println("\nTasks AFTER test 'Проверяем, что Task не меняется в Менеджере без вызова метода 'updateTask'':");
            System.out.println(taskManager.getAllTasks());
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
            Epic testEpic = new Epic("testEpic", DESCRIPTION);
            System.out.println("\nEpics BEFORE test 'Проверяем добавление Epic без заданного ID':");
            System.out.println(taskManager.getAllEpics());
            //When
            actualResult = taskManager.addEpic(testEpic);
            System.out.println("\nEpics AFTER test 'Проверяем добавление Epic без заданного ID':");
            System.out.println(taskManager.getAllEpics());
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
        void epic_Status_Progress_Update_Test(){
            //Given
            StatusProgress expectedStatusProgress = StatusProgress.DONE;
            String messageIfFail = "Если подазадчи DONE, эпик должен быть DONE";
            System.out.println("\nEpic/Subtasks BEFORE test 'Проверяем изменение статуса Epic после обновления Subtask с измененным статусом':");
            System.out.println(taskManager.getAllEpics());
            System.out.println(taskManager.getAllSubtasks());
            //When
            baseSubtask1.setStatusProgress(StatusProgress.DONE);
            taskManager.updateSubtask(baseSubtask1);
            StatusProgress actualStatusProgress = taskManager.getEpicByIdNumber(BASE_EPIC_1_ID).getStatusProgress();
            System.out.println("\nEpic/Subtasks AFTER test 'Проверяем изменение статуса Epic после обновления Subtask с измененным статусом':");
            System.out.println(taskManager.getAllEpics());
            System.out.println(taskManager.getAllSubtasks());
            //Then
            assertEquals(expectedStatusProgress, actualStatusProgress, messageIfFail);
        }

        @Test
        @DisplayName("Проверяем что статус Epic NEW, когда статус всех Subtasks NEW")
        void epic_Check_Status_Progress_When_All_Subtasks_NEW_Test(){
            //Given
            StatusProgress expectedStatusProgress = StatusProgress.NEW;
            System.out.println("\nПроверяем что статус Epic NEW, когда статус всех Subtasks NEW");
            //When
            System.out.println(taskManager.getAllEpics());
            System.out.println(taskManager.getAllSubtasks());
            StatusProgress actualStatusProgress = taskManager.getEpicByIdNumber(BASE_EPIC_2_ID).getStatusProgress();
            //Then
            assertEquals(expectedStatusProgress, actualStatusProgress);
        }

        @Test
        @DisplayName("Проверяем что статус Epic IN_PROGRESS, когда статус всех Subtasks IN_PROGRESS")
        void epic_Check_Status_Progress_When_All_Subtasks_IN_PROGRESS_Test(){
            //Given
            StatusProgress expectedStatusProgress = StatusProgress.IN_PROGRESS;
            System.out.println("\nПроверяем что статус Epic IN_PROGRESS, когда статус всех Subtasks IN_PROGRESS");
            baseSubtask2.setStatusProgress(StatusProgress.IN_PROGRESS);
            baseSubtask3.setStatusProgress(StatusProgress.IN_PROGRESS);
            taskManager.updateSubtask(baseSubtask2);
            taskManager.updateSubtask(baseSubtask3);
            //When
            System.out.println(taskManager.getAllEpics());
            System.out.println(taskManager.getAllSubtasks());
            StatusProgress actualStatusProgress = taskManager.getEpicByIdNumber(BASE_EPIC_2_ID).getStatusProgress();
            //Then
            assertEquals(expectedStatusProgress, actualStatusProgress);
        }

        @Test
        @DisplayName("Проверяем что статус Epic DONE, когда статус всех Subtasks DONE")
        void epic_Check_Status_Progress_When_All_Subtasks_DONE_Test(){
            //Given
            StatusProgress expectedStatusProgress = StatusProgress.DONE;
            System.out.println("\nПроверяем что статус DONE, когда статус всех Subtasks DONE");
            baseSubtask2.setStatusProgress(StatusProgress.DONE);
            baseSubtask3.setStatusProgress(StatusProgress.DONE);
            taskManager.updateSubtask(baseSubtask2);
            taskManager.updateSubtask(baseSubtask3);
            //When
            System.out.println(taskManager.getAllEpics());
            System.out.println(taskManager.getAllSubtasks());
            StatusProgress actualStatusProgress = taskManager.getEpicByIdNumber(BASE_EPIC_2_ID).getStatusProgress();
            //Then
            assertEquals(expectedStatusProgress, actualStatusProgress);
        }

        @Test
        @DisplayName("Проверяем что статус Epic IN_PROGRESS, когда статус Subtasks NEW and DONE")
        void epic_Check_Status_Progress_When_Subtasks_NEW_And_DONE_Test(){
            //Given
            StatusProgress expectedStatusProgress = StatusProgress.IN_PROGRESS;
            System.out.println("\nПроверяем что статус IN_PROGRESS, когда статус Subtasks NEW and DONE");
            baseSubtask3.setStatusProgress(StatusProgress.DONE);
            taskManager.updateSubtask(baseSubtask3);
            //When
            System.out.println(taskManager.getAllEpics());
            System.out.println(taskManager.getAllSubtasks());
            StatusProgress actualStatusProgress = taskManager.getEpicByIdNumber(BASE_EPIC_2_ID).getStatusProgress();
            //Then
            assertEquals(expectedStatusProgress, actualStatusProgress);
        }

        @Test
        @DisplayName("Проверяем что статус Epic IN_PROGRESS, когда статус Subtasks NEW, IN_PROGRESS, DONE")
        void epic_Check_Status_Progress_When_Subtasks_NEW_INPROGRESS_DONE_Test(){
            //Given
            StatusProgress expectedStatusProgress = StatusProgress.IN_PROGRESS;
            System.out.println("\nПроверяем что статус IN_PROGRESS, когда статус Subtasks NEW, IN_PROGRESS, DONE");
            baseSubtask2.setStatusProgress(StatusProgress.IN_PROGRESS);
            baseSubtask3.setStatusProgress(StatusProgress.DONE);
            taskManager.updateSubtask(baseSubtask2);
            taskManager.updateSubtask(baseSubtask3);
            Subtask testSubtaskEpic2 = new Subtask("subtaskEpic2", DESCRIPTION, StatusProgress.NEW, BASE_EPIC_2_ID);
            taskManager.addSubtask(testSubtaskEpic2);
            //When
            System.out.println(taskManager.getAllEpics());
            System.out.println(taskManager.getAllSubtasks());
            StatusProgress actualStatusProgress = taskManager.getEpicByIdNumber(BASE_EPIC_2_ID).getStatusProgress();
            //Then
            assertEquals(expectedStatusProgress, actualStatusProgress);
        }

        @Test
        @DisplayName("Проверяем что статус Epic IN_PROGRESS, когда статус Subtasks IN_PROGRESS and DONE")
        void epic_Check_Status_Progress_When_Subtasks_INPROGRESS_DONE_Test(){
            //Given
            StatusProgress expectedStatusProgress = StatusProgress.IN_PROGRESS;
            System.out.println("\nПроверяем что статус IN_PROGRESS, когда статус Subtasks IN_PROGRESS and DONE");
            baseSubtask2.setStatusProgress(StatusProgress.IN_PROGRESS);
            baseSubtask3.setStatusProgress(StatusProgress.DONE);
            taskManager.updateSubtask(baseSubtask2);
            taskManager.updateSubtask(baseSubtask3);
            //When
            System.out.println(taskManager.getAllEpics());
            System.out.println(taskManager.getAllSubtasks());
            StatusProgress actualStatusProgress = taskManager.getEpicByIdNumber(BASE_EPIC_2_ID).getStatusProgress();
            //Then
            assertEquals(expectedStatusProgress, actualStatusProgress);
        }

        @Test
        @DisplayName("Проверяем удаление существующего Epic по ID")
        void removeEpicForIdNumber_Test() {
            //Given
            System.out.println("\nEpic/Subtasks BEFORE test 'Проверяем удаление существующего Epic по ID':");
            System.out.println(taskManager.getAllEpics());
            System.out.println(taskManager.getAllSubtasks());
            //When
            taskManager.removeEpicByIdNumber(BASE_EPIC_WITHOUT_SUBTASK_ID);
            //Then
            System.out.println("\nEpic/Subtasks AFTER test 'Проверяем удаление существующего Epic по ID':");
            System.out.println(taskManager.getAllEpics());
            System.out.println(taskManager.getAllSubtasks());
            assertThrows(RuntimeException.class, () -> taskManager.getEpicByIdNumber(BASE_EPIC_WITHOUT_SUBTASK_ID));
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
            System.out.println("\nEpic/Subtasks BEFORE test 'Проверяем удаление всех Epic (и как следствие Subtask)':");
            System.out.println(taskManager.getAllEpics());
            System.out.println(taskManager.getAllSubtasks());
            //When
            taskManager.removeAllEpics();
            System.out.println("\nEpic/Subtasks AFTER test 'Проверяем удаление всех Epic':");
            try {
                taskManager.getAllEpics();
            } catch (NotFoundException e) {
                System.out.println(e.getMessage());
            }
            //Then
            assertThrows(RuntimeException.class, () -> taskManager.getAllEpics());
        }

        @Test
        @DisplayName("Проверяем обновление Epic Name в Менеджере")
        void updateEpicName_Test() {
            //Given
            String expectedName = "testEpicUPDATE";
            String oldName = "testEpic";
            Epic testEpic = new Epic(oldName, DESCRIPTION);
            taskManager.addEpic(testEpic);
            System.out.println("\nEpic BEFORE test 'Проверяем обновление Epic Name в Менеджере':");
            System.out.println(taskManager.getAllEpics());
            //When
            taskManager.updateEpicName(testEpic.getIdNumber(), expectedName);
            String actualName = taskManager.getEpicByIdNumber(testEpic.getIdNumber()).getName().get();
            System.out.println("\nEpic AFTER test 'Проверяем обновление Epic Name в Менеджере':");
            System.out.println(taskManager.getAllEpics());
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
            System.out.println("\nEpic BEFORE test 'Проверяем обновление Epic Description в Менеджере':");
            System.out.println(taskManager.getAllEpics());
            //When
            taskManager.updateEpicDescription(testEpic.getIdNumber(), expectedDescription);
            String actualDescription = taskManager.getEpicByIdNumber(testEpic.getIdNumber()).getDescription().get();
            System.out.println("\nEpic AFTER test 'Проверяем обновление Epic Description в Менеджере':");
            System.out.println(taskManager.getAllEpics());
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
            System.out.println("\nEpic BEFORE test 'Проверяем, что Epic Name не меняется в Менеджере без вызова метода" +
                    " 'updateEpicName'':");
            System.out.println(taskManager.getAllEpics());
            //When
            testEpicNameImmutability.setName(newName);
            String actualName = taskManager.getEpicByIdNumber(testEpicNameImmutability.getIdNumber()).getName().get();
            System.out.println("\nEpic AFTER test 'Проверяем, что Epic Name не меняется в Менеджере без вызова метода" +
                    " 'updateEpicName'':");
            System.out.println(taskManager.getAllEpics());
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
            System.out.println("\nEpic BEFORE test 'Проверяем, что Epic Description не меняется в Менеджере без вызова " +
                    "метода 'updateEpicDescription':");
            System.out.println(taskManager.getAllEpics());
            //When
            testEpicDescriptionImmutability.setDescription(newDescription);
            String actualDescription = taskManager.getEpicByIdNumber(testEpicDescriptionImmutability.getIdNumber())
                    .getDescription().get();
            System.out.println("\nEpic AFTER test 'Проверяем, что Epic Description не меняется в Менеджере без вызова " +
                    "метода 'updateEpicDescription':");
            System.out.println(taskManager.getAllEpics());
            //Then
            assertEquals(expectedDescription, actualDescription);
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
            assertThrows(RuntimeException.class, () -> taskManager.getSubtaskByIdNumber(notExistsIdNumber));
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
            //When
            taskManager.removeAllSubtasks();
            //Then
            assertThrows(RuntimeException.class, () -> taskManager.getAllSubtasks());
        }

        @Test
        @DisplayName("Проверяем, что Subtask не меняется в Менеджере без вызова метода 'updateSubtask'")
        void check_Immutability_Subtask_In_Manager_Without_Update_Test() {
            //Given
            String expectedName = "testSubtaskImmutability";
            String newName = "testSubtaskNEW";
            Subtask testSubtaskImmutability = new Subtask(expectedName, DESCRIPTION, StatusProgress.NEW, BASE_EPIC_2_ID);
            taskManager.addSubtask(testSubtaskImmutability);
            System.out.println("\nSubtasks BEFORE test 'Проверяем, что Subtask не меняется в Менеджере без вызова метода " +
                    "'updateSubtask'':");
            System.out.println(taskManager.getAllSubtasks());
            //When
            testSubtaskImmutability.setName(newName);
            String actualName = taskManager.getSubtaskByIdNumber(testSubtaskImmutability.getIdNumber()).getName().get();
            System.out.println("\nSubtasks AFTER test 'Проверяем, что Subtask не меняется в Менеджере без вызова метода " +
                    "'updateSubtask'':");
            System.out.println(taskManager.getAllSubtasks());
            //Then
            assertEquals(expectedName, actualName);
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
        baseTask1.setDurationInMinutes(10);
        baseTask1.setStartDateTime(LocalDateTime.now().withNano(0).plusHours(3));
        baseSubtask2.setDurationInMinutes(10);
        baseSubtask2.setStartDateTime(LocalDateTime.now().withNano(0).plusHours(2));
        baseSubtask3.setDurationInMinutes(10);
        baseSubtask3.setStartDateTime(LocalDateTime.now().withNano(0).plusHours(1));
        expectedList.add(baseSubtask3);
        expectedList.add(baseSubtask2);
        expectedList.add(baseTask1);

        System.out.println("\nПроверяем получение списка приоритетных задач");
        System.out.println("taskManager.updateTask(baseTask1):" + taskManager.updateTask(baseTask1));
        System.out.println("taskManager.updateSubtask(baseSubtask2):" + taskManager.updateSubtask(baseSubtask2));
        System.out.println("taskManager.updateSubtask(baseSubtask3):" + taskManager.updateSubtask(baseSubtask3));
        //When
        ArrayList<AbstractTask> actualList = taskManager.getPrioritizedTasks();
        System.out.println(actualList);
        //Then
        assertEquals(expectedList, actualList);
    }

    @Test
    @DisplayName("Проверяем пересечение двух задач по времени")
    void isTimeIntersectBoth_Test() {
        //Given, expected true
        baseTask1.setDurationInMinutes(10);
        baseTask1.setStartDateTime(LocalDateTime.now().withNano(0).plusMinutes(55));
        baseSubtask2.setDurationInMinutes(10);
        baseSubtask2.setStartDateTime(LocalDateTime.now().withNano(0).plusHours(1));
        //When, Then
        assertTrue(taskManager.isTimeIntersectBoth(baseTask1, baseSubtask2));
    }

    @Test
    @DisplayName("Проверяем пересечение задачи по времени с приоритетными задачами")
    void isTimeIntersectWithOthers_Test() {
        //Given, expected true
        baseTask1.setDurationInMinutes(10);
        baseTask1.setStartDateTime(LocalDateTime.now().withNano(0).plusHours(3));
        baseSubtask2.setDurationInMinutes(10);
        baseSubtask2.setStartDateTime(LocalDateTime.now().withNano(0).plusHours(2));
        baseSubtask3.setDurationInMinutes(10);
        baseSubtask3.setStartDateTime(LocalDateTime.now().withNano(0).plusHours(1));
        taskManager.updateTask(baseTask1);
        taskManager.updateSubtask(baseSubtask2);
        taskManager.updateSubtask(baseSubtask3);
        baseTask2.setDurationInMinutes(10);
        baseTask2.setStartDateTime(LocalDateTime.now().withNano(0).plusHours(1).plusMinutes(5));
        //When, Then
        assertTrue(taskManager.isTimeIntersectWithOthers(baseTask2));
    }

    @Test
    @DisplayName("Проверяем замену задачи (с новым временем) в приоритетных задачах")
    void check_Replace_Task_In_Prioritized_With_New_Time_Test() {
        //Given
        baseTask1.setDurationInMinutes(10);
        baseTask1.setStartDateTime(LocalDateTime.now().withNano(0).plusHours(1));
        baseTask2.setDurationInMinutes(10);
        baseTask2.setStartDateTime(LocalDateTime.now().withNano(0).plusHours(2));
        taskManager.updateTask(baseTask1);
        taskManager.updateTask(baseTask2);
        System.out.println("\nПроверяем замену задачи (с новым временем) в приоритетных задачах");
        System.out.println("before" + taskManager.getPrioritizedTasks());
        //When
        baseTask2.setStartDateTime(LocalDateTime.now().withNano(0).plusHours(2).plusMinutes(5));
        System.out.println("taskManager.updateTask(baseTask2):" + taskManager.updateTask(baseTask2));
        Task newBaseTask2 = taskManager.getTaskByIdNumber(BASE_TASK_2_ID);
        System.out.println("after" + taskManager.getPrioritizedTasks());
        //Then
        assertTrue(taskManager.getPrioritizedTasks().contains(newBaseTask2));
    }

}
