package ru.yandex.practicum.java.tasktracker.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.java.tasktracker.task.Task;
import ru.yandex.practicum.java.tasktracker.utils.enums.ResultOfOperation;
import ru.yandex.practicum.java.tasktracker.utils.enums.StatusProgress;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Тесты потокобезопасности TaskManager")
class TaskManagerConcurrencyTest {
    private static final int THREADS = 16;
    private static final int TASKS_PER_THREAD = 50;

    @Test
    @DisplayName("Параллельное добавление задач: все добавляются, id уникальны")
    void parallelAddTasks_Should_BeThreadSafe_Test() throws InterruptedException {
        //Given
        TaskManager taskManager = new TaskManager();
        int totalTasks = THREADS * TASKS_PER_THREAD;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(THREADS);
        ExecutorService executor = Executors.newFixedThreadPool(THREADS);
        List<ResultOfOperation> results = new ArrayList<>();

        //When
        for (int t = 0; t < THREADS; t++) {
            final int threadIndex = t;
            executor.submit(() -> {
                try {
                    startLatch.await();
                    for (int i = 0; i < TASKS_PER_THREAD; i++) {
                        Task task = new Task("task-" + threadIndex + "-" + i, "desc", StatusProgress.NEW);
                        ResultOfOperation result = taskManager.addTask(task);
                        synchronized (results) {
                            results.add(result);
                        }
                    }
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        assertTrue(doneLatch.await(30, TimeUnit.SECONDS), "Threads did not finish in time");
        executor.shutdown();

        //Then
        long successCount = results.stream()
                .filter(r -> r == ResultOfOperation.SUCCESS)
                .count();
        assertEquals(totalTasks, successCount, "Не все addTask вернули SUCCESS");

        List<Task> allTasks = taskManager.getAllTasks();
        assertEquals(totalTasks, allTasks.size(), "Не все задачи добавились");

        Set<Integer> ids = new HashSet<>();
        for (Task task : allTasks) {
            assertTrue(ids.add(task.getIdNumber()), "Дубликат id: " + task.getIdNumber());
        }
        assertEquals(totalTasks, ids.size(), "Количество уникальных id не совпадает");
    }
}