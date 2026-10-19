package ru.yandex.practicum.java.tasktracker.http;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.*;
import ru.yandex.practicum.java.tasktracker.service.TaskManager;
import ru.yandex.practicum.java.tasktracker.task.*;
import ru.yandex.practicum.java.tasktracker.utils.AbstractTask;
import ru.yandex.practicum.java.tasktracker.utils.TextForGson;
import ru.yandex.practicum.java.tasktracker.utils.enums.StatusProgress;
import ru.yandex.practicum.java.tasktracker.utils.gson.*;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

@DisplayName("Тесты для HttpTaskServer")
class HttpTaskServerTest {

    static TaskManager taskManager;
    static HttpTaskServer httpTaskServer;
    static Gson gson;
    static String baseUrl;

    @BeforeAll
    public static void startServer() throws IOException {
        taskManager = new TaskManager();
        httpTaskServer = new HttpTaskServer(taskManager, 0);
        gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeTypeAdapter())
                .registerTypeAdapter(Duration.class, new DurationTypeAdapter())
                .registerTypeAdapter(AbstractTask.class, new AbstractTaskDeserializer())
                .create();
        httpTaskServer.start();
        baseUrl = "http://localhost:" + httpTaskServer.getPort();
    }

    @AfterAll
    public static void stopServer() {
        httpTaskServer.stop();
    }

    @BeforeEach
    public void cleanData() {
        taskManager.removeAllTasks();
        taskManager.removeAllEpics();
    }

    @Nested
    @DisplayName("Тесты работы эндпоинтов /tasks.")
    class testForTasks {
        @Test
        @DisplayName("Проверяем эндпоинт 'DELETE /tasks{id}' - удаление Task.")
        void endpointTasks_Delete_Task_Test() throws IOException, InterruptedException {
            //Given
            int expectedStatusCode = 204;

            Task task = new Task("task", "test task", StatusProgress.NEW, 10,
                    LocalDateTime.now().withNano(0));
            taskManager.addTask(task);

            HttpClient httpClient = HttpClient.newHttpClient();
            URI url = URI.create(baseUrl + "/tasks/" + task.getIdNumber());
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(url)
                    .DELETE()
                    .build();
            //When
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            //Then
            assertEquals(expectedStatusCode, response.statusCode());
            assertThrows(RuntimeException.class, () -> taskManager.getTaskByIdNumber(task.getIdNumber()));
        }

        @Test
        @DisplayName("Проверяем эндпоинт 'POST /tasks' - добавление Task.")
        void endpointTasks_Add_Task_Test() throws IOException, InterruptedException {
            //Given
            int expectedStatusCode = 201;
            int expectedSizeTasksList = 1;
            String expectedTaskDescription = "test task";
            Task task = new Task("task", expectedTaskDescription, StatusProgress.NEW, 10,
                    LocalDateTime.now().withNano(0));
            String taskJson = gson.toJson(task);

            HttpClient httpClient = HttpClient.newHttpClient();
            URI url = URI.create(baseUrl + "/tasks");
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .header("Content-type", "application/json")
                    .uri(url)
                    .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                    .build();
            //When
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            //Then
            assertEquals(expectedStatusCode, response.statusCode());

            ArrayList<Task> tasksFromManager = taskManager.getAllTasks();
            String actualTaskDescription = tasksFromManager.get(0).getDescription().get();
            int actualSizeTasksList = tasksFromManager.size();

            assertEquals(expectedSizeTasksList, actualSizeTasksList, "Неверное количество задач");
            assertEquals(expectedTaskDescription, actualTaskDescription, "Возвращаемая задача не соответствует заданной");
        }

        @Test
        @DisplayName("Проверяем эндпоинт 'POST /tasks' - обновление Task.")
        void endpointTasks_Update_Task_Test() throws IOException, InterruptedException {
            //Given
            int expectedStatusCode = 200;
            StatusProgress expectedStatusProgress = StatusProgress.IN_PROGRESS;

            Task task = new Task("task", "test task", StatusProgress.NEW, 10,
                    LocalDateTime.now().withNano(0));
            taskManager.addTask(task);
            task.setStatusProgress(StatusProgress.IN_PROGRESS);
            String taskJson = gson.toJson(task);

            HttpClient httpClient = HttpClient.newHttpClient();
            URI url = URI.create(baseUrl + "/tasks");
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .header("Content-type", "application/json")
                    .uri(url)
                    .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                    .build();
            //When
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            //Then
            assertEquals(expectedStatusCode, response.statusCode());

            Task tasksFromManager = taskManager.getTaskByIdNumber(task.getIdNumber());
            StatusProgress actualStatusProgress = tasksFromManager.getStatusProgress();

            assertEquals(expectedStatusProgress, actualStatusProgress, "Задача в менеджере не обновилась");
        }

        @Test
        @DisplayName("Проверяем эндпоинт 'GET /tasks' - получение всех Task.")
        void endpointTasks_Get_All_Tasks_Test() throws IOException, InterruptedException {
            //Given
            int expectedStatusCode = 200;
            int expectedSizeTasksList = 2;

            Task task1 = new Task("task1", "test task1", StatusProgress.NEW, 10,
                    LocalDateTime.now().withNano(0));
            Task task2 = new Task("task2", "test task2", StatusProgress.NEW, 10,
                    LocalDateTime.now().withNano(0).plusHours(1));
            taskManager.addTask(task1);
            taskManager.addTask(task2);

            HttpClient httpClient = HttpClient.newHttpClient();
            URI url = URI.create(baseUrl + "/tasks");
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(url)
                    .GET()
                    .build();
            //When
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            //Then
            assertEquals(expectedStatusCode, response.statusCode());

            TypeToken<ArrayList<Task>> collectionType = new TypeToken<>(){};
            ArrayList<Task> tasksFromServer = gson.fromJson(response.body(), collectionType);
            int actualSizeTasksList = tasksFromServer.size();

            assertEquals(expectedSizeTasksList, actualSizeTasksList, "Неверное количество задач");
        }

        @Test
        @DisplayName("Проверяем эндпоинт 'GET /tasks?from=1&size=1' - пагинация.")
        void endpointTasks_Get_With_Pagination_Test() throws IOException, InterruptedException {
            //Given
            int expectedStatusCode = 200;
            int expectedSize = 1;

            Task task1 = new Task("task1", "test task1", StatusProgress.NEW, 10,
                    LocalDateTime.now().withNano(0));
            Task task2 = new Task("task2", "test task2", StatusProgress.NEW, 10,
                    LocalDateTime.now().withNano(0).plusHours(1));
            Task task3 = new Task("task3", "test task3", StatusProgress.NEW, 10,
                    LocalDateTime.now().withNano(0).plusHours(2));
            taskManager.addTask(task1);
            taskManager.addTask(task2);
            taskManager.addTask(task3);

            HttpClient httpClient = HttpClient.newHttpClient();

            // Полный список — чтобы взять эталон "второго элемента по порядку"
            URI fullUrl = URI.create(baseUrl + "/tasks");
            HttpRequest fullRequest = HttpRequest.newBuilder()
                    .uri(fullUrl)
                    .GET()
                    .build();
            HttpResponse<String> fullResponse = httpClient.send(fullRequest,
                    HttpResponse.BodyHandlers.ofString());

            //When
            URI url = URI.create(baseUrl + "/tasks?from=1&size=1");
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(url)
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(httpRequest,
                    HttpResponse.BodyHandlers.ofString());

            //Then
            assertEquals(expectedStatusCode, response.statusCode());

            TypeToken<ArrayList<Task>> collectionType = new TypeToken<>(){};
            ArrayList<Task> fullList = gson.fromJson(fullResponse.body(), collectionType);
            ArrayList<Task> pagedList = gson.fromJson(response.body(), collectionType);

            assertEquals(expectedSize, pagedList.size(), "Неверный размер страницы");
            assertEquals(fullList.get(1).getIdNumber(), pagedList.get(0).getIdNumber(),
                    "Вернулась не вторая по порядку задача");
        }

        @Test
        @DisplayName("Проверяем ответ сервера (400) - 'GET /tasks?from=-1', отрицательный from.")
        void endpointTasks_Get_Negative_From_Test() throws IOException, InterruptedException {
            //Given
            int expectedStatusCode = 400;

            HttpClient httpClient = HttpClient.newHttpClient();
            URI url = URI.create(baseUrl + "/tasks?from=-1");
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(url)
                    .GET()
                    .build();
            //When
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            //Then
            assertEquals(expectedStatusCode, response.statusCode());
            TextForGson body = gson.fromJson(response.body(), TextForGson.class);
            assertNotNull(body);
            assertTrue(body.getDetail() != null && body.getDetail().contains("from"),
                    "Сообщение об ошибке должно упоминать 'from'");
        }

        @Test
        @DisplayName("Проверяем ответ сервера (400) - 'GET /tasks?size=0', нулевой size.")
        void endpointTasks_Get_Zero_Size_Test() throws IOException, InterruptedException {
            //Given
            int expectedStatusCode = 400;

            HttpClient httpClient = HttpClient.newHttpClient();
            URI url = URI.create(baseUrl + "/tasks?size=0");
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(url)
                    .GET()
                    .build();
            //When
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            //Then
            assertEquals(expectedStatusCode, response.statusCode());
            TextForGson body = gson.fromJson(response.body(), TextForGson.class);
            assertNotNull(body);
            assertTrue(body.getDetail() != null && body.getDetail().contains("size"),
                    "Сообщение об ошибке должно упоминать 'size'");
        }

        @Test
        @DisplayName("Проверяем ответ сервера (400) - 'GET /tasks?size=101', превышение лимита size.")
        void endpointTasks_Get_Oversized_Size_Test() throws IOException, InterruptedException {
            //Given
            int expectedStatusCode = 400;

            HttpClient httpClient = HttpClient.newHttpClient();
            URI url = URI.create(baseUrl + "/tasks?size=101");
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(url)
                    .GET()
                    .build();
            //When
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            //Then
            assertEquals(expectedStatusCode, response.statusCode());
            TextForGson body = gson.fromJson(response.body(), TextForGson.class);
            assertNotNull(body);
            assertTrue(body.getDetail() != null && body.getDetail().contains("size"),
                    "Сообщение об ошибке должно упоминать 'size'");
        }

        @Test
        @DisplayName("Проверяем ответ сервера (400) - 'GET /tasks?from=abc', нечисловой from.")
        void endpointTasks_Get_NonInteger_From_Test() throws IOException, InterruptedException {
            //Given
            int expectedStatusCode = 400;

            HttpClient httpClient = HttpClient.newHttpClient();
            URI url = URI.create(baseUrl + "/tasks?from=abc");
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(url)
                    .GET()
                    .build();
            //When
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            //Then
            assertEquals(expectedStatusCode, response.statusCode());
            TextForGson body = gson.fromJson(response.body(), TextForGson.class);
            assertNotNull(body);
            assertTrue(body.getDetail() != null && body.getDetail().contains("from"),
                    "Сообщение об ошибке должно упоминать 'from'");
        }

        @Test
        @DisplayName("Проверяем ответ сервера (400) - 'GET /tasks?status=WRONG', невалидный status.")
        void endpointTasks_Get_Invalid_Status_Test() throws IOException, InterruptedException {
            //Given
            int expectedStatusCode = 400;

            HttpClient httpClient = HttpClient.newHttpClient();
            URI url = URI.create(baseUrl + "/tasks?status=WRONG");
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(url)
                    .GET()
                    .build();
            //When
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            //Then
            assertEquals(expectedStatusCode, response.statusCode());
            TextForGson body = gson.fromJson(response.body(), TextForGson.class);
            assertNotNull(body);
            assertTrue(body.getDetail() != null && body.getDetail().contains("status"),
                    "Сообщение об ошибке должно упоминать 'status'");
        }

        @Test
        @DisplayName("Проверяем ответ сервера (400) - 'GET /tasks?sortBy=bad', невалидный sortBy.")
        void endpointTasks_Get_Invalid_SortBy_Test() throws IOException, InterruptedException {
            //Given
            int expectedStatusCode = 400;

            HttpClient httpClient = HttpClient.newHttpClient();
            URI url = URI.create(baseUrl + "/tasks?sortBy=bad");
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(url)
                    .GET()
                    .build();
            //When
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            //Then
            assertEquals(expectedStatusCode, response.statusCode());
            TextForGson body = gson.fromJson(response.body(), TextForGson.class);
            assertNotNull(body);
            assertTrue(body.getDetail() != null && body.getDetail().contains("sortBy"),
                    "Сообщение об ошибке должно упоминать 'sortBy'");
        }

        @Test
        @DisplayName("Проверяем эндпоинт 'GET /tasks?status=DONE' - фильтр по статусу.")
        void endpointTasks_Get_Filtered_By_Status_Test() throws IOException, InterruptedException {
            //Given
            int expectedStatusCode = 200;
            int expectedSize = 2;

            Task newTask = new Task("new", "new task", StatusProgress.NEW, 10,
                    LocalDateTime.now().withNano(0));
            Task doneTask1 = new Task("done1", "done task 1", StatusProgress.NEW, 10,
                    LocalDateTime.now().withNano(0).plusHours(1));
            doneTask1.setStatusProgress(StatusProgress.DONE);
            Task doneTask2 = new Task("done2", "done task 2", StatusProgress.NEW, 10,
                    LocalDateTime.now().withNano(0).plusHours(2));
            doneTask2.setStatusProgress(StatusProgress.DONE);
            taskManager.addTask(newTask);
            taskManager.addTask(doneTask1);
            taskManager.addTask(doneTask2);

            HttpClient httpClient = HttpClient.newHttpClient();
            URI url = URI.create(baseUrl + "/tasks?status=DONE");
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(url)
                    .GET()
                    .build();
            //When
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            //Then
            assertEquals(expectedStatusCode, response.statusCode());

            TypeToken<ArrayList<Task>> collectionType = new TypeToken<>(){};
            ArrayList<Task> result = gson.fromJson(response.body(), collectionType);

            assertEquals(expectedSize, result.size(), "Неверное количество задач после фильтра");
            assertTrue(result.stream().allMatch(t -> t.getStatusProgress() == StatusProgress.DONE),
                    "В результате есть задача не со статусом DONE");
        }

        @Test
        @DisplayName("Проверяем эндпоинт 'GET /tasks?sortBy=startDateTime' - сортировка по дате старта.")
        void endpointTasks_Get_Sorted_By_StartDateTime_Test() throws IOException, InterruptedException {
            //Given
            int expectedStatusCode = 200;
            int expectedSize = 3;
            LocalDateTime base = LocalDateTime.of(2026, 1, 1, 10, 0);

            // Добавляем в "неправильном" порядке: средняя, поздняя, ранняя
            Task middle = new Task("middle", "middle", StatusProgress.NEW, 10, base.plusHours(5));
            Task late = new Task("late", "late", StatusProgress.NEW, 10, base.plusHours(10));
            Task early = new Task("early", "early", StatusProgress.NEW, 10, base.plusHours(1));
            taskManager.addTask(middle);
            taskManager.addTask(late);
            taskManager.addTask(early);

            HttpClient httpClient = HttpClient.newHttpClient();
            URI url = URI.create(baseUrl + "/tasks?sortBy=startDateTime");
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(url)
                    .GET()
                    .build();
            //When
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            //Then
            assertEquals(expectedStatusCode, response.statusCode());

            TypeToken<ArrayList<Task>> collectionType = new TypeToken<>(){};
            ArrayList<Task> result = gson.fromJson(response.body(), collectionType);

            assertEquals(expectedSize, result.size(), "Неверное количество задач");
            assertEquals(early.getIdNumber(), result.get(0).getIdNumber(),
                    "Первой должна быть задача с самым ранним startDateTime");
            assertEquals(middle.getIdNumber(), result.get(1).getIdNumber(),
                    "Второй должна быть задача со средним startDateTime");
            assertEquals(late.getIdNumber(), result.get(2).getIdNumber(),
                    "Третьей должна быть задача с самым поздним startDateTime");
        }

        @Test
        @DisplayName("Проверяем эндпоинт 'GET /tasks' без параметров - дефолт size=20.")
        void endpointTasks_Get_Default_Params_Test() throws IOException, InterruptedException {
            //Given
            int expectedStatusCode = 200;
            int totalTasks = 25;
            int expectedSize = 20;
            LocalDateTime base = LocalDateTime.of(2026, 1, 1, 10, 0);

            for (int i = 0; i < totalTasks; i++) {
                Task task = new Task("task-" + i, "desc", StatusProgress.NEW, 10,
                        base.plusMinutes(i * 20L));
                taskManager.addTask(task);
            }

            HttpClient httpClient = HttpClient.newHttpClient();
            URI url = URI.create(baseUrl + "/tasks");
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(url)
                    .GET()
                    .build();
            //When
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            //Then
            assertEquals(expectedStatusCode, response.statusCode());

            TypeToken<ArrayList<Task>> collectionType = new TypeToken<>(){};
            ArrayList<Task> result = gson.fromJson(response.body(), collectionType);

            assertEquals(expectedSize, result.size(),
                    "По умолчанию должно вернуться 20 задач (size=20)");
        }

        @Test
        @DisplayName("Проверяем эндпоинт 'GET /tasks/{id}' - получение конкретной Task.")
        void endpointTasks_Get_That_Task_Test() throws IOException, InterruptedException {
            //Given
            int expectedStatusCode = 200;

            Task task1 = new Task("task1", "test task1", StatusProgress.NEW, 10,
                    LocalDateTime.now().withNano(0));
            Task task2 = new Task("task2", "test task2", StatusProgress.NEW, 10,
                    LocalDateTime.now().withNano(0).plusHours(1));
            taskManager.addTask(task1);
            taskManager.addTask(task2);

            Task expectedTask = task2;

            HttpClient httpClient = HttpClient.newHttpClient();
            URI url = URI.create(baseUrl + "/tasks/" + task2.getIdNumber());
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(url)
                    .GET()
                    .build();
            //When
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            //Then
            assertEquals(expectedStatusCode, response.statusCode());

            Task actualTask = gson.fromJson(response.body(), Task.class);

            assertEquals(expectedTask, actualTask, "Возвращаемая задача не равна ожидаемой");
        }

        //4хх
        @Test
        @DisplayName("Проверяем ответ сервера (404) - эндпоинт 'DELETE /tasks{id}', удаление несуществующей задачи.")
        void endpointTasks_Delete_Task_Not_Found_Test() throws IOException, InterruptedException {
            //Given
            int expectedStatusCode = 404;
            int idNotExistsTask = 666;
            HttpClient httpClient = HttpClient.newHttpClient();
            URI url = URI.create(baseUrl + "/tasks/" + idNotExistsTask);
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(url)
                    .DELETE()
                    .build();
            //When
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            //Then
            assertEquals(expectedStatusCode, response.statusCode());
        }

        @Test
        @DisplayName("Проверяем ответ сервера (406) - эндпоинт 'POST /tasks' добавление Task пересекающейся по времени.")
        void endpointTasks_Add_Task_Has_Time_Intersect_Test() throws IOException, InterruptedException {
            //Given
            int expectedStatusCode = 409;

            Task task = new Task("task", "task test", StatusProgress.NEW, 10,
                    LocalDateTime.now().withNano(0));
            taskManager.addTask(task);
            Task task409 = new Task("task409", "task409 test", StatusProgress.NEW, 10,
                    LocalDateTime.now().withNano(0));

            String task409Json = gson.toJson(task409);

            HttpClient httpClient = HttpClient.newHttpClient();
            URI url = URI.create(baseUrl + "/tasks");
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .header("Content-type", "application/json")
                    .uri(url)
                    .POST(HttpRequest.BodyPublishers.ofString(task409Json))
                    .build();
            //When
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            //Then
            assertEquals(expectedStatusCode, response.statusCode());
        }

        @Test
        @DisplayName("Проверяем ответ сервера (400) - эндпоинт 'POST /tasks' добавление Task с некорректными JSON данными.")
        void endpointTasks_Add_Task_Incorrect_Json_Test() throws IOException, InterruptedException {
            //Given
            int expectedStatusCode = 400;

            String task400JsonIncorrect = "{\"name\":\"task400\",\"description\":\"task400 test\","
                    + "\"statusProgress\":\"something\",\"durationInMinutes\":10,\"startDateTime\":\"EMPTY\"}";

            HttpClient httpClient = HttpClient.newHttpClient();
            URI url = URI.create(baseUrl + "/tasks");
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .header("Content-type", "application/json")
                    .uri(url)
                    .POST(HttpRequest.BodyPublishers.ofString(task400JsonIncorrect))
                    .build();
            //When
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            //Then
            assertEquals(expectedStatusCode, response.statusCode());
        }

        @Test
        @DisplayName("Проверяем ответ сервера (200) - эндпоинт 'GET /tasks', запрос всех Tasks, когда список пуст.")
        void endpointTasks_Get_All_Tasks_List_Empty_Test() throws IOException, InterruptedException {
            //Given
            int expectedStatusCode = 200;
            int expectedSize = 0;

            HttpClient httpClient = HttpClient.newHttpClient();
            URI url = URI.create(baseUrl + "/tasks");
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(url)
                    .GET()
                    .build();
            //When
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            //Then
            assertEquals(expectedStatusCode, response.statusCode());

            TypeToken<ArrayList<Task>> collectionType = new TypeToken<>(){};
            ArrayList<Task> tasksFromServer = gson.fromJson(response.body(), collectionType);
            assertEquals(expectedSize, tasksFromServer.size());
        }

        @Test
        @DisplayName("Проверяем ответ сервера (404) - эндпоинт 'GET /tasks{id}', запрос несуществующей Task.")
        void endpointTasks_Get_Not_Exists_Task_Test() throws IOException, InterruptedException {
            //Given
            int expectedStatusCode = 404;
            int idNotExistsTask = 666;
            HttpClient httpClient = HttpClient.newHttpClient();
            URI url = URI.create(baseUrl + "/tasks/" + idNotExistsTask);
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(url)
                    .GET()
                    .build();
            //When
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            //Then
            assertEquals(expectedStatusCode, response.statusCode());
        }
    }

    @Nested
    @DisplayName("Тесты работы эндпоинтов /subtasks.")
    class testForSubtasks {
        @Test
        @DisplayName("Проверяем эндпоинт 'DELETE /subtasks{id}' - удаление Subtask.")
        void endpointSubtasks_Delete_Subtask_Test() throws IOException, InterruptedException {
            //Given
            int expectedStatusCode = 204;

            Epic epic = new Epic("epic", "epic");
            taskManager.addEpic(epic);
            Subtask subtask = new Subtask("subtask", "test subtask", StatusProgress.NEW,
                    epic.getIdNumber(),10, LocalDateTime.now().withNano(0));
            taskManager.addSubtask(subtask);

            HttpClient httpClient = HttpClient.newHttpClient();
            URI url = URI.create(baseUrl + "/subtasks/" + subtask.getIdNumber());
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(url)
                    .DELETE()
                    .build();
            //When
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            //Then
            assertEquals(expectedStatusCode, response.statusCode());
            assertThrows(RuntimeException.class, () -> taskManager.getSubtaskByIdNumber(subtask.getIdNumber()));
        }

        @Test
        @DisplayName("Проверяем эндпоинт 'POST /subtasks' - добавление Subtask.")
        void endpointSubtasks_Add_Subtask_Test() throws IOException, InterruptedException {
            //Given
            int expectedStatusCode = 201;
            int expectedSizeSubtasksList = 1;
            String expectedSubtaskDescription = "test subtask";

            Epic epic = new Epic("epic", "epic");
            taskManager.addEpic(epic);
            Subtask subtask = new Subtask("subtask", "test subtask", StatusProgress.NEW,
                    epic.getIdNumber(),10, LocalDateTime.now().withNano(0));

            String subtaskJson = gson.toJson(subtask);

            HttpClient httpClient = HttpClient.newHttpClient();
            URI url = URI.create(baseUrl + "/subtasks");
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .header("Content-type", "application/json")
                    .uri(url)
                    .POST(HttpRequest.BodyPublishers.ofString(subtaskJson))
                    .build();
            //When
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            //Then
            assertEquals(expectedStatusCode, response.statusCode());

            ArrayList<Subtask> subtasksFromManager = taskManager.getAllSubtasks();
            String actualSubtaskDescription = subtasksFromManager.get(0).getDescription().get();
            int actualSizeSubtasksList = subtasksFromManager.size();

            assertEquals(expectedSizeSubtasksList, actualSizeSubtasksList, "Неверное количество задач");
            assertEquals(expectedSubtaskDescription, actualSubtaskDescription, "Возвращаемая задача не соответствует заданной");
        }

        @Test
        @DisplayName("Проверяем эндпоинт 'POST /subtasks' - обновление Subtask.")
        void endpointSubtasks_Update_Subtask_Test() throws IOException, InterruptedException {
            //Given
            int expectedStatusCode = 200;
            StatusProgress expectedStatusProgress = StatusProgress.IN_PROGRESS;

            Epic epic = new Epic("epic", "epic");
            taskManager.addEpic(epic);
            Subtask subtask = new Subtask("subtask", "test subtask", StatusProgress.NEW,
                    epic.getIdNumber(),10, LocalDateTime.now().withNano(0));
            taskManager.addSubtask(subtask);

            subtask.setStatusProgress(StatusProgress.IN_PROGRESS);
            String subtaskJson = gson.toJson(subtask);

            HttpClient httpClient = HttpClient.newHttpClient();
            URI url = URI.create(baseUrl + "/subtasks");
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .header("Content-type", "application/json")
                    .uri(url)
                    .POST(HttpRequest.BodyPublishers.ofString(subtaskJson))
                    .build();
            //When
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            //Then
            assertEquals(expectedStatusCode, response.statusCode());

            Subtask subtasksFromManager = taskManager.getSubtaskByIdNumber(subtask.getIdNumber());
            StatusProgress actualStatusProgress = subtasksFromManager.getStatusProgress();

            assertEquals(expectedStatusProgress, actualStatusProgress, "Задача в менеджере не обновилась");
        }

        @Test
        @DisplayName("Проверяем эндпоинт 'GET /subtasks' - получение всех Subtask.")
        void endpointSubtasks_Get_All_Subtasks_Test() throws IOException, InterruptedException {
            //Given
            int expectedStatusCode = 200;
            int expectedSizeSubtasksList = 2;

            Epic epic = new Epic("epic", "epic");
            taskManager.addEpic(epic);
            Subtask subtask1 = new Subtask("subtask1", "test subtask1", StatusProgress.NEW,
                    epic.getIdNumber(),10, LocalDateTime.now().withNano(0));
            Subtask subtask2 = new Subtask("subtask2", "test subtask2", StatusProgress.NEW,
                    epic.getIdNumber(),10, LocalDateTime.now().withNano(0).plusHours(1));
            taskManager.addSubtask(subtask1);
            taskManager.addSubtask(subtask2);

            HttpClient httpClient = HttpClient.newHttpClient();
            URI url = URI.create(baseUrl + "/subtasks");
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(url)
                    .GET()
                    .build();
            //When
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            //Then
            assertEquals(expectedStatusCode, response.statusCode());

            TypeToken<ArrayList<Subtask>> collectionType = new TypeToken<>(){};
            ArrayList<Subtask> subtasksFromServer = gson.fromJson(response.body(), collectionType);
            int actualSizeSubtasksList = subtasksFromServer.size();

            assertEquals(expectedSizeSubtasksList, actualSizeSubtasksList, "Неверное количество задач");
        }

        @Test
        @DisplayName("Проверяем эндпоинт 'GET /subtasks?from=1&size=1' - пагинация.")
        void endpointSubtasks_Get_With_Pagination_Test() throws IOException, InterruptedException {
            //Given
            int expectedStatusCode = 200;
            int expectedSize = 1;

            Epic epic = new Epic("epic", "epic");
            taskManager.addEpic(epic);
            Subtask subtask1 = new Subtask("subtask1", "test subtask1", StatusProgress.NEW,
                    epic.getIdNumber(), 10, LocalDateTime.now().withNano(0));
            Subtask subtask2 = new Subtask("subtask2", "test subtask2", StatusProgress.NEW,
                    epic.getIdNumber(), 10, LocalDateTime.now().withNano(0).plusHours(1));
            Subtask subtask3 = new Subtask("subtask3", "test subtask3", StatusProgress.NEW,
                    epic.getIdNumber(), 10, LocalDateTime.now().withNano(0).plusHours(2));
            taskManager.addSubtask(subtask1);
            taskManager.addSubtask(subtask2);
            taskManager.addSubtask(subtask3);

            HttpClient httpClient = HttpClient.newHttpClient();

            URI fullUrl = URI.create(baseUrl + "/subtasks");
            HttpRequest fullRequest = HttpRequest.newBuilder()
                    .uri(fullUrl)
                    .GET()
                    .build();
            HttpResponse<String> fullResponse = httpClient.send(fullRequest,
                    HttpResponse.BodyHandlers.ofString());

            //When
            URI url = URI.create(baseUrl + "/subtasks?from=1&size=1");
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(url)
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(httpRequest,
                    HttpResponse.BodyHandlers.ofString());

            //Then
            assertEquals(expectedStatusCode, response.statusCode());

            TypeToken<ArrayList<Subtask>> collectionType = new TypeToken<>(){};
            ArrayList<Subtask> fullList = gson.fromJson(fullResponse.body(), collectionType);
            ArrayList<Subtask> pagedList = gson.fromJson(response.body(), collectionType);

            assertEquals(expectedSize, pagedList.size(), "Неверный размер страницы");
            assertEquals(fullList.get(1).getIdNumber(), pagedList.get(0).getIdNumber(),
                    "Вернулась не вторая по порядку подзадача");
        }

        @Test
        @DisplayName("Проверяем эндпоинт 'GET /subtasks/{id}' - получение конкретной Subtask.")
        void endpointSubtasks_Get_That_Subtask_Test() throws IOException, InterruptedException {
            //Given
            int expectedStatusCode = 200;

            Epic epic = new Epic("epic", "epic");
            taskManager.addEpic(epic);
            Subtask subtask1 = new Subtask("subtask1", "test subtask1", StatusProgress.NEW,
                    epic.getIdNumber(),10, LocalDateTime.now().withNano(0));
            Subtask subtask2 = new Subtask("subtask2", "test subtask2", StatusProgress.NEW,
                    epic.getIdNumber(),10, LocalDateTime.now().withNano(0).plusHours(1));
            taskManager.addSubtask(subtask1);
            taskManager.addSubtask(subtask2);

            Subtask expectedSubtask = subtask2;

            HttpClient httpClient = HttpClient.newHttpClient();
            URI url = URI.create(baseUrl + "/subtasks/" + subtask2.getIdNumber());
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(url)
                    .GET()
                    .build();
            //When
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            //Then
            assertEquals(expectedStatusCode, response.statusCode());

            Subtask actualSubtask = gson.fromJson(response.body(), Subtask.class);

            assertEquals(expectedSubtask, actualSubtask, "Возвращаемая задача не равна ожидаемой");
        }

        //4хх
        @Test
        @DisplayName("Проверяем ответ сервера (404) - эндпоинт 'DELETE /subtasks{id}', удаление несуществующей subtask.")
        void endpointSubtasks_Delete_Subtask_Not_Found_Test() throws IOException, InterruptedException {
            //Given
            int expectedStatusCode = 404;
            int idNotExistsSubtask = 666;
            HttpClient httpClient = HttpClient.newHttpClient();
            URI url = URI.create(baseUrl + "/subtasks/" + idNotExistsSubtask);
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(url)
                    .DELETE()
                    .build();
            //When
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            //Then
            assertEquals(expectedStatusCode, response.statusCode());
        }

        @Test
        @DisplayName("Проверяем ответ сервера (406) - эндпоинт 'POST /subtasks' нельзя добавить Subtask пересекающийся по времени.")
        void endpointSubtasks_Add_Subtask_Has_Time_Intersect_Test() throws IOException, InterruptedException {
            //Given
            int expectedStatusCode = 409;

            Epic epic = new Epic("epic", "epic");
            taskManager.addEpic(epic);
            Subtask subtask1 = new Subtask("subtask1", "test subtask1", StatusProgress.NEW,
                    epic.getIdNumber(),10, LocalDateTime.now().withNano(0));
            Subtask subtask409 = new Subtask("subtask409", "test subtask409", StatusProgress.NEW,
                    epic.getIdNumber(),10, LocalDateTime.now().withNano(0));
            taskManager.addSubtask(subtask1);

            String subtask409Json = gson.toJson(subtask409);

            HttpClient httpClient = HttpClient.newHttpClient();
            URI url = URI.create(baseUrl + "/subtasks");
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .header("Content-type", "application/json")
                    .uri(url)
                    .POST(HttpRequest.BodyPublishers.ofString(subtask409Json))
                    .build();
            //When
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            //Then
            assertEquals(expectedStatusCode, response.statusCode());
        }

        @Test
        @DisplayName("Проверяем ответ сервера (400) - эндпоинт 'POST /subtasks' добавление Subtask с некорректными JSON данными.")
        void endpointSubtasks_Add_Subtask_Incorrect_Json_Test() throws IOException, InterruptedException {
            //Given
            int expectedStatusCode = 400;

            Epic epic = new Epic("epic", "epic");
            taskManager.addEpic(epic);
            String subtask400JsonIncorrect = "{\"name\":\"subtask400\",\"description\":\"test subtask400\","
                    + "\"statusProgress\":\"NEW\",\"parentEpicIdNumber\":" + epic.getIdNumber() + ","
                    + "\"durationInMinutes\":\"ten\",\"startDateTime\":\"EMPTY\"}";

            HttpClient httpClient = HttpClient.newHttpClient();
            URI url = URI.create(baseUrl + "/subtasks");
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .header("Content-type", "application/json")
                    .uri(url)
                    .POST(HttpRequest.BodyPublishers.ofString(subtask400JsonIncorrect))
                    .build();
            //When
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            //Then
            assertEquals(expectedStatusCode, response.statusCode());
        }

        @Test
        @DisplayName("Проверяем ответ сервера (200) - эндпоинт 'GET /subtasks', запрос всех Subtasks, когда список пуст.")
        void endpointSubtasks_Get_All_Subtasks_List_Empty_Test() throws IOException, InterruptedException {
            //Given
            int expectedStatusCode = 200;
            int expectedSize = 0;

            HttpClient httpClient = HttpClient.newHttpClient();
            URI url = URI.create(baseUrl + "/subtasks");
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(url)
                    .GET()
                    .build();
            //When
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            //Then
            assertEquals(expectedStatusCode, response.statusCode());

            TypeToken<ArrayList<Subtask>> collectionType = new TypeToken<>(){};
            ArrayList<Subtask> subtasksFromServer = gson.fromJson(response.body(), collectionType);
            assertEquals(expectedSize, subtasksFromServer.size());
        }

        @Test
        @DisplayName("Проверяем ответ сервера (404) - эндпоинт 'GET /subtasks{id}', запрос несуществующей Subtask.")
        void endpointSubtasks_Get_Not_Exists_Subtask_Test() throws IOException, InterruptedException {
            //Given
            int expectedStatusCode = 404;
            int idNotExistsSubtask = 666;
            HttpClient httpClient = HttpClient.newHttpClient();
            URI url = URI.create(baseUrl + "/subtasks/" + idNotExistsSubtask);
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(url)
                    .GET()
                    .build();
            //When
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            //Then
            assertEquals(expectedStatusCode, response.statusCode());
        }
    }

    @Nested
    @DisplayName("Тесты работы эндпоинтов /epics.")
    class testForEpics {
        @Test
        @DisplayName("Проверяем эндпоинт 'DELETE /epics{id}' - удаление Epic.")
        void endpointEpics_Delete_Epic_Test() throws IOException, InterruptedException {
            //Given
            int expectedStatusCode = 204;

            Epic epic = new Epic("epic", "epic");
            taskManager.addEpic(epic);

            HttpClient httpClient = HttpClient.newHttpClient();
            URI url = URI.create(baseUrl + "/epics/" + epic.getIdNumber());
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(url)
                    .DELETE()
                    .build();
            //When
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            //Then
            assertEquals(expectedStatusCode, response.statusCode());
            assertThrows(RuntimeException.class, () -> taskManager.getEpicByIdNumber(epic.getIdNumber()));
        }

        @Test
        @DisplayName("Проверяем эндпоинт 'POST /epics' - добавление Epic.")
        void endpointEpics_Add_Epic_Test() throws IOException, InterruptedException {
            //Given
            int expectedStatusCode = 201;
            int expectedSizeEpicsList = 1;
            String expectedEpicDescription = "test epic";

            Epic epic = new Epic("epic", expectedEpicDescription);

            String epicJson = gson.toJson(epic);

            HttpClient httpClient = HttpClient.newHttpClient();
            URI url = URI.create(baseUrl + "/epics");
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .header("Content-type", "application/json")
                    .uri(url)
                    .POST(HttpRequest.BodyPublishers.ofString(epicJson))
                    .build();
            //When
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            //Then
            assertEquals(expectedStatusCode, response.statusCode());

            ArrayList<Epic> epicsFromManager = taskManager.getAllEpics();
            String actualEpicDescription = epicsFromManager.get(0).getDescription().get();
            int actualSizeEpicsList = epicsFromManager.size();

            assertEquals(expectedSizeEpicsList, actualSizeEpicsList, "Неверное количество задач");
            assertEquals(expectedEpicDescription, actualEpicDescription, "Возвращаемая задача не соответствует заданной");
        }

        @Test
        @DisplayName("Проверяем эндпоинт 'GET /epics' - получение всех Epic.")
        void endpointEpics_Get_All_Epics_Test() throws IOException, InterruptedException {
            //Given
            int expectedStatusCode = 200;
            int expectedSizeEpicsList = 2;

            Epic epic1 = new Epic("epic1", "epic1");
            Epic epic2 = new Epic("epic2", "epic2");
            taskManager.addEpic(epic1);
            taskManager.addEpic(epic2);

            HttpClient httpClient = HttpClient.newHttpClient();
            URI url = URI.create(baseUrl + "/epics");
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(url)
                    .GET()
                    .build();
            //When
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            //Then
            assertEquals(expectedStatusCode, response.statusCode());

            TypeToken<ArrayList<Epic>> collectionType = new TypeToken<>(){};
            ArrayList<Epic> epicsFromServer = gson.fromJson(response.body(), collectionType);
            int actualSizeEpicsList = epicsFromServer.size();

            assertEquals(expectedSizeEpicsList, actualSizeEpicsList, "Неверное количество задач");
        }

        @Test
        @DisplayName("Проверяем эндпоинт 'GET /epics?from=1&size=1' - пагинация.")
        void endpointEpics_Get_With_Pagination_Test() throws IOException, InterruptedException {
            //Given
            int expectedStatusCode = 200;
            int expectedSize = 1;

            Epic epic1 = new Epic("epic1", "epic1");
            Epic epic2 = new Epic("epic2", "epic2");
            Epic epic3 = new Epic("epic3", "epic3");
            taskManager.addEpic(epic1);
            taskManager.addEpic(epic2);
            taskManager.addEpic(epic3);

            HttpClient httpClient = HttpClient.newHttpClient();

            URI fullUrl = URI.create(baseUrl + "/epics");
            HttpRequest fullRequest = HttpRequest.newBuilder()
                    .uri(fullUrl)
                    .GET()
                    .build();
            HttpResponse<String> fullResponse = httpClient.send(fullRequest,
                    HttpResponse.BodyHandlers.ofString());

            //When
            URI url = URI.create(baseUrl + "/epics?from=1&size=1");
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(url)
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(httpRequest,
                    HttpResponse.BodyHandlers.ofString());

            //Then
            assertEquals(expectedStatusCode, response.statusCode());

            TypeToken<ArrayList<Epic>> collectionType = new TypeToken<>(){};
            ArrayList<Epic> fullList = gson.fromJson(fullResponse.body(), collectionType);
            ArrayList<Epic> pagedList = gson.fromJson(response.body(), collectionType);

            assertEquals(expectedSize, pagedList.size(), "Неверный размер страницы");
            assertEquals(fullList.get(1).getIdNumber(), pagedList.get(0).getIdNumber(),
                    "Вернулся не второй по порядку эпик");
        }

        @Test
        @DisplayName("Проверяем эндпоинт 'GET /epics/{id}' - получение конкретного Epic.")
        void endpointEpics_Get_That_Epic_Test() throws IOException, InterruptedException {
            //Given
            int expectedStatusCode = 200;

            Epic epic = new Epic("epic", "epic");
            taskManager.addEpic(epic);

            Epic expectedEpic = epic;

            HttpClient httpClient = HttpClient.newHttpClient();
            URI url = URI.create(baseUrl + "/epics/" + epic.getIdNumber());
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(url)
                    .GET()
                    .build();
            //When
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            //Then
            assertEquals(expectedStatusCode, response.statusCode());

            Epic actualEpic = gson.fromJson(response.body(), Epic.class);

            assertEquals(expectedEpic, actualEpic, "Возвращаемая задача не равна ожидаемой");
        }

        @Test
        @DisplayName("Проверяем эндпоинт 'GET /epics/{id}/subtasks' - получение конкретного Subtask.")
        void endpointEpics_Get_That_Subtask_Test() throws IOException, InterruptedException {
            //Given
            int expectedStatusCode = 200;
            int expectedSizeSubtasksList = 2;

            Epic epic = new Epic("epic", "epic");
            taskManager.addEpic(epic);
            Subtask subtask1 = new Subtask("subtask1", "test subtask1", StatusProgress.NEW,
                    epic.getIdNumber(),10, LocalDateTime.now().withNano(0));
            Subtask subtask2 = new Subtask("subtask2", "test subtask2", StatusProgress.NEW,
                    epic.getIdNumber(),10, LocalDateTime.now().withNano(0).plusHours(1));
            taskManager.addSubtask(subtask1);
            taskManager.addSubtask(subtask2);

            HttpClient httpClient = HttpClient.newHttpClient();
            URI url = URI.create(baseUrl + "/epics/" + epic.getIdNumber() + "/subtasks");
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(url)
                    .GET()
                    .build();
            //When
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            //Then
            assertEquals(expectedStatusCode, response.statusCode());

            TypeToken<ArrayList<Subtask>> collectionType = new TypeToken<>(){};
            ArrayList<Subtask> subtasksFromServer = gson.fromJson(response.body(), collectionType);
            int actualSizeSubtasksList = subtasksFromServer.size();

            assertEquals(expectedSizeSubtasksList, actualSizeSubtasksList, "Неверное количество задач");
        }

        //4хх
        @Test
        @DisplayName("Проверяем ответ сервера (404) - эндпоинт 'DELETE /epics{id}', удаление несуществующего epic.")
        void endpointEpics_Delete_Epic_Not_Found_Test() throws IOException, InterruptedException {
            //Given
            int expectedStatusCode = 404;
            int idNotExistsEpic = 666;
            HttpClient httpClient = HttpClient.newHttpClient();
            URI url = URI.create(baseUrl + "/epics/" + idNotExistsEpic);
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(url)
                    .DELETE()
                    .build();
            //When
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            //Then
            assertEquals(expectedStatusCode, response.statusCode());
        }

        @Test
        @DisplayName("Проверяем эндпоинт 'POST /epics' - невозможность обновить Epic.")
        void endpointEpics_Update_Epic_Test() throws IOException, InterruptedException {
            //Given
            int expectedStatusCode = 400;

            Epic epic = new Epic("epic", "epic");
            taskManager.addEpic(epic);
            epic.setName("updated epic");

            String epicJson = gson.toJson(epic);

            HttpClient httpClient = HttpClient.newHttpClient();
            URI url = URI.create(baseUrl + "/epics");
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .header("Content-type", "application/json")
                    .uri(url)
                    .POST(HttpRequest.BodyPublishers.ofString(epicJson))
                    .build();
            //When
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            //Then
            assertEquals(expectedStatusCode, response.statusCode());
        }

        @Test
        @DisplayName("Проверяем ответ сервера (400) - эндпоинт 'POST /epics' нельзя добавить Epic с некорректными JSON данными.")
        void endpointEpics_Add_Epic_Incorrect_Json_Test() throws IOException, InterruptedException {
            //Given
            int expectedStatusCode = 400;

            String epic400JsonIncorrect = "{\"name\":\"epic\",\"description\":\"epic\","
                    + "\"statusProgress\":\"OLD\"}";

            HttpClient httpClient = HttpClient.newHttpClient();
            URI url = URI.create(baseUrl + "/epics");
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .header("Content-type", "application/json")
                    .uri(url)
                    .POST(HttpRequest.BodyPublishers.ofString(epic400JsonIncorrect))
                    .build();
            //When
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            //Then
            assertEquals(expectedStatusCode, response.statusCode());
        }

        @Test
        @DisplayName("Проверяем ответ сервера (200) - эндпоинт 'GET /epics', запрос всех Epics, когда список пуст.")
        void endpointEpics_Get_All_Epics_List_Empty_Test() throws IOException, InterruptedException {
            //Given
            int expectedStatusCode = 200;
            int expectedSize = 0;

            HttpClient httpClient = HttpClient.newHttpClient();
            URI url = URI.create(baseUrl + "/epics");
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(url)
                    .GET()
                    .build();
            //When
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            //Then
            assertEquals(expectedStatusCode, response.statusCode());

            TypeToken<ArrayList<Epic>> collectionType = new TypeToken<>(){};
            ArrayList<Epic> epicsFromServer = gson.fromJson(response.body(), collectionType);
            assertEquals(expectedSize, epicsFromServer.size());
        }

        @Test
        @DisplayName("Проверяем ответ сервера (404) - эндпоинт 'GET /epics{id}', запрос несуществующего Epic.")
        void endpointEpics_Get_Not_Exists_Epic_Test() throws IOException, InterruptedException {
            //Given
            int expectedStatusCode = 404;
            int idNotExistsEpic = 666;
            HttpClient httpClient = HttpClient.newHttpClient();
            URI url = URI.create(baseUrl + "/epics/" + idNotExistsEpic);
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(url)
                    .GET()
                    .build();
            //When
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            //Then
            assertEquals(expectedStatusCode, response.statusCode());
        }
    }

    @Nested
    @DisplayName("Тесты работы эндпоинтов /history.")
    class testForHistory {
        @Test
        @DisplayName("Проверяем эндпоинт 'GET /history' - получение истории.")
        void endpointHistory_Get_History_Test() throws IOException, InterruptedException {
            //Given
            int expectedStatusCode = 200;
            int expectedSizeHistory = 3;

            Task task = new Task("task", "test task", StatusProgress.NEW, 10,
                    LocalDateTime.now().withNano(0));
            taskManager.addTask(task);
            Epic epic = new Epic("epic", "epic");
            taskManager.addEpic(epic);
            Subtask subtask = new Subtask("subtask", "test subtask", StatusProgress.NEW,
                    epic.getIdNumber(),10, LocalDateTime.now().withNano(0).plusHours(1));
            taskManager.addSubtask(subtask);

            taskManager.getTaskByIdNumber(task.getIdNumber());
            taskManager.getEpicByIdNumber(epic.getIdNumber());
            taskManager.getSubtaskByIdNumber(subtask.getIdNumber());

            HttpClient httpClient = HttpClient.newHttpClient();
            URI url = URI.create(baseUrl + "/history");
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(url)
                    .GET()
                    .build();
            //When
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            //Then
            assertEquals(expectedStatusCode, response.statusCode());

            TypeToken<ArrayList<AbstractTask>> collectionType = new TypeToken<>(){};
            ArrayList<AbstractTask> historyFromServer = gson.fromJson(response.body(), collectionType);
            int actualSizeHistory = historyFromServer.size();
            assertEquals(expectedSizeHistory, actualSizeHistory, "Неверное количество задач");
        }

        @Test
        @DisplayName("Проверяем эндпоинт 'GET /history', запрос History, когда список пуст.")
        void endpointHistory_Get_History_When_Empty_Test() throws IOException, InterruptedException {
            //Given
            int expectedStatusCode = 200;
            int expectedSizeHistory = 0;

            HttpClient httpClient = HttpClient.newHttpClient();
            URI url = URI.create(baseUrl + "/history");
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(url)
                    .GET()
                    .build();
            //When
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            //Then
            assertEquals(expectedStatusCode, response.statusCode());

            TypeToken<ArrayList<AbstractTask>> collectionType = new TypeToken<>(){};
            ArrayList<AbstractTask> historyFromServer = gson.fromJson(response.body(), collectionType);
            int actualSizeHistory = historyFromServer.size();
            assertEquals(expectedSizeHistory, actualSizeHistory, "Неверное количество задач");
        }

        //4xx
        @Test
        @DisplayName("Проверяем ответ сервера (400) - эндпоинт '/history', но с методом 'DELETE'.")
        void endpointHistory_But_Delete_Test() throws IOException, InterruptedException {
            //Given
            int expectedStatusCode = 405;

            HttpClient httpClient = HttpClient.newHttpClient();
            URI url = URI.create(baseUrl + "/history");
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(url)
                    .DELETE()
                    .build();
            //When
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            //Then
            assertEquals(expectedStatusCode, response.statusCode());
        }
    }

    @Nested
    @DisplayName("Тесты работы эндпоинтов /prioritized.")
    class testForPrioritized {
        @Test
        @DisplayName("Проверяем эндпоинт 'GET /prioritized' - получение списка приоритетных задач.")
        void endpointPrioritized_Get_Prioritized_Test() throws IOException, InterruptedException {
            //Given
            int expectedStatusCode = 200;
            int expectedSizePrioritized = 2;

            Epic epic = new Epic("epic", "epic");
            taskManager.addEpic(epic);

            Subtask subtask1 = new Subtask("subtask1", "test subtask1", StatusProgress.NEW,
                    epic.getIdNumber(),10, LocalDateTime.now().withNano(0).plusHours(1));
            taskManager.addSubtask(subtask1);
            Subtask subtask2 = new Subtask("subtask2", "test subtask2", StatusProgress.NEW,
                    epic.getIdNumber());
            taskManager.addSubtask(subtask2);

            Task task1 = new Task("task1", "test task1", StatusProgress.NEW, 10,
                    LocalDateTime.now().withNano(0));
            taskManager.addTask(task1);
            Task task2 = new Task("task2", "test task2", StatusProgress.NEW);
            taskManager.addTask(task2);

            HttpClient httpClient = HttpClient.newHttpClient();
            URI url = URI.create(baseUrl + "/prioritized");
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(url)
                    .GET()
                    .build();
            //When
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            //Then
            assertEquals(expectedStatusCode, response.statusCode());

            TypeToken<ArrayList<AbstractTask>> collectionType = new TypeToken<>(){};
            ArrayList<AbstractTask> prioritizedFromServer = gson.fromJson(response.body(), collectionType);
            int actualSizePrioritized = prioritizedFromServer.size();
            assertEquals(expectedSizePrioritized, actualSizePrioritized, "Неверное количество задач");
        }

        @Test
        @DisplayName("Проверяем эндпоинт 'GET /prioritized' - получение списка приоритетных задач.")
        void endpointPrioritized_Get_Prioritized_When_Empty_Test() throws IOException, InterruptedException {
            //Given
            int expectedStatusCode = 200;
            int expectedSizePrioritized = 0;

            HttpClient httpClient = HttpClient.newHttpClient();
            URI url = URI.create(baseUrl + "/prioritized");
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(url)
                    .GET()
                    .build();
            //When
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            //Then
            assertEquals(expectedStatusCode, response.statusCode());

            TypeToken<ArrayList<AbstractTask>> collectionType = new TypeToken<>(){};
            ArrayList<AbstractTask> prioritizedFromServer = gson.fromJson(response.body(), collectionType);
            int actualSizePrioritized = prioritizedFromServer.size();
            assertEquals(expectedSizePrioritized, actualSizePrioritized, "Неверное количество задач");
        }

        //4xx
        @Test
        @DisplayName("Проверяем ответ сервера (400) - эндпоинт '/prioritized', но с методом 'DELETE'.")
        void endpointPrioritized_But_Delete_Test() throws IOException, InterruptedException {
            //Given
            int expectedStatusCode = 405;

            HttpClient httpClient = HttpClient.newHttpClient();
            URI url = URI.create(baseUrl + "/prioritized");
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(url)
                    .DELETE()
                    .build();
            //When
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            //Then
            assertEquals(expectedStatusCode, response.statusCode());
        }
    }

    @Nested
    @DisplayName("Тесты работы эндпоинтов /health.")
    class testForHealth {
        @Test
        @DisplayName("Проверяем эндпоинт 'GET /health' - возвращает статус UP.")
        void endpointHealth_Get_Test() throws IOException, InterruptedException {
            //Given
            int expectedStatusCode = 200;

            HttpClient httpClient = HttpClient.newHttpClient();
            URI url = URI.create(baseUrl + "/health");
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(url)
                    .GET()
                    .build();
            //When
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            //Then
            assertEquals(expectedStatusCode, response.statusCode());

            TypeToken<Map<String, String>> mapType = new TypeToken<>(){};
            Map<String, String> body = gson.fromJson(response.body(), mapType);

            assertNotNull(body);
            assertEquals("UP", body.get("status"));
        }

        @Test
        @DisplayName("Проверяем ответ сервера (405) - эндпоинт '/health', но с методом 'DELETE'.")
        void endpointHealth_But_Delete_Test() throws IOException, InterruptedException {
            //Given
            int expectedStatusCode = 405;

            HttpClient httpClient = HttpClient.newHttpClient();
            URI url = URI.create(baseUrl + "/health");
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(url)
                    .DELETE()
                    .build();
            //When
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            //Then
            assertEquals(expectedStatusCode, response.statusCode());
        }
    }

    @Nested
    @DisplayName("Тесты работы эндпоинтов /version.")
    class testForVersion {
        @Test
        @DisplayName("Проверяем эндпоинт 'GET /version' - возвращает версию.")
        void endpointVersion_Get_Test() throws IOException, InterruptedException {
            //Given
            int expectedStatusCode = 200;
            String expectedVersion = "0.2.0";

            HttpClient httpClient = HttpClient.newHttpClient();
            URI url = URI.create(baseUrl + "/version");
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(url)
                    .GET()
                    .build();
            //When
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            //Then
            assertEquals(expectedStatusCode, response.statusCode());

            TypeToken<Map<String, String>> mapType = new TypeToken<>(){};
            Map<String, String> body = gson.fromJson(response.body(), mapType);

            assertNotNull(body);
            assertEquals(expectedVersion, body.get("version"));
        }

        @Test
        @DisplayName("Проверяем ответ сервера (405) - эндпоинт '/version', но с методом 'DELETE'.")
        void endpointVersion_But_Delete_Test() throws IOException, InterruptedException {
            //Given
            int expectedStatusCode = 405;

            HttpClient httpClient = HttpClient.newHttpClient();
            URI url = URI.create(baseUrl + "/version");
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(url)
                    .DELETE()
                    .build();
            //When
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            //Then
            assertEquals(expectedStatusCode, response.statusCode());
        }
    }
}