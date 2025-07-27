package ru.yandex.practicum.java.tasktracker.manage;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.java.tasktracker.task.*;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest {
    private static TaskManager inMemoryTaskManager;
    private static Task baseTask1;
    private static Task baseTask2;
    private static Task baseTaskNull;
    private static Epic baseEpic1;
    private static Epic baseEpic2;
    private static Epic baseEpicNull;
    private static Subtask baseSubtask1;
    private static Subtask baseSubtask2;
    private static Subtask baseSubtask3;
    private static Subtask baseSubtaskNull;

    @BeforeAll
    public static void Prepare_For_Test() {
        inMemoryTaskManager = ManagersUtil.getDefault();
        baseTaskNull = new Task();
        baseEpicNull = new Epic();
        baseSubtaskNull = new Subtask();

        baseTask1 = new Task("baseTask1 name", "baseTask1 description", StatusProgress.NEW, 1);
        baseTask2 = new Task("baseTask2 name", "baseTask2 description", StatusProgress.NEW, 3);

        inMemoryTaskManager.addTask(baseTask1);
        inMemoryTaskManager.addTask(baseTask2);
        System.out.println("Tasks (base) before test: " + inMemoryTaskManager.getAllTasks());
        System.out.println();

        baseEpic1 = new Epic("baseEpic1 name", "baseEpic1 description", 4);
        baseEpic2 = new Epic("baseEpic2 name", "baseEpic2 description", 5);
        baseSubtask1 = new Subtask(baseEpic1.getIdNumber(),"baseSubtask1 name", "baseSubtask1 description"
                , StatusProgress.NEW, 6);
        baseSubtask2 = new Subtask(baseEpic2.getIdNumber(),"baseSubtask2 name", "baseSubtask2 description"
                , StatusProgress.NEW, 7);
        baseSubtask3 = new Subtask(baseEpic2.getIdNumber(),"baseSubtask3 name", "baseSubtask3 description"
                , StatusProgress.NEW, 8);
        inMemoryTaskManager.addEpic(baseEpic1);
        inMemoryTaskManager.addEpic(baseEpic2);
        inMemoryTaskManager.addSubtask(baseSubtask1);
        inMemoryTaskManager.addSubtask(baseSubtask2);
        inMemoryTaskManager.addSubtask(baseSubtask3);

        System.out.println("Epics (base) before test: " + inMemoryTaskManager.getAllEpics());
        System.out.println();
        System.out.println("Subtasks (base) before test: " + inMemoryTaskManager.getAllSubtasks());
        System.out.println();
    }

    @AfterAll
    public static void Print_All() {
        System.out.println("History: " + inMemoryTaskManager.getHistory());
        System.out.println();
        System.out.println("Tasks (all) after test: " + inMemoryTaskManager.getAllTasks());
        System.out.println();
        System.out.println("Epic (all) after test: " + inMemoryTaskManager.getAllEpics());
        System.out.println();
        System.out.println("Subtasks (all) after test: " + inMemoryTaskManager.getAllSubtasks());
        System.out.println();
    }

    @Test
    void addTask_NULL() {
        Task testTask1 = null;

        assertEquals(ResultOfOperation.ERROR_OBJECT_NULL, inMemoryTaskManager.addTask(testTask1));
    }

    @Test
    void addTask_Without_IdNumber() {
        Task testTask2 = new Task("testTask2", "testTask2 description", StatusProgress.NEW);

        assertEquals(ResultOfOperation.SUCCESS, inMemoryTaskManager.addTask(testTask2));
    }

    @Test
    void addTask_With_Unique_IdNumber() {
        Task testTask3 = new Task("testTask3", "testTask3 description", StatusProgress.NEW, 888);

        assertEquals(ResultOfOperation.SUCCESS, inMemoryTaskManager.addTask(testTask3));
    }

    @Test
    void addTask_With_IdNumber_Already_In_Manager() {
        Task testTask4 = new Task("testTask4", "testTask4 description",
                StatusProgress.NEW, baseTask1.getIdNumber());

        assertEquals(ResultOfOperation.ERROR_OBJECT_ALREADY_EXISTS, inMemoryTaskManager.addTask(testTask4));
    }

    @Test
    void getTaskForIdNumber_Exists() {
        assertEquals(baseTask1, inMemoryTaskManager.getTaskForIdNumber(baseTask1.getIdNumber()));
    }

    @Test
    void getTaskForIdNumber_NotExists() {
        assertEquals(baseTaskNull, inMemoryTaskManager.getTaskForIdNumber(666));
    }

    @Test
    void addEpic_Without_IdNumber() {
        Epic testEpic1 = new Epic("testEpic1 name", "testEpic1 description");

        assertEquals(ResultOfOperation.SUCCESS, inMemoryTaskManager.addEpic(testEpic1));
    }

    @Test
    void getEpicForIdNumber_Exists() {
        assertEquals(baseEpic1, inMemoryTaskManager.getEpicForIdNumber(baseEpic1.getIdNumber()));
    }

    @Test
    void addSubtask_Already_In_Manager() {
        assertEquals(ResultOfOperation.ERROR_OBJECT_ALREADY_EXISTS, inMemoryTaskManager.addSubtask(baseSubtask3));
    }

    @Test
    void addSubtask_New_To_Exists_Epic() {
        Subtask testSubtask1 = new Subtask(baseEpic2.getIdNumber(), "testSubtask1 name"
                , "testSubtask1 description", StatusProgress.NEW);
        assertEquals(ResultOfOperation.SUCCESS, inMemoryTaskManager.addSubtask(testSubtask1));
    }

    @Test
    void addSubtask_New_To_Not_Exists_Epic() {
        Subtask testSubtask2 = new Subtask(666, "testSubtask2 name"
                , "testSubtask2 description", StatusProgress.NEW);
        assertEquals(ResultOfOperation.ERROR_MISMATCH_PARENT_ID, inMemoryTaskManager.addSubtask(testSubtask2));
    }

    @Test
    void getSubtaskForIdNumber_Exists() {
        assertEquals(baseSubtask1, inMemoryTaskManager.getSubtaskForIdNumber(baseSubtask1.getIdNumber()));
    }
    @Test
    void getSubtaskForIdNumber_Not_Exists() {
        assertEquals(baseSubtaskNull, inMemoryTaskManager.getSubtaskForIdNumber(666));
    }

    @Test
    void getAllSubtasksFromEpic() {
        assertEquals(1, inMemoryTaskManager.getAllSubtasksFromEpic(baseEpic1.getIdNumber()).size());
    }

    @Test
    void updateSubtask() {
        baseSubtask2.setStatusProgress(StatusProgress.DONE);

        assertEquals(ResultOfOperation.SUCCESS, inMemoryTaskManager.updateSubtask(baseSubtask2));
    }

    @Test
    void getHistory() {
        inMemoryTaskManager.getTaskForIdNumber(baseTask2.getIdNumber());
        inMemoryTaskManager.getEpicForIdNumber(baseEpic2.getIdNumber());
        inMemoryTaskManager.getSubtaskForIdNumber(baseSubtask2.getIdNumber());

        assertTrue(inMemoryTaskManager.getHistory().size() > 0, "История не может быть пустой после обращения к задачам по idNumber");
    }

    @Test
    void Status_Progress_Epic_Update_After_Change_Subtask_Status_Progress(){
        baseSubtask1.setStatusProgress(StatusProgress.DONE);
        inMemoryTaskManager.updateSubtask(baseSubtask1);

        assertEquals(StatusProgress.DONE, baseEpic1.getStatusProgress(), "Если подазадчи DONE, эпик должен быть DONE");
    }
}